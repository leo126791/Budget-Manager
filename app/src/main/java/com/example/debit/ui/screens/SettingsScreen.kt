package com.example.debit.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.app.TimePickerDialog
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import com.example.debit.data.ReminderTime
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import com.example.debit.service.PaymentNotificationListenerService
import com.example.debit.data.AppLanguage
import com.example.debit.data.AppThemeColor
import com.example.debit.data.SettingsPreferences
import com.example.debit.receiver.DailyReminderReceiver
import com.example.debit.ui.utils.AppStrings
import com.example.debit.ui.utils.bouncyClickable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
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
    isEmbedded: Boolean = false,
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
    onReplayOnboarding: () -> Unit = {},
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current

    if (!isEmbedded) {
        BackHandler {
            onDismissRequest()
        }
    }

    var budgetInput by remember {
        mutableStateOf(if (initialTotalBudget > 0) initialTotalBudget.toInt().toString() else "")
    }
    var selectedColor by remember { mutableStateOf(currentThemeColor) }
    var selectedLanguage by remember { mutableStateOf(currentLanguage) }
    var betaTestingState by remember { mutableStateOf(currentBetaTestingEnabled) }
    var googlePayListenerState by remember { mutableStateOf(currentGooglePayListenerEnabled) }
    var dailyReminderState by remember { mutableStateOf(currentDailyReminderEnabled) }
    var incomeTrackingState by remember { mutableStateOf(currentIncomeTrackingEnabled) }
    var searchState by remember { mutableStateOf(currentSearchEnabled) }
    var subscriptionState by remember { mutableStateOf(currentSubscriptionEnabled) }
    var multiAccountState by remember { mutableStateOf(currentMultiAccountEnabled) }
    var modern3DUiState by remember { mutableStateOf(currentModern3DUiEnabled) }
    var dragDateReorderState by remember { mutableStateOf(currentDragDateReorderEnabled) }
    var autoBackupState by remember { mutableStateOf(currentAutoBackupEnabled) }

    val isZh = selectedLanguage == AppLanguage.ZH

    val postNotificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            DailyReminderReceiver.showReminderNotification(
                context,
                SettingsPreferences(context)
            )
            Toast.makeText(
                context,
                if (isZh) "已發送測試通知！" else "Test Notification Sent!",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(
                context,
                if (isZh) "請開啟通知權限以接收記帳提醒！" else "Notification permission required!",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun triggerAutoSave(
        budget: String = budgetInput,
        color: AppThemeColor = selectedColor,
        lang: AppLanguage = selectedLanguage,
        beta: Boolean = betaTestingState,
        gPay: Boolean = googlePayListenerState,
        income: Boolean = incomeTrackingState,
        search: Boolean = searchState,
        sub: Boolean = subscriptionState,
        multiAcc: Boolean = multiAccountState,
        m3d: Boolean = modern3DUiState,
        dragDate: Boolean = dragDateReorderState,
        autoBackup: Boolean = autoBackupState
    ) {
        val limit = budget.toDoubleOrNull() ?: 0.0
        onSaveSettings(
            limit,
            color,
            lang,
            beta,
            gPay,
            income,
            search,
            sub,
            multiAcc,
            m3d,
            dragDate,
            autoBackup
        )
    }

    val bodyContent: @Composable (Modifier) -> Unit = { modifier ->
        SettingsBodyList(
            modifier = modifier,
            budgetInput = budgetInput,
            onBudgetInputChange = { newInput ->
                budgetInput = newInput
                triggerAutoSave(budget = newInput)
            },
            selectedColor = selectedColor,
            onColorSelect = { newColor ->
                selectedColor = newColor
                triggerAutoSave(color = newColor)
            },
            selectedLanguage = selectedLanguage,
            onLanguageSelect = { newLang ->
                selectedLanguage = newLang
                triggerAutoSave(lang = newLang)
            },
            betaTestingState = betaTestingState,
            onBetaTestingChange = { newBeta ->
                betaTestingState = newBeta
                triggerAutoSave(
                    beta = newBeta
                )
            },
            googlePayListenerState = googlePayListenerState,
            onGooglePayListenerChange = { newGPay ->
                googlePayListenerState = newGPay
                triggerAutoSave(gPay = newGPay)
                if (newGPay && !PaymentNotificationListenerService.isPermissionGranted(context)) {
                    val promptMsg = if (isZh) "請在系統設定中開啟「通知存取」權限，以自動捕捉 Google Pay 消費！" else "Please enable Notification Access permission to auto-capture Google Pay!"
                    Toast.makeText(context, promptMsg, Toast.LENGTH_LONG).show()
                    context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                }
            },
            dailyReminderState = dailyReminderState,
            reminderTimes = reminderTimes,
            onDailyReminderMasterChange = { enabled ->
                dailyReminderState = enabled
                onDailyReminderMasterChange(enabled)
                if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    postNotificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            },
            onAddReminderTime = onAddReminderTime,
            onDeleteReminderTime = onDeleteReminderTime,
            onToggleReminderTime = onToggleReminderTime,
            onTestNotification = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    postNotificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    DailyReminderReceiver.showReminderNotification(
                        context,
                        SettingsPreferences(context)
                    )
                    Toast.makeText(
                        context,
                        if (isZh) "已發送測試通知！" else "Test Notification Sent!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            incomeTrackingState = incomeTrackingState,
            onIncomeTrackingChange = { newIncome ->
                incomeTrackingState = newIncome
                triggerAutoSave(income = newIncome)
            },
            searchState = searchState,
            onSearchChange = { newSearch ->
                searchState = newSearch
                triggerAutoSave(search = newSearch)
            },
            multiAccountState = multiAccountState,
            onMultiAccountChange = { newMultiAcc ->
                multiAccountState = newMultiAcc
                triggerAutoSave(multiAcc = newMultiAcc)
            },
            modern3DUiState = modern3DUiState,
            onModern3DUiChange = { new3D ->
                modern3DUiState = new3D
                triggerAutoSave(m3d = new3D)
            },
            dragDateReorderState = dragDateReorderState,
            onDragDateReorderChange = { newDrag ->
                dragDateReorderState = newDrag
                triggerAutoSave(dragDate = newDrag)
            },
            autoBackupState = autoBackupState,
            onAutoBackupChange = { newAutoBackup ->
                autoBackupState = newAutoBackup
                triggerAutoSave(autoBackup = newAutoBackup)
            },
            lastAutoBackupTime = lastAutoBackupTime,
            onBackupToGoogleDrive = onBackupToGoogleDrive,
            onRestoreFromGoogleDrive = onRestoreFromGoogleDrive,
            onExportReport = onExportReport,
            onReplayOnboarding = onReplayOnboarding
        )
    }

    if (isEmbedded) {
        bodyContent(Modifier)
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = AppStrings.get("settings", selectedLanguage),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismissRequest) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = if (isZh) "返回" else "Back"
                            )
                        }
                    },
                    actions = {
                        TextButton(onClick = onDismissRequest) {
                            Text(
                                text = if (isZh) "完成" else "Done",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        ) { innerPadding ->
            bodyContent(Modifier.padding(innerPadding))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SettingsBodyList(
    modifier: Modifier = Modifier,
    budgetInput: String,
    onBudgetInputChange: (String) -> Unit,
    selectedColor: AppThemeColor,
    onColorSelect: (AppThemeColor) -> Unit,
    selectedLanguage: AppLanguage,
    onLanguageSelect: (AppLanguage) -> Unit,
    betaTestingState: Boolean,
    onBetaTestingChange: (Boolean) -> Unit,
    googlePayListenerState: Boolean,
    onGooglePayListenerChange: (Boolean) -> Unit,
    dailyReminderState: Boolean,
    reminderTimes: List<ReminderTime>,
    onDailyReminderMasterChange: (Boolean) -> Unit,
    onAddReminderTime: (Int, Int) -> Unit,
    onDeleteReminderTime: (ReminderTime) -> Unit,
    onToggleReminderTime: (ReminderTime, Boolean) -> Unit,
    onTestNotification: () -> Unit,
    incomeTrackingState: Boolean,
    onIncomeTrackingChange: (Boolean) -> Unit,
    searchState: Boolean,
    onSearchChange: (Boolean) -> Unit,
    multiAccountState: Boolean,
    onMultiAccountChange: (Boolean) -> Unit,
    modern3DUiState: Boolean,
    onModern3DUiChange: (Boolean) -> Unit,
    dragDateReorderState: Boolean,
    onDragDateReorderChange: (Boolean) -> Unit,
    autoBackupState: Boolean,
    onAutoBackupChange: (Boolean) -> Unit,
    lastAutoBackupTime: Long,
    onBackupToGoogleDrive: () -> Unit,
    onRestoreFromGoogleDrive: () -> Unit,
    onExportReport: () -> Unit,
    onReplayOnboarding: () -> Unit
) {
    val isZh = selectedLanguage == AppLanguage.ZH

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Monthly Budget Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = AppStrings.get("set_budget", selectedLanguage),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                OutlinedTextField(
                    value = budgetInput,
                    onValueChange = onBudgetInputChange,
                    label = { Text(AppStrings.get("monthly_budget", selectedLanguage)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // 2. Appearance & Theme Color Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = AppStrings.get("theme_color", selectedLanguage),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AppThemeColor.entries.forEach { themeColor ->
                        val isColorSelected = themeColor == selectedColor
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isColorSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier
                                .bouncyClickable { onColorSelect(themeColor) }
                                .border(
                                    width = if (isColorSelected) 2.dp else 1.dp,
                                    color = if (isColorSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                    shape = RoundedCornerShape(12.dp)
                                )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(themeColor.primaryColor)
                                )
                                Text(
                                    text = if (isZh) themeColor.displayNameZh else themeColor.displayNameEn,
                                    fontSize = 13.sp,
                                    fontWeight = if (isColorSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Language Selection Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Translate,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = AppStrings.get("app_language", selectedLanguage),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    AppLanguage.entries.forEachIndexed { index, lang ->
                        SegmentedButton(
                            selected = selectedLanguage == lang,
                            onClick = { onLanguageSelect(lang) },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = AppLanguage.entries.size)
                        ) {
                            Text(lang.displayName)
                        }
                    }
                }
            }
        }

        // 4. Daily Reminder Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = if (isZh) "每日記帳提醒" else "Daily Expense Reminder",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isZh) "設定多個提醒時間，定時發送通知提醒記錄開銷" else "Set multiple reminder times to log your expenses",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = dailyReminderState,
                        onCheckedChange = { enabled ->
                            onDailyReminderMasterChange(enabled)
                        }
                    )
                }

                if (dailyReminderState) {
                    HorizontalDivider()

                    if (reminderTimes.isEmpty()) {
                        Text(
                            text = if (isZh) "尚未設定提醒時間" else "No reminder times set",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            reminderTimes.forEach { reminder ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "%02d:%02d".format(reminder.hour, reminder.minute),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Switch(
                                            checked = reminder.enabled,
                                            onCheckedChange = { onToggleReminderTime(reminder, it) }
                                        )
                                        IconButton(
                                            onClick = { onDeleteReminderTime(reminder) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = if (isZh) "刪除" else "Delete",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    val currentCtx = LocalContext.current
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                TimePickerDialog(
                                    currentCtx,
                                    { _, hourOfDay, minute ->
                                        onAddReminderTime(hourOfDay, minute)
                                    },
                                    21,
                                    0,
                                    true
                                ).show()
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isZh) "新增提醒時間" else "Add Time",
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = onTestNotification,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isZh) "測試發送通知" else "Test Notification",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 5. Google Pay Auto-Tracking Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = if (isZh) "Google Pay 消費自動記帳" else "Google Pay Auto-Tracking",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isZh) "讀取 Google Pay 扣款通知並自動寫入記帳" else "Auto-capture Google Pay notifications",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = googlePayListenerState,
                        onCheckedChange = onGooglePayListenerChange
                    )
                }

                if (googlePayListenerState) {
                    HorizontalDivider()
                    val currentCtx = LocalContext.current
                    val isGranted = PaymentNotificationListenerService.isPermissionGranted(currentCtx)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isGranted) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                                currentCtx.startActivity(intent)
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (isGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                            Column {
                                Text(
                                    text = if (isGranted) {
                                        if (isZh) "通知存取權限：已啟用 (運作中)" else "Notification Access: Granted"
                                    } else {
                                        if (isZh) "尚未開啟通知存取權限 (點此開啓權限)" else "Notification Access Required (Tap to Grant)"
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isGranted) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                                )
                                if (!isGranted) {
                                    Text(
                                        text = if (isZh) "點擊此處前往系統設定，允許 Budget Manager 讀取通知" else "Tap to enable Notification Access in System Settings",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 6. Beta Testing & Experimental Features Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isZh) "加入 Beta 實驗性功能測試" else "Join Beta Testing",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isZh) "搶先體驗未正式發佈的實驗性功能" else "Get early access to experimental features",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = betaTestingState,
                        onCheckedChange = onBetaTestingChange
                    )
                }

                if (betaTestingState) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {

                            // 1. Income Tracking
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isZh) "收入記帳與收支概覽" else "Income & Net Savings",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (isZh) "記錄薪水/獎金與月度收支平衡" else "Track income & monthly net savings",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Switch(
                                    checked = incomeTrackingState,
                                    onCheckedChange = onIncomeTrackingChange
                                )
                            }

                            HorizontalDivider()

                            // 3. Search Feature
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isZh) "關鍵字搜尋" else "Keyword Search",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (isZh) "搜尋消費備註、類別" else "Search notes & categories",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Switch(
                                    checked = searchState,
                                    onCheckedChange = onSearchChange
                                )
                            }

                            HorizontalDivider()

                            // 3. Multi Account
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isZh) "支付方式" else "Payment Methods",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (isZh) "區分現金、信用卡、LINE Pay等" else "Tag Cash, Credit Card, Line Pay",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Switch(
                                    checked = multiAccountState,
                                    onCheckedChange = onMultiAccountChange
                                )
                            }

                            HorizontalDivider()

                            // 7. Modern 3D Hand-Drawn UI
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isZh) "測試版 UI" else "beta UI",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (isZh) "目前空空" else "none",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Switch(
                                    checked = modern3DUiState,
                                    onCheckedChange = onModern3DUiChange
                                )
                            }

                            HorizontalDivider()

                            // 8. Drag Date Reorder
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isZh) "長按移動紀錄(不建議開啟)" else "Move Date on Long Press(not recommended)",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (isZh) "長按記帳紀錄快速將開銷移至其他日期" else "Long press a record to shift its date",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Switch(
                                    checked = dragDateReorderState,
                                    onCheckedChange = onDragDateReorderChange
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. Data Backup & Cloud Sync Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Auto Backup Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isZh) "自動備份" else "Auto-Backup",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        val backupTimeFormatted = if (lastAutoBackupTime > 0) {
                            val dateStr = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date(lastAutoBackupTime))
                            if (isZh) "上次自動備份：$dateStr" else "Last auto backup: $dateStr"
                        } else {
                            if (isZh) "每次記帳變更時自動建立資料備份快照" else "Auto create backup on every change"
                        }
                        Text(
                            text = backupTimeFormatted,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = autoBackupState,
                        onCheckedChange = onAutoBackupChange
                    )
                }

                HorizontalDivider()

                // Google Drive Backup & Restore
                Text(
                    text = if (isZh) "Google Drive 雲端備份與還原" else "Google Drive Cloud Backup",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isZh) "安全地將您的記帳紀錄與預算設定備份至 Google Drive 雲端或進行備份檔還原。" else "Safely backup or restore your records with Google Drive.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onBackupToGoogleDrive,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isZh) "備份至 Drive" else "Backup Drive",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    OutlinedButton(
                        onClick = onRestoreFromGoogleDrive,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isZh) "還原雲端備份" else "Restore Drive",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                HorizontalDivider()

                // Export Financial Report
                Text(
                    text = if (isZh) "財務報表匯出" else "Export Financial Report",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isZh) "將所有記帳紀錄匯出為 CSV 報表檔，方便於 Excel 或 Numbers 中開啟與備份。" else "Export all transaction records to a CSV file for Excel/Numbers backup.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedButton(
                    onClick = onExportReport,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isZh) "匯出 CSV 財務報表" else "Export CSV Report",
                        fontWeight = FontWeight.Bold
                    )
                }

                HorizontalDivider()

                // Replay Onboarding Animation Button
                OutlinedButton(
                    onClick = onReplayOnboarding,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isZh) "體驗/重看啟動引導動畫" else "Replay Startup Onboarding",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 6. App Version Footer Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Budget Manager Beta v1.0.4",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = if (isZh) "內部測試版 • 預算管家" else "Tester Edition • Internal Build",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
