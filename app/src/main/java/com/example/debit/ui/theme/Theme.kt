package com.example.debit.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.debit.data.AppThemeColor

@Composable
fun DebitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    themeColor: AppThemeColor = AppThemeColor.INDIGO,
    content: @Composable () -> Unit
) {
    val primaryColor = when (themeColor) {
        AppThemeColor.INDIGO -> Color(0xFF6366F1)
        AppThemeColor.EMERALD -> Color(0xFF10B981)
        AppThemeColor.TEAL -> Color(0xFF06B6D4)
        AppThemeColor.ROSE -> Color(0xFFF43F5E)
        AppThemeColor.SLATE -> Color(0xFF64748B)
    }

    val containerColor = themeColor.containerColor

    // Dark Theme Colors custom tailored per theme
    val darkBackground = when (themeColor) {
        AppThemeColor.INDIGO -> Color(0xFF0D0F1D)
        AppThemeColor.EMERALD -> Color(0xFF041710)
        AppThemeColor.TEAL -> Color(0xFF051522)
        AppThemeColor.ROSE -> Color(0xFF1A060D)
        AppThemeColor.SLATE -> Color(0xFF0F172A)
    }

    val darkSurface = when (themeColor) {
        AppThemeColor.INDIGO -> Color(0xFF1A1D33)
        AppThemeColor.EMERALD -> Color(0xFF0B2B1F)
        AppThemeColor.TEAL -> Color(0xFF0D2638)
        AppThemeColor.ROSE -> Color(0xFF2E0F1A)
        AppThemeColor.SLATE -> Color(0xFF1E293B)
    }

    val darkSurfaceLow = when (themeColor) {
        AppThemeColor.INDIGO -> Color(0xFF15172A)
        AppThemeColor.EMERALD -> Color(0xFF072118)
        AppThemeColor.TEAL -> Color(0xFF081E2E)
        AppThemeColor.ROSE -> Color(0xFF240A14)
        AppThemeColor.SLATE -> Color(0xFF172033)
    }

    val darkSurfaceHigh = when (themeColor) {
        AppThemeColor.INDIGO -> Color(0xFF242847)
        AppThemeColor.EMERALD -> Color(0xFF123C2C)
        AppThemeColor.TEAL -> Color(0xFF14354C)
        AppThemeColor.ROSE -> Color(0xFF3D1625)
        AppThemeColor.SLATE -> Color(0xFF27354A)
    }

    // Light Theme Colors
    val lightBackground = when (themeColor) {
        AppThemeColor.INDIGO -> Color(0xFFF5F3FF)
        AppThemeColor.EMERALD -> Color(0xFFECFDF5)
        AppThemeColor.TEAL -> Color(0xFFF0F9FF)
        AppThemeColor.ROSE -> Color(0xFFFFF1F2)
        AppThemeColor.SLATE -> Color(0xFFF8FAFC)
    }

    val lightSurface = when (themeColor) {
        AppThemeColor.INDIGO -> Color(0xFFEDE9FE)
        AppThemeColor.EMERALD -> Color(0xFFD1FAE5)
        AppThemeColor.TEAL -> Color(0xFFE0F2FE)
        AppThemeColor.ROSE -> Color(0xFFFFE4E6)
        AppThemeColor.SLATE -> Color(0xFFF1F5F9)
    }

    val lightSurfaceHigh = when (themeColor) {
        AppThemeColor.INDIGO -> Color(0xFFDDD6FE)
        AppThemeColor.EMERALD -> Color(0xFFA7F3D0)
        AppThemeColor.TEAL -> Color(0xFFBAE6FD)
        AppThemeColor.ROSE -> Color(0xFFFECDD3)
        AppThemeColor.SLATE -> Color(0xFFE2E8F0)
    }

    val lightScheme = lightColorScheme(
        primary = primaryColor,
        onPrimary = Color.White,
        primaryContainer = containerColor,
        onPrimaryContainer = primaryColor,
        secondary = TealSecondary,
        tertiary = AmberAccent,
        background = lightBackground,
        surface = lightSurface,
        surfaceContainer = lightSurface,
        surfaceContainerHigh = lightSurfaceHigh,
        surfaceContainerLow = Color(0xFFFFFFFF),
        error = RedExpense,
        errorContainer = RedExpenseContainer,
        onErrorContainer = Color(0xFF991B1B)
    )

    val darkScheme = darkColorScheme(
        primary = primaryColor,
        onPrimary = Color.White,
        primaryContainer = primaryColor.copy(alpha = 0.35f),
        onPrimaryContainer = Color.White,
        secondary = TealSecondary,
        tertiary = AmberAccent,
        background = darkBackground,
        surface = darkSurface,
        surfaceContainer = darkSurface,
        surfaceContainerLow = darkSurfaceLow,
        surfaceContainerHigh = darkSurfaceHigh,
        error = RedExpense
    )

    val colorScheme = if (darkTheme) darkScheme else lightScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
