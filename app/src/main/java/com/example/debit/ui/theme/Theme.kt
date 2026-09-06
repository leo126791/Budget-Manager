package com.example.debit.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.debit.data.AppThemeColor

@Composable
fun DebitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    themeColor: AppThemeColor = AppThemeColor.INDIGO,
    content: @Composable () -> Unit
) {
    val primaryColor = themeColor.primaryColor
    val containerColor = themeColor.containerColor

    val lightScheme = lightColorScheme(
        primary = primaryColor,
        onPrimary = Color.White,
        primaryContainer = containerColor,
        onPrimaryContainer = primaryColor,
        secondary = TealSecondary,
        tertiary = AmberAccent,
        background = SurfaceLight,
        surface = CardBackgroundLight,
        surfaceContainer = SurfaceContainerLight,
        surfaceContainerHigh = Color(0xFFEDF2F7),
        surfaceContainerLow = Color(0xFFFFFFFF),
        error = RedExpense,
        errorContainer = RedExpenseContainer,
        onErrorContainer = Color(0xFF991B1B)
    )

    val darkScheme = darkColorScheme(
        primary = primaryColor,
        onPrimary = Color.Black,
        primaryContainer = primaryColor.copy(alpha = 0.4f),
        onPrimaryContainer = Color.White,
        secondary = TealSecondary,
        tertiary = AmberAccent,
        background = DarkBackground,
        surface = DarkSurfaceContainer,
        error = RedExpense
    )

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> darkScheme
        else -> lightScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
