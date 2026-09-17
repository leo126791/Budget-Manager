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
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.debit.MainActivity
import com.example.debit.R
import com.example.debit.data.AppLanguage
import com.example.debit.data.SettingsPreferences
import com.example.debit.ui.utils.ReminderUtils

class DailyReminderReceiver : BroadcastReceiver() {

    companion object {
        fun showReminderNotification(
            context: Context,
            settingsPrefs: SettingsPreferences,
            hour: Int = 21,
            minute: Int = 0
        ) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
            val channelId = "daily_reminder_channel_v2"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    "每日記帳提醒",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "定時提醒使用者記錄開銷"
                    enableVibration(true)
                    enableLights(true)
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
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()

            val notificationId = 2000 + (hour * 60 + minute)

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                notificationManager.notify(notificationId, notification)
                Log.d("DailyReminder", "Notification sent successfully with id: $notificationId")
            } else {
                Log.w("DailyReminder", "POST_NOTIFICATIONS permission not granted. Skipping notification.")
            }
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return

        val settingsPrefs = SettingsPreferences(context)
        val action = intent?.action

        Log.d("DailyReminder", "onReceive triggered with action: $action")

        if (action == Intent.ACTION_BOOT_COMPLETED) {
            ReminderUtils.rescheduleAllReminders(context)
            return
        }

        if (action == ReminderUtils.ACTION_DAILY_REMINDER || action == null) {
            if (settingsPrefs.isDailyReminderEnabled()) {
                val hour = intent?.getIntExtra("reminder_hour", 21) ?: 21
                val minute = intent?.getIntExtra("reminder_minute", 0) ?: 0
                showReminderNotification(context, settingsPrefs, hour, minute)
                ReminderUtils.rescheduleAllReminders(context)
            }
        }
    }
}
