package com.example.debit.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.app.TimePickerDialog
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.example.debit.R
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
    currentDarkMode: Boolean = false,
    currentLanguage: AppLanguage,
    currentBetaTestingEnabled: Boolean,
    currentGooglePayListenerEnabled: Boolean = false,
    currentDailyReminderEnabled: Boolean = false,
    reminderTimes: List<ReminderTime> = emptyList(),
    currentConfirmDeleteEnabled: Boolean = true,
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
    onConfirmDeleteChange: (Boolean) -> Unit = {},
    onDarkModeChange: (Boolean) -> Unit = {},
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
    var confirmDeleteState by remember { mutableStateOf(currentConfirmDeleteEnabled) }
    var incomeTrackingState by remember { mutableStateOf(currentIncomeTrackingEnabled) }
    var searchState by remember { mutableStateOf(currentSearchEnabled) }
    var subscriptionState by remember { mutableStateOf(currentSubscriptionEnabled) }
    var multiAccountState by remember { mutableStateOf(currentMultiAccountEnabled) }
    var modern3DUiState by remember { mutableStateOf(currentModern3DUiEnabled) }
    var dragDateReorderState by remember { mutableStateOf(currentDragDateReorderEnabled) }
    var autoBackupState by remember { mutableStateOf(currentAutoBackupEnabled) }
    var isDarkModeState by remember(currentDarkMode) { mutableStateOf(currentDarkMode) }

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
            isDarkMode = isDarkModeState,
            onDarkModeChange = { newDark ->
                isDarkModeState = newDark
                onDarkModeChange(newDark)
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
            confirmDeleteState = confirmDeleteState,
            onConfirmDeleteChange = { newConfirm ->
                confirmDeleteState = newConfirm
                onConfirmDeleteChange(newConfirm)
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
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFFEF3C7),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_piggy_bank),
                                        contentDescription = "Piggy",
                                        tint = Color(0xFFD97706),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = if (isZh) "設定 (Settings)" else "Settings",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
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
    isDarkMode: Boolean = false,
    onDarkModeChange: (Boolean) -> Unit = {},
    selectedLanguage: AppLanguage,
    onLanguageSelect: (AppLanguage) -> Unit,
    betaTestingState: Boolean,
    onBetaTestingChange: (Boolean) -> Unit,
    googlePayListenerState: Boolean,
    onGooglePayListenerChange: (Boolean) -> Unit,
    confirmDeleteState: Boolean,
    onConfirmDeleteChange: (Boolean) -> Unit,
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
    var showBudgetDialog by remember { mutableStateOf(false) }
    var tempBudgetInput by remember { mutableStateOf(budgetInput) }

    // Dialog for Target Budget
    if (showBudgetDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showBudgetDialog = false },
            title = {
                Text(
                    text = if (isZh) "設定本月目標小金庫" else "Set Target Budget",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                OutlinedTextField(
                    value = tempBudgetInput,
                    onValueChange = { tempBudgetInput = it },
                    label = { Text(if (isZh) "預算金額 ($)" else "Budget Amount ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onBudgetInputChange(tempBudgetInput)
                    showBudgetDialog = false
                }) {
                    Text(if (isZh) "儲存" else "Save", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBudgetDialog = false }) {
                    Text(if (isZh) "取消" else "Cancel")
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Group 1: 預算與金庫設定 (Budget & Vault)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = if (isZh) "預算與金庫設定" else "Budget & Vault Settings",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Item 1: 本月目標小金庫
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                tempBudgetInput = budgetInput
                                showBudgetDialog = true
                            }
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isDarkMode) Color(0xFF132B20) else Color(0xFFECFDF5),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_piggy_bank),
                                        contentDescription = null,
                                        tint = if (isDarkMode) Color(0xFF34D399) else Color(0xFF1B4A31),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = if (isZh) "本月目標小金庫" else "Target Budget Vault",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val displayAmount = budgetInput.toDoubleOrNull() ?: 0.0
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isDarkMode) Color(0xFF143525) else Color(0xFFECFDF5)
                            ) {
                                Text(
                                    text = "$${String.format(Locale.getDefault(), "%,.0f", displayAmount)} TWD",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDarkMode) Color(0xFFA7F3D0) else Color(0xFF1B4A31),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outlineVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    // Item 2: 動態生活費預算池
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isDarkMode) Color(0xFF13302C) else Color(0xFFCCFBF1),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = if (isDarkMode) Color(0xFF2DD4BF) else Color(0xFF0F766E),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = if (isZh) "動態生活費預算池" else "Dynamic Budget Pool",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Switch(
                            checked = true,
                            onCheckedChange = { /* Always enabled as core feature */ },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF1B4A31)
                            )
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    // Item 3: Google Pay 自動記帳
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isDarkMode) Color(0xFF1E2E42) else Color(0xFFDBEAFE),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalanceWallet,
                                        contentDescription = null,
                                        tint = if (isDarkMode) Color(0xFF60A5FA) else Color(0xFF2563EB),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = if (isZh) "Google Pay 自動記帳" else "Google Pay Auto Capture",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Switch(
                            checked = googlePayListenerState,
                            onCheckedChange = onGooglePayListenerChange,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = if (isDarkMode) Color(0xFF2E6B47) else Color(0xFF1B4A31)
                            )
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    // Item 4: 定時記帳提醒
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isDarkMode) Color(0xFF332A15) else Color(0xFFFEF3C7),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = null,
                                        tint = if (isDarkMode) Color(0xFFFBBF24) else Color(0xFFD97706),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = if (isZh) "定時記帳提醒" else "Daily Reminder",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Switch(
                            checked = dailyReminderState,
                            onCheckedChange = onDailyReminderMasterChange,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = if (isDarkMode) Color(0xFF2E6B47) else Color(0xFF1B4A31)
                            )
                        )
                    }

                    AnimatedVisibility(
                        visible = dailyReminderState,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 14.dp, end = 14.dp, bottom = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (reminderTimes.isEmpty()) {
                                Text(
                                    text = if (isZh) "尚未設定提醒時間" else "No reminder times set",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    reminderTimes.forEach { reminder ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(if (isDarkMode) Color(0xFF1E2421) else Color(0xFFF3F4F6))
                                                .padding(horizontal = 12.dp, vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "%02d:%02d".format(reminder.hour, reminder.minute),
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Switch(
                                                    checked = reminder.enabled,
                                                    onCheckedChange = { onToggleReminderTime(reminder, it) },
                                                    colors = SwitchDefaults.colors(
                                                        checkedThumbColor = Color.White,
                                                        checkedTrackColor = if (isDarkMode) Color(0xFF2E6B47) else Color(0xFF1B4A31)
                                                    )
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
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isZh) "新增提醒時間" else "Add Time",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
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
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isZh) "測試通知" else "Test",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Group 2: 個人化外觀與語言 (Appearance & Themes)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = if (isZh) "個人化外觀與語言" else "Appearance & Language",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Item 1: 深色外觀模式 (暗色模式開關)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isDarkMode) Color(0xFF282B29) else Color(0xFFF3F4F6),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.DarkMode,
                                        contentDescription = null,
                                        tint = if (isDarkMode) Color(0xFF6EE7B7) else Color(0xFF4B5563),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = if (isZh) "色彩外觀 (亮色 / 暗色)" else "Appearance (Light / Dark)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = { onDarkModeChange(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF1B4A31)
                            )
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    // Item 2: 系統語言
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val nextLang = if (selectedLanguage == AppLanguage.ZH) AppLanguage.EN else AppLanguage.ZH
                                onLanguageSelect(nextLang)
                            }
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isDarkMode) Color(0xFF1E3A5F) else Color(0xFFDBEAFE),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Language,
                                        contentDescription = null,
                                        tint = Color(0xFF2563EB),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = if (isZh) "系統語言" else "Language",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = if (selectedLanguage == AppLanguage.ZH) "繁體中文 (台灣)" else "English",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outlineVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // Group 3: BETA 實驗性功能 (Beta Lab)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isZh) "BETA 實驗性功能" else "BETA Features",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Surface(
                    shape = CircleShape,
                    color = if (isDarkMode) Color(0xFF142B20) else Color(0xFFD1FAE5)
                ) {
                    Text(
                        text = if (isZh) "實驗性實驗室" else "Experimental Lab",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkMode) Color(0xFF86EFAC) else Color(0xFF047857),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Master Switch: 加入 Beta 實驗性測試
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (isDarkMode) Color.Transparent else Color(0xFFF0FDF4))
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isDarkMode) Color(0xFF132B20) else Color(0xFFDCFCE7),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Science,
                                        contentDescription = null,
                                        tint = if (isDarkMode) Color(0xFF34D399) else Color(0xFF166534),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = if (isZh) "加入 Beta 實驗性測試" else "Join Beta Testing",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = if (isDarkMode) Color(0xFF1B3D2B) else Color(0xFF1B4A31)
                                    ) {
                                        Text(
                                            text = "BETA",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDarkMode) Color(0xFFA7F3D0) else Color.White,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Switch(
                            checked = betaTestingState,
                            onCheckedChange = onBetaTestingChange,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = if (isDarkMode) Color(0xFF2E6B47) else Color(0xFF1B4A31)
                            )
                        )
                    }

                    AnimatedVisibility(
                        visible = betaTestingState,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                            // Feature 1: 收入記帳與收支概覽
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isDarkMode) Color(0xFF132B20) else Color(0xFFECFDF5),
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.AccountBalance,
                                                contentDescription = null,
                                                tint = if (isDarkMode) Color(0xFF34D399) else Color(0xFF059669),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    Column {
                                        Text(
                                            text = if (isZh) "收入記帳與收支概覽" else "Income & Balance Overview",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Switch(
                                    checked = incomeTrackingState,
                                    onCheckedChange = onIncomeTrackingChange,
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = Color(0xFF1B4A31)
                                    )
                                )
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                            // Feature 2: 關鍵字搜尋
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isDarkMode) Color(0xFF1E2E42) else Color(0xFFDBEAFE),
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Search,
                                                contentDescription = null,
                                                tint = if (isDarkMode) Color(0xFF60A5FA) else Color(0xFF2563EB),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    Column {
                                        Text(
                                            text = if (isZh) "關鍵字搜尋" else "Keyword Search",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Switch(
                                    checked = searchState,
                                    onCheckedChange = onSearchChange,
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = Color(0xFF1B4A31)
                                    )
                                )
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                            // Feature 3: 支付方式分類
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isDarkMode) Color(0xFF13302C) else Color(0xFFCCFBF1),
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.CreditCard,
                                                contentDescription = null,
                                                tint = if (isDarkMode) Color(0xFF2DD4BF) else Color(0xFF0F766E),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    Column {
                                        Text(
                                            text = if (isZh) "支付方式分類" else "Payment Methods",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Switch(
                                    checked = multiAccountState,
                                    onCheckedChange = onMultiAccountChange,
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = Color(0xFF1B4A31)
                                    )
                                )
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                            // Feature 4: 長按移動紀錄 (不建議開啟)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isDarkMode) Color(0xFF291A38) else Color(0xFFF3E8FF),
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.TouchApp,
                                                contentDescription = null,
                                                tint = if (isDarkMode) Color(0xFFC084FC) else Color(0xFF9333EA),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    Column {
                                        Text(
                                            text = if (isZh) "長按移動紀錄 (不建議開啟)" else "Long Press to Move (Beta)",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Switch(
                                    checked = dragDateReorderState,
                                    onCheckedChange = onDragDateReorderChange,
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = Color(0xFF1B4A31)
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Group 4: 雲端同步與資料管理 (Sync & Data)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = if (isZh) "雲端同步與資料管理" else "Cloud Sync & Data Management",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Item 1: 自動雲端備份
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isDarkMode) Color(0xFF132B20) else Color(0xFFECFDF5),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Sync,
                                        contentDescription = null,
                                        tint = if (isDarkMode) Color(0xFF34D399) else Color(0xFF1B4A31),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = if (isZh) "自動雲端備份" else "Auto Cloud Backup",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Switch(
                            checked = autoBackupState,
                            onCheckedChange = onAutoBackupChange,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = if (isDarkMode) Color(0xFF2E6B47) else Color(0xFF1B4A31)
                            )
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    // Item 2: Google Drive 備份與還原
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isDarkMode) Color(0xFF1E3A5F) else Color(0xFFDBEAFE),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.CloudUpload,
                                        contentDescription = null,
                                        tint = if (isDarkMode) Color(0xFF60A5FA) else Color(0xFF2563EB),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = if (isZh) "Google Drive 備份與還原" else "Google Drive Sync",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = onBackupToGoogleDrive,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isDarkMode) Color(0xFF242926) else Color(0xFFF3F4F6)
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudUpload,
                                    contentDescription = null,
                                    tint = if (isDarkMode) Color(0xFF34D399) else Color(0xFF1B4A31),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isZh) "備份至 Drive" else "Backup to Drive",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (isDarkMode) Color(0xFFE2E8F0) else Color(0xFF1F2937)
                                )
                            }

                            OutlinedButton(
                                onClick = onRestoreFromGoogleDrive,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isDarkMode) Color(0xFF242926) else Color(0xFFF3F4F6)
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudDownload,
                                    contentDescription = null,
                                    tint = if (isDarkMode) Color(0xFF60A5FA) else Color(0xFF2563EB),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isZh) "還原雲端備份" else "Restore Cloud",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (isDarkMode) Color(0xFFE2E8F0) else Color(0xFF1F2937)
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    // Item 3: 匯出 CSV 財務報表
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isDarkMode) Color(0xFF173347) else Color(0xFFE0F2FE),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.FileDownload,
                                        contentDescription = null,
                                        tint = if (isDarkMode) Color(0xFF38BDF8) else Color(0xFF0284C7),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = if (isZh) "匯出 CSV 財務報表" else "Export CSV Report",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF191C1D),
                            modifier = Modifier.clickable { onExportReport() }
                        ) {
                            Text(
                                text = if (isZh) "匯出" else "Export",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Version & Footer Info
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Budget Manager v1.1.1",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = if (isZh) "版本 1.1.1・記帳與預算管家" else "Version 1.1.1 • Budget Manager",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}
