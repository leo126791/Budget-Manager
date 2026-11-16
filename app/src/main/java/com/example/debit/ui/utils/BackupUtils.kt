package com.example.debit.ui.utils

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.debit.data.Budget
import com.example.debit.data.SavingsGoal
import com.example.debit.data.SettingsPreferences
import com.example.debit.data.Subscription
import com.example.debit.data.Transaction
import com.example.debit.data.TransactionType
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SystemSettingsBackup(
    val themeColor: String,
    val isDarkMode: Boolean,
    val language: String,
    val betaTesting: Boolean,
    val googlePayListener: Boolean,
    val dailyReminderEnabled: Boolean,
    val reminderHour: Int,
    val reminderMinute: Int,
    val reminderTimesRaw: String,
    val confirmDelete: Boolean,
    val incomeTracking: Boolean,
    val searchFeature: Boolean,
    val subscriptionFeature: Boolean,
    val multiAccount: Boolean,
    val modern3DUi: Boolean,
    val dragDateReorder: Boolean,
    val autoBackup: Boolean
)

data class FullBackupData(
    val transactions: List<Transaction>,
    val budgets: List<Budget>,
    val subscriptions: List<Subscription> = emptyList(),
    val savingsGoals: List<SavingsGoal> = emptyList(),
    val settings: SystemSettingsBackup? = null
)

data class BackupCompareInfo(
    val exportDate: Long,
    val jsonContent: String,
    val cloudTxCount: Int,
    val cloudTotalExpense: Double,
    val cloudTotalIncome: Double,
    val localTxCount: Int,
    val localTotalExpense: Double,
    val localTotalIncome: Double,
    val uniqueCloudCount: Int,
    val cloudSubscriptionCount: Int,
    val cloudSavingsGoalCount: Int,
    val hasSettings: Boolean,
    val parsedFullBackup: FullBackupData
)

object BackupUtils {

