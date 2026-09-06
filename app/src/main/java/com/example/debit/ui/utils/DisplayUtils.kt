package com.example.debit.ui.utils

import android.app.Activity
import android.os.Build

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
