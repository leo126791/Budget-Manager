package com.example.debit.ui.dialogs

import androidx.compose.runtime.Composable
import com.example.debit.data.AppLanguage
import com.example.debit.data.AppThemeColor
import com.example.debit.ui.screens.SettingsScreen

@Composable
fun SettingsDialog(
    initialTotalBudget: Double,
    currentThemeColor: AppThemeColor,
    currentLanguage: AppLanguage,
    currentBetaTestingEnabled: Boolean,
    currentIncomeTrackingEnabled: Boolean = true,
    currentSearchEnabled: Boolean = true,
    currentSubscriptionEnabled: Boolean = true,
    currentMultiAccountEnabled: Boolean = true,
    currentModern3DUiEnabled: Boolean = true,
    currentDragDateReorderEnabled: Boolean = true,
    currentAutoBackupEnabled: Boolean = true,
    lastAutoBackupTime: Long,
    onSaveSettings: (
        budgetLimit: Double,
        themeColor: AppThemeColor,
        language: AppLanguage,
        betaTestingEnabled: Boolean,
        incomeTrackingEnabled: Boolean,
        searchEnabled: Boolean,
        subscriptionEnabled: Boolean,
        multiAccountEnabled: Boolean,
        modern3DUiEnabled: Boolean,
        dragDateReorderEnabled: Boolean,
        autoBackupEnabled: Boolean
    ) -> Unit,
    onExportReport: () -> Unit,
    onBackupToGoogleDrive: () -> Unit,
    onRestoreFromGoogleDrive: () -> Unit,
    onDismissRequest: () -> Unit
) {
    SettingsScreen(
        initialTotalBudget = initialTotalBudget,
        currentThemeColor = currentThemeColor,
        currentLanguage = currentLanguage,
        currentBetaTestingEnabled = currentBetaTestingEnabled,
        currentIncomeTrackingEnabled = currentIncomeTrackingEnabled,
        currentSearchEnabled = currentSearchEnabled,
        currentSubscriptionEnabled = currentSubscriptionEnabled,
        currentMultiAccountEnabled = currentMultiAccountEnabled,
        currentModern3DUiEnabled = currentModern3DUiEnabled,
        currentDragDateReorderEnabled = currentDragDateReorderEnabled,
        currentAutoBackupEnabled = currentAutoBackupEnabled,
        lastAutoBackupTime = lastAutoBackupTime,
        onSaveSettings = onSaveSettings,
        onExportReport = onExportReport,
        onBackupToGoogleDrive = onBackupToGoogleDrive,
        onRestoreFromGoogleDrive = onRestoreFromGoogleDrive,
        onDismissRequest = onDismissRequest
    )
}
