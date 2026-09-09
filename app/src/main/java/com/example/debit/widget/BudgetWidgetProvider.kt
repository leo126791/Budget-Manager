package com.example.debit.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.widget.RemoteViews
import com.example.debit.MainActivity
import com.example.debit.R
import com.example.debit.data.AppDatabase
import com.example.debit.data.SettingsPreferences
import com.example.debit.data.TransactionType
import com.example.debit.data.primaryColorInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BudgetWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        updateWidgets(context, appWidgetManager, appWidgetIds)
    }

    companion object {
        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, BudgetWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            updateWidgets(context, appWidgetManager, appWidgetIds)

            // Sync QuickAddWidget theme color
            QuickAddWidgetProvider.updateAllWidgets(context)
        }

        private fun updateWidgets(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetIds: IntArray
        ) {
            CoroutineScope(Dispatchers.IO).launch {
                val db = AppDatabase.getDatabase(context)
                val prefs = SettingsPreferences(context)
                val themeColor = prefs.getThemeColor()
                val currentYM = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())

                val transactions = db.transactionDao().getAllTransactions().first()
                val currentMonthTx = transactions.filter {
                    SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date(it.date)) == currentYM
                }
                val totalExpense = currentMonthTx
                    .filter { it.type == TransactionType.EXPENSE }
                    .sumOf { it.amount }

                val globalBudget = db.budgetDao().getGlobalTotalBudget().first()
                val monthBudgets = db.budgetDao().getBudgetsByMonth(currentYM).first()
                val totalBudget = globalBudget?.amountLimit
                    ?: monthBudgets.find { it.category == "TOTAL" }?.amountLimit
                    ?: 0.0

                val remaining = if (totalBudget > 0) maxOf(0.0, totalBudget - totalExpense) else 0.0
                val percent = if (totalBudget > 0) ((totalExpense / totalBudget) * 100).toInt() else 0

                val chartBitmap = drawDonutChartBitmap(
                    spent = totalExpense,
                    limit = totalBudget,
                    primaryColor = themeColor.primaryColorInt
                )

                for (appWidgetId in appWidgetIds) {
                    val views = RemoteViews(context.packageName, R.layout.widget_budget_chart)

                    views.setImageViewBitmap(R.id.widget_chart_image, chartBitmap)
                    views.setTextViewText(R.id.widget_percent_text, "$percent%")
                    views.setTextColor(R.id.widget_percent_text, themeColor.primaryColorInt)
                    views.setTextViewText(
                        R.id.widget_spent_text,
                        "$${String.format(Locale.getDefault(), "%,.0f", totalExpense)} / $${String.format(Locale.getDefault(), "%,.0f", totalBudget)}"
                    )
                    views.setTextViewText(
                        R.id.widget_remaining_text,
                        if (totalExpense > totalBudget && totalBudget > 0) "已超支 $${String.format(Locale.getDefault(), "%,.0f", totalExpense - totalBudget)}"
                        else "剩餘 $${String.format(Locale.getDefault(), "%,.0f", remaining)}"
                    )

                    // Click intent to open MainActivity
                    val intent = Intent(context, MainActivity::class.java)
                    val pendingIntent = PendingIntent.getActivity(
                        context, 0, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    views.setOnClickPendingIntent(R.id.widget_budget_container, pendingIntent)

                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            }
        }

        private fun drawDonutChartBitmap(
            spent: Double,
            limit: Double,
            primaryColor: Int,
            sizePx: Int = 180
        ): Bitmap {
            val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val strokeWidth = sizePx * 0.22f

            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                this.strokeWidth = strokeWidth
            }

            val margin = strokeWidth / 2f + 2f
            val rect = RectF(margin, margin, sizePx - margin, sizePx - margin)

            // Track background
            paint.color = Color.parseColor("#E5E7EB")
            canvas.drawArc(rect, -90f, 360f, false, paint)

            // Expense slice
            val baseAmount = if (limit > 0) maxOf(limit, spent) else maxOf(1.0, spent)
            val sweepAngle = ((spent / baseAmount) * 360f).coerceAtMost(360.0).toFloat()

            paint.color = when {
                spent > limit && limit > 0 -> Color.parseColor("#EF4444") // Red
                spent >= limit * 0.8 && limit > 0 -> Color.parseColor("#F59E0B") // Amber
                else -> primaryColor
            }

            if (sweepAngle > 0) {
                canvas.drawArc(rect, -90f, sweepAngle, false, paint)
            }

            return bitmap
        }
    }
}
