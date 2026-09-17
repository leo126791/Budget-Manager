package com.example.debit.ui.dialogs

import androidx.compose.runtime.Composable
import com.example.debit.data.AppLanguage
import com.example.debit.data.AppThemeColor
import com.example.debit.data.ReminderTime
import com.example.debit.ui.screens.SettingsScreen

@Composable
fun SettingsDialog(
    initialTotalBudget: Double,
    currentThemeColor: AppThemeColor,
    currentLanguage: AppLanguage,
    currentBetaTestingEnabled: Boolean,
    currentGooglePayListenerEnabled: Boolean = false,
    currentDailyReminderEnabled: Boolean = false,
    reminderTimes: List<ReminderTime> = emptyList(),
    currentIncomeTrackingEnabled: Boolean = true,
    currentSearchEnabled: Boolean = true,
    currentSubscriptionEnabled: Boolean = true,
    currentMultiAccountEnabled: Boolean = true,
    currentModern3DUiEnabled: Boolean = true,
    currentDragDateReorderEnabled: Boolean = true,
    currentAutoBackupEnabled: Boolean = true,
    lastAutoBackupTime: Long,
    onDailyReminderMasterChange: (Boolean) -> Unit = {},
    onAddReminderTime: (Int, Int) -> Unit = { _, _ -> },
    onDeleteReminderTime: (ReminderTime) -> Unit = {},
    onToggleReminderTime: (ReminderTime, Boolean) -> Unit = { _, _ -> },
    onSaveSettings: (
        budgetLimit: Double,
        themeColor: AppThemeColor,
        language: AppLanguage,
        betaTestingEnabled: Boolean,
        googlePayListenerEnabled: Boolean,
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
        currentGooglePayListenerEnabled = currentGooglePayListenerEnabled,
        currentDailyReminderEnabled = currentDailyReminderEnabled,
        reminderTimes = reminderTimes,
        currentIncomeTrackingEnabled = currentIncomeTrackingEnabled,
        currentSearchEnabled = currentSearchEnabled,
        currentSubscriptionEnabled = currentSubscriptionEnabled,
        currentMultiAccountEnabled = currentMultiAccountEnabled,
        currentModern3DUiEnabled = currentModern3DUiEnabled,
        currentDragDateReorderEnabled = currentDragDateReorderEnabled,
        currentAutoBackupEnabled = currentAutoBackupEnabled,
        lastAutoBackupTime = lastAutoBackupTime,
        onDailyReminderMasterChange = onDailyReminderMasterChange,
        onAddReminderTime = onAddReminderTime,
        onDeleteReminderTime = onDeleteReminderTime,
        onToggleReminderTime = onToggleReminderTime,
        onSaveSettings = onSaveSettings,
        onExportReport = onExportReport,
        onBackupToGoogleDrive = onBackupToGoogleDrive,
        onRestoreFromGoogleDrive = onRestoreFromGoogleDrive,
        onDismissRequest = onDismissRequest
    )
}