    fun createBackupJson(
        transactions: List<Transaction>,
        budgets: List<Budget>,
        subscriptions: List<Subscription> = emptyList(),
        savingsGoals: List<SavingsGoal> = emptyList(),
        settingsPrefs: SettingsPreferences? = null
    ): String {
        val root = JSONObject()
        root.put("version", 2)
        root.put("exportDate", System.currentTimeMillis())

        // 1. Transactions
        val txArray = JSONArray()
        transactions.forEach { tx ->
            val obj = JSONObject()
            obj.put("amount", tx.amount)
            obj.put("category", tx.category)
            obj.put("type", tx.type.name)
            obj.put("date", tx.date)
            obj.put("note", tx.note)
            obj.put("locationName", tx.locationName)
            obj.put("deductFromPool", tx.deductFromPool)
            obj.put("accountName", tx.accountName)
            txArray.put(obj)
        }
        root.put("transactions", txArray)

        // 2. Budgets
        val bgtArray = JSONArray()
        budgets.forEach { bgt ->
            val obj = JSONObject()
            obj.put("category", bgt.category)
            obj.put("amountLimit", bgt.amountLimit)
            obj.put("yearMonth", bgt.yearMonth)
            bgtArray.put(obj)
        }
        root.put("budgets", bgtArray)

        // 3. Subscriptions (Recurring items)
        val subArray = JSONArray()
        subscriptions.forEach { sub ->
            val obj = JSONObject()
            obj.put("name", sub.name)
            obj.put("amount", sub.amount)
            obj.put("billingDay", sub.billingDay)
            obj.put("billingMonth", sub.billingMonth)
            obj.put("category", sub.category)
            obj.put("isAnnual", sub.isAnnual)
            obj.put("type", sub.type.name)
            obj.put("isCustomInterval", sub.isCustomInterval)
            obj.put("intervalDays", sub.intervalDays)
            obj.put("billingHour", sub.billingHour)
            obj.put("billingMinute", sub.billingMinute)
            obj.put("lastProcessedDate", sub.lastProcessedDate)
            subArray.put(obj)
        }
        root.put("subscriptions", subArray)

        // 4. Savings Goals
        val goalsArray = JSONArray()
        savingsGoals.forEach { goal ->
            val obj = JSONObject()
            obj.put("title", goal.title)
            obj.put("targetAmount", goal.targetAmount)
            obj.put("currentAmount", goal.currentAmount)
            goalsArray.put(obj)
        }
        root.put("savingsGoals", goalsArray)

        // 5. System Settings
        if (settingsPrefs != null) {
            val settingsObj = JSONObject()
            settingsObj.put("themeColor", settingsPrefs.getThemeColor().name)
            settingsObj.put("isDarkMode", settingsPrefs.isDarkMode())
            settingsObj.put("language", settingsPrefs.getLanguage().name)
            settingsObj.put("betaTesting", settingsPrefs.isBetaTestingEnabled())
            settingsObj.put("googlePayListener", settingsPrefs.isGooglePayListenerEnabled())
            settingsObj.put("dailyReminderEnabled", settingsPrefs.isDailyReminderEnabled())
            settingsObj.put("reminderHour", settingsPrefs.getReminderHour())
            settingsObj.put("reminderMinute", settingsPrefs.getReminderMinute())

            val reminderList = settingsPrefs.getReminderTimes()
            val reminderSerialized = reminderList.joinToString("|") { "${it.id},${it.hour},${it.minute},${if (it.enabled) 1 else 0}" }
            settingsObj.put("reminderTimesRaw", reminderSerialized)

            settingsObj.put("confirmDelete", settingsPrefs.isConfirmDeleteEnabled())
            settingsObj.put("incomeTracking", settingsPrefs.isIncomeTrackingEnabled())
            settingsObj.put("searchFeature", settingsPrefs.isSearchEnabled())
            settingsObj.put("subscriptionFeature", settingsPrefs.isSubscriptionEnabled())
            settingsObj.put("multiAccount", settingsPrefs.isMultiAccountEnabled())
            settingsObj.put("modern3DUi", settingsPrefs.isModern3DUiEnabled())
            settingsObj.put("dragDateReorder", settingsPrefs.isDragDateReorderEnabled())
            settingsObj.put("autoBackup", settingsPrefs.isAutoBackupEnabled())
            root.put("settings", settingsObj)
        }

        return root.toString(2)
    }

