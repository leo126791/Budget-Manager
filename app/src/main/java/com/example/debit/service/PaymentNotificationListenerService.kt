package com.example.debit.service

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import android.widget.Toast
import com.example.debit.data.AppDatabase
import com.example.debit.data.AppLanguage
import com.example.debit.data.SettingsPreferences
import com.example.debit.data.Transaction
import com.example.debit.data.TransactionType
import com.example.debit.widget.BudgetWidgetProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.regex.Pattern
import kotlin.math.abs

class PaymentNotificationListenerService : NotificationListenerService() {

    private data class RecentPayment(
        val amount: Double,
        val timestamp: Long
    )

    companion object {
        private val recentPaymentsCache = mutableListOf<RecentPayment>()
        private const val DEDUPLICATION_WINDOW_MS = 60_000L // 60 seconds time window for duplicate payment notifications

        fun isPermissionGranted(context: Context): Boolean {
            val packageName = context.packageName
            val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
            return flat != null && flat.contains(packageName)
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val context = applicationContext
        val settingsPrefs = SettingsPreferences(context)
        val betaOn = settingsPrefs.isBetaTestingEnabled()
        val gpayOn = settingsPrefs.isGooglePayListenerEnabled()
        val packageName = sbn.packageName ?: ""

        if (!betaOn || !gpayOn) return

        val extras = sbn.notification?.extras ?: return

        fun getStr(key: String): String {
            return try {
                extras.getCharSequence(key)?.toString() ?: ""
            } catch (_: Exception) {
                ""
            }
        }

        val title = getStr("android.title")
        val text = getStr("android.text")
        val bigText = getStr("android.bigText")
        val subText = getStr("android.subText")
        val titleBig = getStr("android.title.big")
        val summaryText = getStr("android.summaryText")

        val combined = "$title $text $bigText $subText $titleBig $summaryText".trim()

        Log.d("PaymentService", "Notification received from pkg: $packageName | text: '$combined'")

        // 1. Strict Exclusions: Exclude promotional / marketing / advertisement messages
        val promoKeywords = listOf(
            "優惠", "折扣", "點數", "抽獎", "優惠券", "回饋", "領取", "現折", "廣告", "促銷",
            "限時", "邀請", "訂閱", "推薦", "特價", "紅利", "序號", "禮券", "好康", "活動", "問券"
        )
        if (promoKeywords.any { combined.contains(it) }) {
            Log.d("PaymentService", "Excluded promotional/marketing message. Skipping.")
            return
        }

        // 2. Strict Google Pay / Wallet Identification
        val isGoogleWalletPkg = packageName.contains("wallet") || packageName.contains("gpay") ||
                packageName == "com.google.android.apps.walletnf" || packageName == "com.google.android.apps.wallet"
        val isExplicitGooglePay = combined.contains("Google Pay") || combined.contains("Google Wallet")
        val isShellTest = packageName.contains("shell")

        if (!isGoogleWalletPkg && !isExplicitGooglePay && !isShellTest) {
            Log.d("PaymentService", "Not a Google Pay / Wallet notification. Skipping.")
            return
        }

        // 3. Must contain payment transaction action keywords
        val paymentKeywords = listOf(
            "付款", "支付", "消費", "刷卡", "扣款", "交易", "Tap to pay", "Paid", "Spent"
        )
        if (!paymentKeywords.any { combined.contains(it) }) {
            Log.d("PaymentService", "No payment action keyword found. Skipping.")
            return
        }

        // 4. Extract Amount
        val amount = extractAmount(combined)
        if (amount == null || amount <= 0.0) {
            Log.d("PaymentService", "Could not extract valid amount from: '$combined'. Skipping.")
            return
        }

        val currentTime = System.currentTimeMillis()

        // 5. In-Memory Deduplication Check (Window: 60s)
        synchronized(recentPaymentsCache) {
            recentPaymentsCache.removeAll { currentTime - it.timestamp > DEDUPLICATION_WINDOW_MS }
            val isInMemoryDuplicate = recentPaymentsCache.any {
                abs(it.amount - amount) < 0.01 && (currentTime - it.timestamp) < DEDUPLICATION_WINDOW_MS
            }
            if (isInMemoryDuplicate) {
                Log.d("PaymentService", "Duplicate payment detected in cache ($amount within 60s). Skipping.")
                return
            }
        }

        val merchantName = extractMerchant(title, text)
        val category = inferCategory(combined, merchantName)
        val isZh = settingsPrefs.getLanguage() == AppLanguage.ZH

        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getDatabase(context)

            // 6. Database Deduplication Check (Window: 60s)
            val startTime = currentTime - DEDUPLICATION_WINDOW_MS
            val recentTxs = db.transactionDao().getRecentTransactions(startTime, currentTime + 5_000L)
            val isDbDuplicate = recentTxs.any { abs(it.amount - amount) < 0.01 }

            if (isDbDuplicate) {
                Log.d("PaymentService", "Duplicate payment detected in DB ($amount within 60s). Skipping.")
                return@launch
            }

            // Add to cache
            synchronized(recentPaymentsCache) {
                recentPaymentsCache.add(RecentPayment(amount, currentTime))
            }

            val transactionNote = if (merchantName.isNotBlank()) {
                "Google Pay • $merchantName"
            } else {
                if (isZh) "Google Pay 自動捕捉" else "Google Pay Auto-Captured"
            }

            val newTx = Transaction(
                amount = amount,
                category = category,
                type = TransactionType.EXPENSE,
                date = currentTime,
                note = transactionNote,
                locationName = merchantName,
                accountName = "Google Pay"
            )

            db.transactionDao().insertTransaction(newTx)
            BudgetWidgetProvider.updateAllWidgets(context)

            Log.d("PaymentService", "Successfully recorded payment! Amount: $amount, Merchant: '$merchantName', Category: $category")

            Handler(Looper.getMainLooper()).post {
                val displayMsg = if (isZh) {
                    "🎉 自動記錄 Google Pay 消費 $${amount.toInt()} ($category)"
                } else {
                    "🎉 Auto-recorded Google Pay: $${amount.toInt()} ($category)"
                }
                Toast.makeText(context, displayMsg, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun extractAmount(text: String): Double? {
        val patterns = listOf(
            Pattern.compile("""(?:NT\$|TWD|\$|新臺幣|新台幣)\s*([0-9,]+(?:\.[0-9]+)?)"""),
            Pattern.compile("""(?:消費|付款|金額|支出|支付)\s*[:：]?\s*\$?\s*([0-9,]+(?:\.[0-9]+)?)"""),
            Pattern.compile("""([0-9,]+(?:\.[0-9]+)?)\s*(?:元|TWD|\$)""")
        )

        for (pattern in patterns) {
            val matcher = pattern.matcher(text)
            if (matcher.find()) {
                val rawNum = matcher.group(1)?.replace(",", "") ?: continue
                val parsed = rawNum.toDoubleOrNull()
                if (parsed != null && parsed > 0) {
                    return parsed
                }
            }
        }
        return null
    }

    private fun extractMerchant(title: String, text: String): String {
        val combined = "$title $text"
        val knownMerchants = listOf(
            "7-ELEVEN", "7-11", "全家", "萊爾富", "OK超商", "全聯", "麥當勞", "摩斯漢堡",
            "肯德基", "星巴克", "家樂福", "寶雅", "大潤發", "中油", "台亞", "捷運", "公車",
            "Uber", "Foodpanda", "55688", "蝦皮", "PChome", "momo"
        )
        for (merchant in knownMerchants) {
            if (combined.contains(merchant, ignoreCase = true)) {
                return merchant
            }
        }
        if (title.isNotBlank() && !title.contains("Google") && !title.contains("付款") && !title.contains("消費")) {
            return title.trim()
        }
        return ""
    }

    private fun inferCategory(combined: String, merchant: String): String {
        return when {
            combined.contains("餐") || combined.contains("飯") || combined.contains("麵") ||
                    combined.contains("麥當勞") || combined.contains("肯德基") || combined.contains("星巴克") ||
                    combined.contains("7-11") || combined.contains("7-ELEVEN") || combined.contains("全家") -> "餐飲"

            combined.contains("中油") || combined.contains("加油") || combined.contains("捷運") ||
                    combined.contains("公車") || combined.contains("Uber") || combined.contains("高鐵") || combined.contains("台鐵") -> "交通"

            combined.contains("家樂福") || combined.contains("全聯") || combined.contains("寶雅") || combined.contains("大潤發") -> "日常"

            combined.contains("電影") || combined.contains("遊戲") || combined.contains("KTV") -> "娛樂"

            combined.contains("診所") || combined.contains("醫院") || combined.contains("藥局") -> "醫療"

            else -> "日常"
        }
    }
}
