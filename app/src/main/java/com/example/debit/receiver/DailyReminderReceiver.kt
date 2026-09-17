package com.example.debit.receiver

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.debit.MainActivity
import com.example.debit.R
import com.example.debit.data.AppLanguage
import com.example.debit.data.SettingsPreferences
import com.example.debit.ui.utils.ReminderUtils

class DailyReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return

        val settingsPrefs = SettingsPreferences(context)

        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            ReminderUtils.rescheduleAllReminders(context)
            return
        }

        if (intent?.action == ReminderUtils.ACTION_DAILY_REMINDER || intent?.action == null) {
            if (settingsPrefs.isDailyReminderEnabled()) {
                showReminderNotification(context, settingsPrefs)
                ReminderUtils.rescheduleAllReminders(context)
            }
        }
    }

    private fun showReminderNotification(context: Context, settingsPrefs: SettingsPreferences) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
        val channelId = "daily_reminder_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "每日記帳提醒",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "定時提醒使用者記錄開銷"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getActivity(context, 0, mainIntent, flags)

        val isZh = settingsPrefs.getLanguage() == AppLanguage.ZH
        val title = if (isZh) "記帳時間到了" else "Time to Log Expenses"
        val content = if (isZh) "今天有消費嗎？花一分鐘記錄今天的開銷吧！" else "Logged your expenses today? Spend a minute to record today's spending!"

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            notificationManager.notify(2001, notification)
        }
    }
}