    fun parseFullBackup(jsonString: String): FullBackupData {
        val root = JSONObject(jsonString)
        val txList = mutableListOf<Transaction>()
        val bgtList = mutableListOf<Budget>()
        val subList = mutableListOf<Subscription>()
        val goalsList = mutableListOf<SavingsGoal>()
        var settings: SystemSettingsBackup? = null

        if (root.has("transactions")) {
            val txArray = root.getJSONArray("transactions")
            for (i in 0 until txArray.length()) {
                val obj = txArray.getJSONObject(i)
                val amount = obj.getDouble("amount")
                val category = obj.getString("category")
                val typeStr = obj.optString("type", "EXPENSE")
                val type = try { TransactionType.valueOf(typeStr) } catch (_: Exception) { TransactionType.EXPENSE }
                val date = obj.optLong("date", System.currentTimeMillis())
                val note = obj.optString("note", "")
                val locationName = obj.optString("locationName", "")
                val deductFromPool = obj.optBoolean("deductFromPool", false)
                val accountName = obj.optString("accountName", "現金")

                txList.add(
                    Transaction(
                        amount = amount,
                        category = category,
                        type = type,
                        date = date,
                        note = note,
                        locationName = locationName,
                        deductFromPool = deductFromPool,
                        accountName = accountName
                    )
                )
            }
        }

        if (root.has("budgets")) {
            val bgtArray = root.getJSONArray("budgets")
            for (i in 0 until bgtArray.length()) {
                val obj = bgtArray.getJSONObject(i)
                val category = obj.getString("category")
                val amountLimit = obj.getDouble("amountLimit")
                val yearMonth = obj.optString("yearMonth", "GLOBAL")

                bgtList.add(
                    Budget(
                        category = category,
                        amountLimit = amountLimit,
                        yearMonth = yearMonth
                    )
                )
            }
        }

        if (root.has("subscriptions")) {
            val subArray = root.getJSONArray("subscriptions")
            for (i in 0 until subArray.length()) {
                val obj = subArray.getJSONObject(i)
                val name = obj.getString("name")
                val amount = obj.getDouble("amount")
                val billingDay = obj.optInt("billingDay", 1)
                val billingMonth = obj.optInt("billingMonth", 1)
                val category = obj.optString("category", "日常")
                val isAnnual = obj.optBoolean("isAnnual", false)
                val typeStr = obj.optString("type", "EXPENSE")
                val type = try { TransactionType.valueOf(typeStr) } catch (_: Exception) { TransactionType.EXPENSE }
                val isCustomInterval = obj.optBoolean("isCustomInterval", false)
                val intervalDays = obj.optInt("intervalDays", 30)
                val billingHour = obj.optInt("billingHour", 9)
                val billingMinute = obj.optInt("billingMinute", 0)
                val lastProcessedDate = obj.optLong("lastProcessedDate", 0L)

                subList.add(
                    Subscription(
                        name = name,
                        amount = amount,
                        billingDay = billingDay,
                        billingMonth = billingMonth,
                        category = category,
                        isAnnual = isAnnual,
                        type = type,
                        isCustomInterval = isCustomInterval,
                        intervalDays = intervalDays,
                        billingHour = billingHour,
                        billingMinute = billingMinute,
                        lastProcessedDate = lastProcessedDate
                    )
                )
            }
        }

        if (root.has("savingsGoals")) {
            val goalsArray = root.getJSONArray("savingsGoals")
            for (i in 0 until goalsArray.length()) {
                val obj = goalsArray.getJSONObject(i)
                val title = obj.getString("title")
                val targetAmount = obj.getDouble("targetAmount")
                val currentAmount = obj.optDouble("currentAmount", 0.0)

                goalsList.add(
                    SavingsGoal(
                        title = title,
                        targetAmount = targetAmount,
                        currentAmount = currentAmount
                    )
                )
            }
        }

        if (root.has("settings")) {
            val s = root.getJSONObject("settings")
            settings = SystemSettingsBackup(
                themeColor = s.optString("themeColor", "EMERALD"),
                isDarkMode = s.optBoolean("isDarkMode", false),
                language = s.optString("language", "ZH"),
                betaTesting = s.optBoolean("betaTesting", true),
                googlePayListener = s.optBoolean("googlePayListener", false),
                dailyReminderEnabled = s.optBoolean("dailyReminderEnabled", false),
                reminderHour = s.optInt("reminderHour", 21),
                reminderMinute = s.optInt("reminderMinute", 0),
                reminderTimesRaw = s.optString("reminderTimesRaw", ""),
                confirmDelete = s.optBoolean("confirmDelete", true),
                incomeTracking = s.optBoolean("incomeTracking", true),
                searchFeature = s.optBoolean("searchFeature", true),
                subscriptionFeature = s.optBoolean("subscriptionFeature", true),
                multiAccount = s.optBoolean("multiAccount", true),
                modern3DUi = s.optBoolean("modern3DUi", true),
                dragDateReorder = s.optBoolean("dragDateReorder", true),
                autoBackup = s.optBoolean("autoBackup", true)
            )
        }

        return FullBackupData(
            transactions = txList,
            budgets = bgtList,
            subscriptions = subList,
            savingsGoals = goalsList,
            settings = settings
        )
    }

    fun parseBackupJson(jsonString: String): Pair<List<Transaction>, List<Budget>> {
        val full = parseFullBackup(jsonString)
        return Pair(full.transactions, full.budgets)
    }

