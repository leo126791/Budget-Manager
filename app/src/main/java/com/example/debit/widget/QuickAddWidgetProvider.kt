package com.example.debit.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.widget.RemoteViews
import com.example.debit.QuickAddActivity
import com.example.debit.R
import com.example.debit.data.SettingsPreferences
import com.example.debit.data.darkPrimaryColorInt
import com.example.debit.data.primaryColorInt

class QuickAddWidgetProvider : AppWidgetProvider() {

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
            val componentName = ComponentName(context, QuickAddWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            updateWidgets(context, appWidgetManager, appWidgetIds)
        }

        private fun updateWidgets(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetIds: IntArray
        ) {
            val prefs = SettingsPreferences(context)
            val themeColor = prefs.getThemeColor()
            val bgBitmap = drawQuickAddBackground(
                startColor = themeColor.primaryColorInt,
                endColor = themeColor.darkPrimaryColorInt
            )

            for (appWidgetId in appWidgetIds) {
                val views = RemoteViews(context.packageName, R.layout.widget_quick_add)
                views.setImageViewBitmap(R.id.widget_quick_add_bg, bgBitmap)

                val expenseIntent = Intent(context, QuickAddActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                val expensePendingIntent = PendingIntent.getActivity(
                    context,
                    101,
                    expenseIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_btn_expense, expensePendingIntent)
                views.setOnClickPendingIntent(R.id.widget_quick_add_container, expensePendingIntent)

                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }

        private fun drawQuickAddBackground(
            widthPx: Int = 200,
            heightPx: Int = 200,
            startColor: Int,
            endColor: Int,
            cornerRadiusPx: Float = 40f
        ): Bitmap {
            val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            val gradient = LinearGradient(
                0f, 0f, widthPx.toFloat(), heightPx.toFloat(),
                startColor, endColor, Shader.TileMode.CLAMP
            )
            paint.shader = gradient
            val rect = RectF(0f, 0f, widthPx.toFloat(), heightPx.toFloat())
            canvas.drawRoundRect(rect, cornerRadiusPx, cornerRadiusPx, paint)
            return bitmap
        }
    }
}
