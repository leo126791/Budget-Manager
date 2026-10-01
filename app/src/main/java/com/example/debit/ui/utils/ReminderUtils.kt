package com.example.debit.ui.utils

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.debit.MainActivity
import com.example.debit.R
import com.example.debit.data.AppLanguage
import com.example.debit.data.ReminderTime
import com.example.debit.data.SettingsPreferences
import com.example.debit.receiver.DailyReminderReceiver
import java.util.Calendar
import java.util.Locale

object ReminderUtils {

    const val ACTION_DAILY_REMINDER = "com.example.debit.ACTION_DAILY_REMINDER"

    private fun getRequestCode(reminder: ReminderTime): Int {
        return reminder.id.hashCode() and 0x7fffffff
    }

    fun scheduleReminderTime(context: Context, reminder: ReminderTime) {
        if (!reminder.enabled) {
            cancelReminderTime(context, reminder)
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, DailyReminderReceiver::class.java).apply {
            action = ACTION_DAILY_REMINDER
            putExtra("reminder_id", reminder.id)
            putExtra("reminder_hour", reminder.hour)
            putExtra("reminder_minute", reminder.minute)
        }

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val requestCode = getRequestCode(reminder)
        val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, flags)

        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, reminder.hour)
            set(Calendar.MINUTE, reminder.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            if (timeInMillis < now - 60_000L) {
                // Time is in the past (earlier than 1 minute ago), schedule for tomorrow
                add(Calendar.DAY_OF_YEAR, 1)
            } else if (timeInMillis <= now) {
                // Time is within current minute, trigger in 2 seconds
                timeInMillis = now + 2_000L
            }
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        } catch (_: Exception) {
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }

    fun cancelReminderTime(context: Context, reminder: ReminderTime) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, DailyReminderReceiver::class.java).apply {
            action = ACTION_DAILY_REMINDER
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val requestCode = getRequestCode(reminder)
        val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, flags)
        alarmManager.cancel(pendingIntent)
    }

    fun rescheduleAllReminders(context: Context) {
        val settingsPrefs = SettingsPreferences(context)
        val masterEnabled = settingsPrefs.isDailyReminderEnabled()
        val times = settingsPrefs.getReminderTimes()

        for (reminder in times) {
            if (masterEnabled && reminder.enabled) {
                scheduleReminderTime(context, reminder)
            } else {
                cancelReminderTime(context, reminder)
            }
        }
    }

    fun showRecurringItemNotification(
        context: Context,
        name: String,
        amount: Double,
        isIncome: Boolean,
        language: AppLanguage
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
        val channelId = "recurring_item_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = if (language == AppLanguage.ZH) "固定收支自動提醒" else "Recurring Item Reminders"
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "固定扣款與固定收入自動記錄提醒"
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

        val requestCode = (name.hashCode() and 0x7fffffff)
        val pendingIntent = PendingIntent.getActivity(context, requestCode, mainIntent, flags)

        val isZh = language == AppLanguage.ZH
        val title = if (isIncome) {
            if (isZh) "固定收入入帳通知" else "Recurring Income Received"
        } else {
            if (isZh) "固定扣款執行通知" else "Recurring Payment Executed"
        }

        val amountStr = String.format(Locale.getDefault(), "%,.0f", amount)
        val content = if (isIncome) {
            if (isZh) "已自動記錄固定收入：$name $${amountStr}" else "Auto-recorded recurring income: $name $${amountStr}"
        } else {
            if (isZh) "已自動記錄固定扣款：$name $${amountStr}" else "Auto-recorded recurring payment: $name $${amountStr}"
        }

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

        val notificationId = (name.hashCode() and 0x7fffffff)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            notificationManager.notify(notificationId, notification)
        }
    }
}
