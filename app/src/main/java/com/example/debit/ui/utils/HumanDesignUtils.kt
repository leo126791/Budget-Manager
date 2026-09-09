package com.example.debit.ui.utils

import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.example.debit.data.AppLanguage
import java.util.Calendar

/**
 * Custom Organic Shapes that break stiff symmetrical grids
 */
val OrganicShapeLarge = RoundedCornerShape(
    topStart = 28.dp,
    topEnd = 12.dp,
    bottomEnd = 24.dp,
    bottomStart = 10.dp
)

val OrganicShapeMedium = RoundedCornerShape(
    topStart = 20.dp,
    topEnd = 8.dp,
    bottomEnd = 18.dp,
    bottomStart = 12.dp
)

val OrganicShapeChip = RoundedCornerShape(
    topStart = 16.dp,
    topEnd = 6.dp,
    bottomEnd = 14.dp,
    bottomStart = 8.dp
)

/**
 * Clean tactile clickable modifier with haptic vibration feedback
 */
fun Modifier.bouncyClickable(
    enabled: Boolean = true,
    hapticType: HapticFeedbackType? = HapticFeedbackType.LongPress,
    onClick: () -> Unit
): Modifier = composed {
    val haptic = LocalHapticFeedback.current
    this.clickable(
        enabled = enabled,
        onClick = {
            hapticType?.let { haptic.performHapticFeedback(it) }
            onClick()
        }
    )
}

/**
 * Returns a warm time-of-day greeting
 */
fun getWarmTimeGreeting(language: AppLanguage): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val isZh = language == AppLanguage.ZH

    return when (hour) {
        in 5..11 -> if (isZh) "早安！今天早餐吃了什麼好吃的？☀️" else "Good morning! Ready for a fresh day? ☀️"
        in 12..17 -> if (isZh) "午安！今天中午的花費上記了嗎？🍱" else "Good afternoon! How's your day going? 🍱"
        in 18..22 -> if (isZh) "晚安！來算算今天一共花了多少小錢錢 🌙" else "Good evening! Let's check today's budget 🌙"
        else -> if (isZh) "深夜囉！記得早點休息，明天繼續加油 ⭐️" else "Late night! Get some rest ⭐️"
    }
}