    fun parseCompareInfo(
        jsonString: String,
        localTransactions: List<Transaction>
    ): BackupCompareInfo {
        val root = JSONObject(jsonString)
        val exportDate = root.optLong("exportDate", System.currentTimeMillis())
        val fullBackup = parseFullBackup(jsonString)

        val cloudExpense = fullBackup.transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val cloudIncome = fullBackup.transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }

        val localExpense = localTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val localIncome = localTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }

        val localKeys = localTransactions.map { "${it.date}_${it.amount}_${it.category}_${it.note}" }.toSet()
        val uniqueCloudCount = fullBackup.transactions.count { tx ->
            val key = "${tx.date}_${tx.amount}_${tx.category}_${tx.note}"
            !localKeys.contains(key)
        }

        return BackupCompareInfo(
            exportDate = exportDate,
            jsonContent = jsonString,
            cloudTxCount = fullBackup.transactions.size,
            cloudTotalExpense = cloudExpense,
            cloudTotalIncome = cloudIncome,
            localTxCount = localTransactions.size,
            localTotalExpense = localExpense,
            localTotalIncome = localIncome,
            uniqueCloudCount = uniqueCloudCount,
            cloudSubscriptionCount = fullBackup.subscriptions.size,
            cloudSavingsGoalCount = fullBackup.savingsGoals.size,
            hasSettings = fullBackup.settings != null,
            parsedFullBackup = fullBackup
        )
    }

    fun performAutoBackup(
        context: Context,
        transactions: List<Transaction>,
        budgets: List<Budget>,
        subscriptions: List<Subscription> = emptyList(),
        savingsGoals: List<SavingsGoal> = emptyList(),
        settingsPrefs: SettingsPreferences? = null
    ): File? {
        return try {
            val jsonContent = createBackupJson(
                transactions = transactions,
                budgets = budgets,
                subscriptions = subscriptions,
                savingsGoals = savingsGoals,
                settingsPrefs = settingsPrefs
            )
            val backupDir = File(context.filesDir, "auto_backups")
            if (!backupDir.exists()) backupDir.mkdirs()

            val latestFile = File(backupDir, "auto_backup_latest.json")
            val prevFile = File(backupDir, "auto_backup_prev.json")

            if (latestFile.exists()) {
                latestFile.copyTo(prevFile, overwrite = true)
            }

            val tempFile = File(backupDir, "auto_backup_latest.tmp")
            tempFile.writeText(jsonContent, Charsets.UTF_8)
            tempFile.renameTo(latestFile)

            val prefs = SettingsPreferences(context)
            prefs.setLastAutoBackupTime(System.currentTimeMillis())

            latestFile
        } catch (_: Exception) {
            null
        }
    }

    fun backupToGoogleDrive(
        context: Context,
        transactions: List<Transaction>,
        budgets: List<Budget>,
        subscriptions: List<Subscription> = emptyList(),
        savingsGoals: List<SavingsGoal> = emptyList(),
        settingsPrefs: SettingsPreferences? = null
    ) {
        val jsonContent = createBackupJson(
            transactions = transactions,
            budgets = budgets,
            subscriptions = subscriptions,
            savingsGoals = savingsGoals,
            settingsPrefs = settingsPrefs
        )
        val dateStr = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
        val fileName = "BudgetManager_Backup_$dateStr.json"
        val file = File(context.cacheDir, fileName)
        file.writeText(jsonContent, Charsets.UTF_8)

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Budget Manager 完整雲端備份 ($dateStr)")
            putExtra(Intent.EXTRA_TEXT, "這是您的 Budget Manager 完整記帳與系統設定備份檔，請儲存至 Google Drive。")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooserIntent = Intent.createChooser(shareIntent, "備份至 Google Drive / 雲端")
        context.startActivity(chooserIntent)
    }
}
