package com.example.debit.ui.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.debit.data.ReminderTime
import com.example.debit.data.SettingsPreferences
import com.example.debit.receiver.DailyReminderReceiver
import java.util.Calendar

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

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, reminder.hour)
            set(Calendar.MINUTE, reminder.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.set(
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
}
