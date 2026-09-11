package com.example.debit.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.debit.data.AppLanguage
import com.example.debit.data.AppThemeColor
import com.example.debit.ui.utils.AppStrings
import com.example.debit.ui.utils.bouncyClickable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsDialog(
    initialTotalBudget: Double,
    currentThemeColor: AppThemeColor,
    currentLanguage: AppLanguage,
    currentBetaTestingEnabled: Boolean,
    currentLivingExpensePoolEnabled: Boolean = false,
    currentIncomeTrackingEnabled: Boolean = true,
    currentSearchEnabled: Boolean = true,
    currentSubscriptionEnabled: Boolean = true,
    currentMultiAccountEnabled: Boolean = true,
    currentSavingsGoalsEnabled: Boolean = true,
    currentModern3DUiEnabled: Boolean = true,
    currentDragDateReorderEnabled: Boolean = true,
    currentAutoBackupEnabled: Boolean = true,
    lastAutoBackupTime: Long,
    onSaveSettings: (
        budgetLimit: Double,
        themeColor: AppThemeColor,
        language: AppLanguage,
        betaTestingEnabled: Boolean,
        livingExpensePoolEnabled: Boolean,
        incomeTrackingEnabled: Boolean,
        searchEnabled: Boolean,
        subscriptionEnabled: Boolean,
        multiAccountEnabled: Boolean,
        savingsGoalsEnabled: Boolean,
        modern3DUiEnabled: Boolean,
        dragDateReorderEnabled: Boolean,
        autoBackupEnabled: Boolean
    ) -> Unit,
    onExportReport: () -> Unit,
    onBackupToGoogleDrive: () -> Unit,
    onRestoreFromGoogleDrive: () -> Unit,
    onDismissRequest: () -> Unit
) {
    var budgetInput by remember {
        mutableStateOf(if (initialTotalBudget > 0) initialTotalBudget.toInt().toString() else "")
    }
    var selectedColor by remember { mutableStateOf(currentThemeColor) }
    var selectedLanguage by remember { mutableStateOf(currentLanguage) }
    var betaTestingState by remember { mutableStateOf(currentBetaTestingEnabled) }
    var livingExpensePoolState by remember { mutableStateOf(currentLivingExpensePoolEnabled) }
    var incomeTrackingState by remember { mutableStateOf(currentIncomeTrackingEnabled) }
    var searchState by remember { mutableStateOf(currentSearchEnabled) }
    var subscriptionState by remember { mutableStateOf(currentSubscriptionEnabled) }
    var multiAccountState by remember { mutableStateOf(currentMultiAccountEnabled) }
    var savingsGoalsState by remember { mutableStateOf(currentSavingsGoalsEnabled) }
    var modern3DUiState by remember { mutableStateOf(currentModern3DUiEnabled) }
    var dragDateReorderState by remember { mutableStateOf(currentDragDateReorderEnabled) }
    var autoBackupState by remember { mutableStateOf(currentAutoBackupEnabled) }

    val isZh = selectedLanguage == AppLanguage.ZH

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = AppStrings.get("settings", selectedLanguage),
                    style = MaterialTheme.typography.titleLarge
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 460.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Monthly Budget Section
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = AppStrings.get("set_budget", selectedLanguage),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = budgetInput,
                        onValueChange = { budgetInput = it },
                        label = { Text(AppStrings.get("monthly_budget", selectedLanguage)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                HorizontalDivider()

                // 2. Theme Color Selection Section
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = AppStrings.get("theme_color", selectedLanguage),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AppThemeColor.entries.forEach { themeColor ->
                            val isColorSelected = themeColor == selectedColor
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isColorSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                                modifier = Modifier
                                    .bouncyClickable { selectedColor = themeColor }
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

                HorizontalDivider()

                // 3. Language Selection Section
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = AppStrings.get("app_language", selectedLanguage),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        AppLanguage.entries.forEachIndexed { index, lang ->
                            SegmentedButton(
                                selected = selectedLanguage == lang,
                                onClick = { selectedLanguage = lang },
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = AppLanguage.entries.size)
                            ) {
                                Text(lang.displayName)
                            }
                        }
                    }
                }

                HorizontalDivider()

                // 4. Beta Testing & Experimental Features Section
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isZh) "🧪 加入 Beta 實驗性功能測試" else "🧪 Join Beta Testing",
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
                            onCheckedChange = {
                                betaTestingState = it
                                if (!it) {
                                    livingExpensePoolState = false
                                }
                            }
                        )
                    }

                    if (betaTestingState) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // 1. Living Expense Pool
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (isZh) "動態生活費預算池" else "Living Expense Pool",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = if (isZh) "累積過去天數未花完預算" else "Rollover past unspent daily budget",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Switch(
                                        checked = livingExpensePoolState,
                                        onCheckedChange = { livingExpensePoolState = it }
                                    )
                                }

                                HorizontalDivider()

                                // 2. Income Tracking
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
                                        onCheckedChange = { incomeTrackingState = it }
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
                                        onCheckedChange = { searchState = it }
                                    )
                                }

                                HorizontalDivider()

                                // 4. Subscriptions Tracker
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (isZh) "訂閱與定期扣款" else "Subscriptions Tracker",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = if (isZh) "固定扣款與月度訂閱" else "Track fixed bills & subscriptions",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Switch(
                                        checked = subscriptionState,
                                        onCheckedChange = { subscriptionState = it }
                                    )
                                }

                                HorizontalDivider()

                                // 5. Multi Account
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
                                        onCheckedChange = { multiAccountState = it }
                                    )
                                }

                                HorizontalDivider()

                                // 7. Savings Goals
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (isZh) "儲蓄目標" else "Savings Goals",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = if (isZh) "旅遊/購物存錢進度" else "Track wishlist & savings progress",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Switch(
                                        checked = savingsGoalsState,
                                        onCheckedChange = { savingsGoalsState = it }
                                    )
                                }

                                HorizontalDivider()

                                // 8. Modern 3D Hand-Drawn UI
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
                                        onCheckedChange = { modern3DUiState = it }
                                    )
                                }

                                HorizontalDivider()

                                // 9. Drag Date Reorder
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (isZh) "長按移動紀錄(不建議開啟)" else "Move Date on Long Press(not recommended to enable it)",
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
                                        onCheckedChange = { dragDateReorderState = it }
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider()

                // 5. Automatic Backup Switch Section
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
                        onCheckedChange = { autoBackupState = it }
                    )
                }

                HorizontalDivider()

                // 6. Google Drive Cloud Backup & Restore Section
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                }

                HorizontalDivider()

                // 7. Export Financial Report Section
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                }

                HorizontalDivider()

                // 8. App Version Footer Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Budget Manager v1.0.2",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (isZh) "版本 1.0.2 • 記帳與預算管家" else "Version 1.0.2 • Smart Budget Tracker",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val limit = budgetInput.toDoubleOrNull() ?: 0.0
                    val effectivePool = betaTestingState && livingExpensePoolState
                    onSaveSettings(
                        limit,
                        selectedColor,
                        selectedLanguage,
                        betaTestingState,
                        effectivePool,
                        incomeTrackingState,
                        searchState,
                        subscriptionState,
                        multiAccountState,
                        savingsGoalsState,
                        modern3DUiState,
                        dragDateReorderState,
                        autoBackupState
                    )
                    onDismissRequest()
                }
            ) {
                Text(AppStrings.get("save", selectedLanguage), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(AppStrings.get("cancel", selectedLanguage))
            }
        }
    )
}
