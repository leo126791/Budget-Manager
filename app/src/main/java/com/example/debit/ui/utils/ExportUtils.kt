package com.example.debit.ui.utils

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.debit.data.AppLanguage
import com.example.debit.data.Transaction
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportUtils {
    fun exportTransactionsToCsv(
        context: Context,
        transactions: List<Transaction>,
        language: AppLanguage
    ) {
        val isZh = language == AppLanguage.ZH
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        val sb = StringBuilder()
        // UTF-8 BOM for Excel / Numbers compatibility
        sb.append("\uFEFF")

        // CSV Header
        if (isZh) {
            sb.append("ID,日期,時間,類別,金額($),備註\n")
        } else {
            sb.append("ID,Date,Time,Category,Amount($),Note\n")
        }

        // CSV Rows
        transactions.sortedBy { it.date }.forEach { tx ->
            val dateStr = dateFormat.format(Date(tx.date))
            val timeStr = timeFormat.format(Date(tx.date))
            val catStr = AppStrings.getCategoryName(tx.category, language)
            val noteClean = tx.note.replace(",", " ").replace("\n", " ")
            val amountStr = if (tx.amount % 1.0 == 0.0) tx.amount.toInt().toString() else tx.amount.toString()

            sb.append("${tx.id},$dateStr,$timeStr,\"$catStr\",$amountStr,\"$noteClean\"\n")
        }

        val exportDate = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
        val fileName = "BudgetManager_Report_$exportDate.csv"
        val file = File(context.cacheDir, fileName)
        file.writeText(sb.toString(), Charsets.UTF_8)

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, if (isZh) "Budget Manager 財務報表 ($exportDate)" else "Budget Manager Financial Report ($exportDate)")
            putExtra(Intent.EXTRA_TEXT, if (isZh) "附檔為您的 Budget Manager 記帳財務報表 CSV 檔案。" else "Attached is your Budget Manager financial report CSV file.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooserTitle = if (isZh) "匯出/分享財務報表 (CSV)" else "Export / Share Report (CSV)"
        context.startActivity(Intent.createChooser(shareIntent, chooserTitle))
    }
}
