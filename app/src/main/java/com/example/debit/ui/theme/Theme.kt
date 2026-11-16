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
    themeColor: AppThemeColor = AppThemeColor.EMERALD,
    content: @Composable () -> Unit
) {
    val primaryColor = when (themeColor) {
        AppThemeColor.INDIGO -> Color(0xFF4F46E5)
        AppThemeColor.EMERALD -> Color(0xFF1B4A31)
        AppThemeColor.TEAL -> Color(0xFF0284C7)
        AppThemeColor.ROSE -> Color(0xFFE11D48)
        AppThemeColor.SLATE -> Color(0xFF475569)
    }

    val containerColor = themeColor.containerColor

    // Light Theme Colors custom tailored per theme
    val lightBackground = when (themeColor) {
        AppThemeColor.INDIGO -> Color(0xFFF8F9FA)
        AppThemeColor.EMERALD -> Color(0xFFF4F6F4)
        AppThemeColor.TEAL -> Color(0xFFF0F9FF)
        AppThemeColor.ROSE -> Color(0xFFFFF1F2)
        AppThemeColor.SLATE -> Color(0xFFF8FAFC)
    }

    val lightSurface = when (themeColor) {
        AppThemeColor.INDIGO -> Color(0xFFFFFFFF)
        AppThemeColor.EMERALD -> Color(0xFFFFFFFF)
        AppThemeColor.TEAL -> Color(0xFFFFFFFF)
        AppThemeColor.ROSE -> Color(0xFFFFFFFF)
        AppThemeColor.SLATE -> Color(0xFFFFFFFF)
    }

    val lightSurfaceHigh = when (themeColor) {
        AppThemeColor.INDIGO -> StitchSurfaceContainerHigh
        AppThemeColor.EMERALD -> StitchSurfaceContainerHigh
        AppThemeColor.TEAL -> Color(0xFFBAE6FD)
        AppThemeColor.ROSE -> Color(0xFFFECDD3)
        AppThemeColor.SLATE -> Color(0xFFE2E8F0)
    }

    val lightScheme = lightColorScheme(
        primary = primaryColor,
        onPrimary = Color.White,
        primaryContainer = containerColor,
        onPrimaryContainer = primaryColor,
        secondary = StitchSecondary,
        secondaryContainer = StitchSecondaryContainer,
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

    val darkScheme = darkColorScheme(
        primary = Color(0xFF6EE7B7),
        onPrimary = Color(0xFF003822),
        primaryContainer = Color(0xFF1B4A31),
        onPrimaryContainer = Color(0xFFA9DBB9),
        secondary = StitchSecondaryContainer,
        onSecondary = Color(0xFF003731),
        secondaryContainer = Color(0xFF004F46),
        onSecondaryContainer = Color(0xFF8CF5E4),
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
