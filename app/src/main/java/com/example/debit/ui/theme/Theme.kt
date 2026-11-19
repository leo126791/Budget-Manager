package com.example.debit.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.example.debit.data.AppThemeColor

@Composable
fun DebitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    themeColor: AppThemeColor = AppThemeColor.EMERALD,
    customColorHex: String? = null,
    content: @Composable () -> Unit
) {
    val isCustom = !customColorHex.isNullOrBlank() || themeColor == AppThemeColor.CUSTOM
    val primaryColor = if (isCustom && !customColorHex.isNullOrBlank()) {
        try {
            Color(android.graphics.Color.parseColor(customColorHex))
        } catch (_: Exception) {
            Color(0xFF6366F1)
        }
    } else {
        when (themeColor) {
            AppThemeColor.INDIGO -> Color(0xFF4F46E5)
            AppThemeColor.EMERALD -> Color(0xFF1B4A31)
            AppThemeColor.TEAL -> Color(0xFF0284C7)
            AppThemeColor.ROSE -> Color(0xFFE11D48)
            AppThemeColor.AMBER -> Color(0xFFD97706)
            AppThemeColor.PURPLE -> Color(0xFF7C3AED)
            AppThemeColor.CYAN -> Color(0xFF0D9488)
            AppThemeColor.CORAL -> Color(0xFFEA580C)
            AppThemeColor.SLATE -> Color(0xFF475569)
            AppThemeColor.CUSTOM -> {
                if (!customColorHex.isNullOrBlank()) {
                    try {
                        Color(android.graphics.Color.parseColor(customColorHex))
                    } catch (_: Exception) {
                        Color(0xFF6366F1)
                    }
                } else {
                    Color(0xFF6366F1)
                }
            }
        }
    }

    val containerColor = if (isCustom) {
        Color(
            androidx.core.graphics.ColorUtils.compositeColors(
                primaryColor.copy(alpha = 0.12f).toArgb(),
                android.graphics.Color.WHITE
            )
        )
    } else {
        themeColor.containerColor
    }

    // Light Theme Colors custom tailored per theme
    val lightBackground = when (themeColor) {
        AppThemeColor.INDIGO -> Color(0xFFF8F9FA)
        AppThemeColor.EMERALD -> Color(0xFFF4F6F4)
        AppThemeColor.TEAL -> Color(0xFFF0F9FF)
        AppThemeColor.ROSE -> Color(0xFFFFF1F2)
        AppThemeColor.AMBER -> Color(0xFFFFFBEB)
        AppThemeColor.PURPLE -> Color(0xFFFAF5FF)
        AppThemeColor.CYAN -> Color(0xFFF0FDFA)
        AppThemeColor.CORAL -> Color(0xFFFFF7ED)
        AppThemeColor.SLATE -> Color(0xFFF8FAFC)
        AppThemeColor.CUSTOM -> Color(0xFFF9FAFB)
    }

    val lightSurface = Color(0xFFFFFFFF)

    val lightSurfaceHigh = if (isCustom) {
        Color(
            androidx.core.graphics.ColorUtils.compositeColors(
                primaryColor.copy(alpha = 0.08f).toArgb(),
                android.graphics.Color.WHITE
            )
        )
    } else {
        when (themeColor) {
            AppThemeColor.INDIGO -> StitchSurfaceContainerHigh
            AppThemeColor.EMERALD -> StitchSurfaceContainerHigh
            AppThemeColor.TEAL -> Color(0xFFBAE6FD)
            AppThemeColor.ROSE -> Color(0xFFFECDD3)
            AppThemeColor.AMBER -> Color(0xFFFDE68A)
            AppThemeColor.PURPLE -> Color(0xFFDDD6FE)
            AppThemeColor.CYAN -> Color(0xFF99F6E4)
            AppThemeColor.CORAL -> Color(0xFFFED7AA)
            AppThemeColor.SLATE -> Color(0xFFE2E8F0)
            AppThemeColor.CUSTOM -> StitchSurfaceContainerHigh
        }
    }

    val isPrimaryLight = androidx.core.graphics.ColorUtils.calculateLuminance(primaryColor.toArgb()) > 0.45
    val customOnPrimary = if (isPrimaryLight) Color(0xFF111413) else Color.White

    val lightScheme = lightColorScheme(
        primary = primaryColor,
        onPrimary = if (isCustom) customOnPrimary else Color.White,
        primaryContainer = containerColor,
        onPrimaryContainer = primaryColor,
        secondary = primaryColor,
        onSecondary = if (isCustom) customOnPrimary else Color.White,
        secondaryContainer = if (themeColor == AppThemeColor.EMERALD && !isCustom) {
            StitchSecondaryContainer
        } else {
            Color(
                androidx.core.graphics.ColorUtils.compositeColors(
                    primaryColor.copy(alpha = 0.14f).toArgb(),
                    android.graphics.Color.WHITE
                )
            )
        },
        onSecondaryContainer = if (themeColor == AppThemeColor.EMERALD && !isCustom) Color(0xFF003822) else primaryColor,
        tertiary = StitchAmberTertiary,
        background = lightBackground,
        surface = lightSurface,
        surfaceContainer = StitchSurfaceContainer,
        surfaceContainerHigh = lightSurfaceHigh,
        surfaceContainerLow = StitchSurfaceContainerLow,
        surfaceContainerLowest = StitchSurfaceContainerLowest,
        error = StitchCoralExpense,
        errorContainer = RedExpenseContainer,
        onErrorContainer = Color(0xFF991B1B)
    )

    val darkPrimary = if (isCustom) {
        primaryColor
    } else if (themeColor == AppThemeColor.EMERALD) {
        Color(0xFF6EE7B7)
    } else {
        val hsl = FloatArray(3)
        androidx.core.graphics.ColorUtils.colorToHSL(primaryColor.toArgb(), hsl)
        hsl[1] = (hsl[1] * 0.95f).coerceIn(0.5f, 1f)
        hsl[2] = 0.72f
        Color(androidx.core.graphics.ColorUtils.HSLToColor(hsl))
    }

    val darkPrimaryContainer = if (isCustom) {
        Color(
            androidx.core.graphics.ColorUtils.compositeColors(
                primaryColor.copy(alpha = 0.22f).toArgb(),
                android.graphics.Color.parseColor("#191C1B")
            )
        )
    } else if (themeColor == AppThemeColor.EMERALD) {
        Color(0xFF1B4A31)
    } else {
        val hsl = FloatArray(3)
        androidx.core.graphics.ColorUtils.colorToHSL(primaryColor.toArgb(), hsl)
        hsl[2] = 0.28f
        Color(androidx.core.graphics.ColorUtils.HSLToColor(hsl))
    }

    val darkScheme = darkColorScheme(
        primary = darkPrimary,
        onPrimary = if (isCustom) customOnPrimary else Color.Black,
        primaryContainer = darkPrimaryContainer,
        onPrimaryContainer = if (isCustom) (if (isPrimaryLight) primaryColor else Color.White) else darkPrimary,
        secondary = darkPrimary,
        onSecondary = if (isCustom) customOnPrimary else Color.Black,
        secondaryContainer = if (themeColor == AppThemeColor.EMERALD && !isCustom) Color(0xFF004F46) else darkPrimaryContainer,
        onSecondaryContainer = if (themeColor == AppThemeColor.EMERALD && !isCustom) Color(0xFF8CF5E4) else (if (isCustom) primaryColor else darkPrimary),
        tertiary = StitchAmberTertiary,
        background = Color(0xFF111413),
        onBackground = Color(0xFFE1E3E1),
        surface = Color(0xFF191C1B),
        onSurface = Color(0xFFE1E3E1),
        surfaceVariant = Color(0xFF404943),
        onSurfaceVariant = Color(0xFFC0C9C1),
        surfaceContainer = Color(0xFF1D211F),
        surfaceContainerLow = Color(0xFF191C1B),
        surfaceContainerLowest = Color(0xFF131715),
        surfaceContainerHigh = Color(0xFF282B29),
        outline = Color(0xFF8A938C),
        outlineVariant = Color(0xFF404943),
        error = StitchCoralExpense,
        errorContainer = Color(0xFF93000A),
        onErrorContainer = Color(0xFFFFDAD6)
    )

    val colorScheme = if (darkTheme) darkScheme else lightScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
