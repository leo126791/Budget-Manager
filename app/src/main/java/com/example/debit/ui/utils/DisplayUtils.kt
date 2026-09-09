package com.example.debit.ui.utils

import android.app.Activity
import android.os.Build
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Requests 120Hz or highest supported display refresh rate for ultra-smooth UI scrolling and animations.
 */
@Suppress("DEPRECATION")
fun requestHighRefreshRate(activity: Activity) {
    try {
        val window = activity.window
        val layoutParams = window.attributes

        val display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity.display
        } else {
            window.windowManager.defaultDisplay
        }

        val modes = display?.supportedModes ?: emptyArray()
        val maxMode = modes.maxByOrNull { it.refreshRate }

        if (maxMode != null) {
            layoutParams.preferredDisplayModeId = maxMode.modeId
            window.attributes = layoutParams
        }
    } catch (_: Exception) {
        // Ignore fallback if device window manager restricts display mode override
    }
}

/**
 * Thread-safe cached SimpleDateFormat formatters to eliminate GC object churn during list scrolling.
 */
object DateFormatUtils {
    private val ymFormatThreadLocal = object : ThreadLocal<SimpleDateFormat>() {
        override fun initialValue(): SimpleDateFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
    }

    private val ymdFormatThreadLocal = object : ThreadLocal<SimpleDateFormat>() {
        override fun initialValue(): SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    }

    private val hmFormatThreadLocal = object : ThreadLocal<SimpleDateFormat>() {
        override fun initialValue(): SimpleDateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    }

    fun formatYM(date: Date): String = ymFormatThreadLocal.get()?.format(date) ?: ""
    fun formatYMD(date: Date): String = ymdFormatThreadLocal.get()?.format(date) ?: ""
    fun formatHM(date: Date): String = hmFormatThreadLocal.get()?.format(date) ?: ""
}
