package com.example.debit.ui.utils

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.debit.data.Budget
import com.example.debit.data.SettingsPreferences
import com.example.debit.data.Transaction
import com.example.debit.data.TransactionType
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BackupUtils {

    fun createBackupJson(transactions: List<Transaction>, budgets: List<Budget>): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("exportDate", System.currentTimeMillis())

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
            txArray.put(obj)
        }
        root.put("transactions", txArray)

        val bgtArray = JSONArray()
        budgets.forEach { bgt ->
            val obj = JSONObject()
            obj.put("category", bgt.category)
            obj.put("amountLimit", bgt.amountLimit)
            obj.put("yearMonth", bgt.yearMonth)
            bgtArray.put(obj)
        }
        root.put("budgets", bgtArray)

        return root.toString(2)
    }

    fun parseBackupJson(jsonString: String): Pair<List<Transaction>, List<Budget>> {
        val root = JSONObject(jsonString)
        val txList = mutableListOf<Transaction>()
        val bgtList = mutableListOf<Budget>()

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

                txList.add(
                    Transaction(
                        amount = amount,
                        category = category,
                        type = type,
                        date = date,
                        note = note,
                        locationName = locationName,
                        deductFromPool = deductFromPool
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

        return Pair(txList, bgtList)
    }

    fun performAutoBackup(
        context: Context,
        transactions: List<Transaction>,
        budgets: List<Budget>
    ): File? {
        return try {
            val jsonContent = createBackupJson(transactions, budgets)
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
        budgets: List<Budget>
    ) {
        val jsonContent = createBackupJson(transactions, budgets)
        val dateStr = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
        val fileName = "BudgetManager_Backup_$dateStr.json"
        val file = File(context.cacheDir, fileName)
        file.writeText(jsonContent, Charsets.UTF_8)

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Budget Manager Google Drive 備份 ($dateStr)")
            putExtra(Intent.EXTRA_TEXT, "這是您的 Budget Manager 記帳雲端備份檔，請儲存至 Google Drive。")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooserIntent = Intent.createChooser(shareIntent, "備份至 Google Drive / 雲端")
        context.startActivity(chooserIntent)
    }
}
