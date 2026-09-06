package com.example.debit.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.debit.R
import com.example.debit.data.AppLanguage
import com.example.debit.data.Transaction
import com.example.debit.ui.DebitViewModel
import com.example.debit.ui.components.ExpensePieChartCard
import com.example.debit.ui.dialogs.AddTransactionDialog
import com.example.debit.ui.dialogs.BudgetSettingsDialog
import com.example.debit.ui.dialogs.MonthSelectorDialog
import com.example.debit.ui.dialogs.SettingsDialog
import com.example.debit.ui.utils.AppStrings
import com.example.debit.ui.utils.BackupUtils
import com.example.debit.ui.utils.ExportUtils
import com.example.debit.ui.utils.getCategoryIcon
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class DateGroup(
    val dateKey: String,
    val displayDate: String,
    val isToday: Boolean,
    val totalDailyExpense: Double,
    val transactions: List<Transaction>
)

fun groupTransactionsByDate(transactions: List<Transaction>, language: AppLanguage): List<DateGroup> {
    val isZh = language == AppLanguage.ZH
    val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val yesterdayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(System.currentTimeMillis() - 86400000L))

    val keyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val displayFormat = if (isZh)
        SimpleDateFormat("MM月dd日 EEEE", Locale.TRADITIONAL_CHINESE)
    else
        SimpleDateFormat("MMM dd, EEE", Locale.ENGLISH)

    val grouped = transactions.groupBy { keyFormat.format(Date(it.date)) }

    return grouped.map { (dateKey, list) ->
        val firstDate = Date(list.first().date)
        val formattedDate = displayFormat.format(firstDate)
        val prefix = when (dateKey) {
            todayStr -> if (isZh) "今日 " else "Today, "
            yesterdayStr -> if (isZh) "昨日 " else "Yesterday, "
            else -> ""
        }
        DateGroup(
            dateKey = dateKey,
            displayDate = "$prefix$formattedDate",
            isToday = dateKey == todayStr,
            totalDailyExpense = list.sumOf { it.amount },
            transactions = list.sortedByDescending { it.date }
        )
    }.sortedByDescending { it.dateKey }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DebitViewModel
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val lang = uiState.appLanguage
    val isZh = lang == AppLanguage.ZH

    var showAddDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showMonthSelectorDialog by remember { mutableStateOf(false) }
    var editingTransaction by remember { mutableStateOf<Transaction?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                context.contentResolver.openInputStream(it)?.use { stream ->
                    val jsonContent = stream.bufferedReader().readText()
                    viewModel.restoreBackupJson(jsonContent) { count ->
                        val msg = if (count >= 0) "🎉 成功還原 $count 筆記帳紀錄與預算數據！" else "❌ 備份檔格式不正確"
                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                    }
                }
            } catch (_: Exception) {
                Toast.makeText(context, "❌ 無法讀取選取的備份檔案", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val groupedTransactions = remember(uiState.transactions, lang) {
        groupTransactionsByDate(uiState.transactions, lang)
    }

    val headerMonthDisplay = if (isZh)
        "${uiState.currentYearMonth.replace("-", "年")}月概覽"
    else
        "Overview (${uiState.currentYearMonth})"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.clickable { showMonthSelectorDialog = true },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_piggy_bank),
                                    contentDescription = "App Logo",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Budget Manager",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = headerMonthDisplay,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "切換月份",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showSettingsDialog = true },
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = AppStrings.get("settings", lang),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = AppStrings.get("add_record", lang)) },
                text = { Text(AppStrings.get("add_record", lang), fontWeight = FontWeight.Bold) },
                shape = RoundedCornerShape(20.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item { Spacer(modifier = Modifier.height(2.dp)) }

            // Historical Month Banner
            if (!uiState.isCurrentMonth) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${AppStrings.get("viewing_history", lang)} (${uiState.currentYearMonth})",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                            TextButton(
                                onClick = { viewModel.resetToCurrentMonth() }
                            ) {
                                Text(AppStrings.get("return_current_month", lang), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // 1. Swipeable Budget Overview Card
            item {
                BudgetOverviewCard(
                    totalBudget = uiState.totalBudgetLimit,
                    totalExpense = uiState.totalExpense,
                    todayExpense = uiState.todayExpense,
                    remainingBudget = uiState.remainingBudget,
                    progress = uiState.totalBudgetProgress,
                    isOverBudget = uiState.isOverBudget,
                    isNearLimit = uiState.isNearBudgetLimit,
                    language = lang,
                    onOpenBudgetSettings = { showSettingsDialog = true }
                )
            }

            // 2. Category Expense Pie Chart & Daily Chart Card
            if ((uiState.totalExpense > 0 || uiState.totalBudgetLimit > 0) && uiState.categoryExpenses.isNotEmpty()) {
                item {
                    ExpensePieChartCard(
                        categoryExpenses = uiState.categoryExpenses,
                        totalExpense = uiState.totalExpense,
                        totalBudget = uiState.totalBudgetLimit,
                        transactions = uiState.transactions,
                        language = lang
                    )
                }
            }

            // 3. Recent Transactions Section Header
            item {
                Text(
                    text = AppStrings.get("records_breakdown", lang),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // 4. Grouped Transactions By Date
            if (groupedTransactions.isEmpty()) {
                item {
                    EmptyTransactionsCard(language = lang, onAddClick = { showAddDialog = true })
                }
            } else {
                groupedTransactions.forEach { group ->
                    item(key = "header_${group.dateKey}") {
                        DateHeaderItem(group = group, language = lang)
                    }

                    items(
                        items = group.transactions,
                        key = { it.id }
                    ) { transaction ->
                        TransactionItem(
                            transaction = transaction,
                            language = lang,
                            onEdit = { editingTransaction = transaction },
                            onDelete = { viewModel.deleteTransaction(transaction) }
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    // Dialogs
    if (showAddDialog) {
        AddTransactionDialog(
            language = lang,
            locationPredictionEnabled = uiState.locationPredictionEnabled,
            recentLocations = uiState.recentLocations,
            onGetLocationEstimate = { loc -> viewModel.getLocationEstimate(loc) },
            onDismissRequest = { showAddDialog = false },
            onConfirm = { amount, category, note, date, locationName ->
                viewModel.addTransaction(amount, category, note, date, locationName)
            }
        )
    }

    if (editingTransaction != null) {
        AddTransactionDialog(
            initialTransaction = editingTransaction,
            language = lang,
            locationPredictionEnabled = uiState.locationPredictionEnabled,
            recentLocations = uiState.recentLocations,
            onGetLocationEstimate = { loc -> viewModel.getLocationEstimate(loc) },
            onDismissRequest = { editingTransaction = null },
            onConfirm = { amount, category, note, date, locationName ->
                viewModel.updateTransaction(
                    id = editingTransaction!!.id,
                    amount = amount,
                    category = category,
                    note = note,
                    date = date,
                    locationName = locationName
                )
                editingTransaction = null
            }
        )
    }

    if (showSettingsDialog) {
        SettingsDialog(
            initialTotalBudget = uiState.totalBudgetLimit,
            currentThemeColor = uiState.themeColor,
            currentLanguage = uiState.appLanguage,
            currentBetaTestingEnabled = uiState.isBetaTestingEnabled,
            currentLocationPredictionEnabled = uiState.locationPredictionEnabled,
            currentAutoBackupEnabled = uiState.autoBackupEnabled,
            lastAutoBackupTime = uiState.lastAutoBackupTime,
            onSaveSettings = { budgetLimit, color, language, betaTesting, locationPrediction, autoBackup ->
                viewModel.saveSettings(budgetLimit, color, language, betaTesting, locationPrediction, autoBackup)
            },
            onExportReport = {
                ExportUtils.exportTransactionsToCsv(context, uiState.transactions, uiState.appLanguage)
            },
            onBackupToGoogleDrive = {
                BackupUtils.backupToGoogleDrive(context, uiState.transactions, emptyList())
            },
            onRestoreFromGoogleDrive = {
                filePickerLauncher.launch("*/*")
            },
            onDismissRequest = { showSettingsDialog = false }
        )
    }

    if (showMonthSelectorDialog) {
        MonthSelectorDialog(
            currentSelectedMonth = uiState.currentYearMonth,
            availableMonths = uiState.availableMonths,
            onSelectMonth = { monthStr -> viewModel.selectYearMonth(monthStr) },
            onResetToCurrentMonth = { viewModel.resetToCurrentMonth() },
            onDismissRequest = { showMonthSelectorDialog = false }
        )
    }
}

@Composable
fun DateHeaderItem(group: DateGroup, language: AppLanguage) {
    val subtotalLabel = AppStrings.get("subtotal", language)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 2.dp, start = 4.dp, end = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = if (group.isToday) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Text(
                text = group.displayDate,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = if (group.isToday) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
        Text(
            text = "$subtotalLabel: -$${String.format(Locale.getDefault(), "%,.0f", group.totalDailyExpense)}",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
fun BudgetOverviewCard(
    totalBudget: Double,
    totalExpense: Double,
    todayExpense: Double,
    remainingBudget: Double,
    progress: Float,
    isOverBudget: Boolean,
    isNearLimit: Boolean,
    language: AppLanguage,
    onOpenBudgetSettings: () -> Unit
) {
    val pagerState = rememberPagerState { 2 }

    val calendar = Calendar.getInstance()
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val today = calendar.get(Calendar.DAY_OF_MONTH)
    val remainingDays = maxOf(1, daysInMonth - today + 1)

    val dailyBudgetLimit = if (totalBudget > 0) totalBudget / daysInMonth else 0.0
    val dynamicDailyBudget = if (totalBudget > 0 && remainingDays > 0) maxOf(0.0, remainingBudget / remainingDays) else 0.0
    val todayRemaining = dailyBudgetLimit - todayExpense
    val isTodayOver = totalBudget > 0 && todayExpense > dailyBudgetLimit
    val todayProgress = if (dailyBudgetLimit > 0) (todayExpense / dailyBudgetLimit).toFloat().coerceIn(0f, 1f) else 0f

    val isCurrentPageError = (pagerState.currentPage == 0 && isOverBudget) || (pagerState.currentPage == 1 && isTodayOver)

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isCurrentPageError)
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
            else
                MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 12.dp, start = 20.dp, end = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth()
            ) { page ->
                if (page == 0) {
                    MonthlyBudgetView(
                        totalBudget = totalBudget,
                        totalExpense = totalExpense,
                        remainingBudget = remainingBudget,
                        progress = progress,
                        isOverBudget = isOverBudget,
                        isNearLimit = isNearLimit,
                        language = language,
                        onOpenBudgetSettings = onOpenBudgetSettings
                    )
                } else {
                    DailyBudgetView(
                        totalBudget = totalBudget,
                        dailyBudgetLimit = dailyBudgetLimit,
                        dynamicDailyBudget = dynamicDailyBudget,
                        todayExpense = todayExpense,
                        todayRemaining = todayRemaining,
                        todayProgress = todayProgress,
                        isTodayOver = isTodayOver,
                        language = language,
                        onOpenBudgetSettings = onOpenBudgetSettings
                    )
                }
            }

            // Pager Indicator Dots
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(2) { index ->
                    val color = if (pagerState.currentPage == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                    val width = if (pagerState.currentPage == index) 18.dp else 6.dp
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 2.dp)
                            .height(6.dp)
                            .width(width)
                            .clip(CircleShape)
                            .background(color)
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthlyBudgetView(
    totalBudget: Double,
    totalExpense: Double,
    remainingBudget: Double,
    progress: Float,
    isOverBudget: Boolean,
    isNearLimit: Boolean,
    language: AppLanguage,
    onOpenBudgetSettings: () -> Unit
) {
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "monthlyProgress")

    val progressColor by animateColorAsState(
        targetValue = when {
            isOverBudget -> MaterialTheme.colorScheme.error
            isNearLimit -> Color(0xFFFF9800)
            else -> MaterialTheme.colorScheme.primary
        },
        label = "monthlyProgressColor"
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.AccountBalanceWallet,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = AppStrings.get("monthly_status", language),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (totalBudget <= 0) {
                OutlinedButton(
                    onClick = onOpenBudgetSettings,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(AppStrings.get("set_budget", language), fontSize = 12.sp)
                }
            } else if (isOverBudget) {
                Surface(
                    color = MaterialTheme.colorScheme.error,
                    shape = CircleShape
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(AppStrings.get("over_budget", language), color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
            } else if (isNearLimit) {
                Surface(
                    color = Color(0xFFFF9800),
                    shape = CircleShape
                ) {
                    Text(
                        text = AppStrings.get("near_limit", language),
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        if (totalBudget > 0) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = AppStrings.get("month_spent", language),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$${String.format(Locale.getDefault(), "%,.0f", totalExpense)}",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (isOverBudget) AppStrings.get("month_over", language) else AppStrings.get("month_remaining", language),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$${String.format(Locale.getDefault(), "%,.0f", if (isOverBudget) totalExpense - totalBudget else remainingBudget)}",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isOverBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = progressColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${AppStrings.get("month_progress", language)}: ${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${AppStrings.get("total_budget", language)}: $${String.format(Locale.getDefault(), "%,.0f", totalBudget)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Text(
                text = if (language == AppLanguage.ZH) "點擊右上角「設定預算」規劃您的本月開銷上限，解鎖今日與全月預算追蹤功能！" else "Tap 'Set Budget' to set your monthly spending limit and unlock daily budget tracking!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DailyBudgetView(
    totalBudget: Double,
    dailyBudgetLimit: Double,
    dynamicDailyBudget: Double,
    todayExpense: Double,
    todayRemaining: Double,
    todayProgress: Float,
    isTodayOver: Boolean,
    language: AppLanguage,
    onOpenBudgetSettings: () -> Unit
) {
    val animatedProgress by animateFloatAsState(targetValue = todayProgress, label = "dailyProgress")

    val progressColor by animateColorAsState(
        targetValue = when {
            isTodayOver -> MaterialTheme.colorScheme.error
            todayProgress >= 0.8f -> Color(0xFFFF9800)
            else -> MaterialTheme.colorScheme.primary
        },
        label = "dailyProgressColor"
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Today,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = AppStrings.get("daily_status", language),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (totalBudget <= 0) {
                OutlinedButton(
                    onClick = onOpenBudgetSettings,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(AppStrings.get("set_budget", language), fontSize = 12.sp)
                }
            } else if (isTodayOver) {
                Surface(
                    color = MaterialTheme.colorScheme.error,
                    shape = CircleShape
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(AppStrings.get("today_over", language), color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                ) {
                    Text(
                        text = AppStrings.get("today_ample", language),
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        if (totalBudget > 0) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = AppStrings.get("today_spent", language),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$${String.format(Locale.getDefault(), "%,.0f", todayExpense)}",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (isTodayOver) AppStrings.get("today_over", language) else AppStrings.get("today_remaining", language),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$${String.format(Locale.getDefault(), "%,.0f", if (isTodayOver) todayExpense - dailyBudgetLimit else todayRemaining)}",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isTodayOver) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = progressColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${AppStrings.get("avg_daily_budget", language)}: $${String.format(Locale.getDefault(), "%,.0f", dailyBudgetLimit)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${AppStrings.get("dynamic_daily_budget", language)}: $${String.format(Locale.getDefault(), "%,.0f", dynamicDailyBudget)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        } else {
            Text(
                text = if (language == AppLanguage.ZH) "點擊右上角「設定預算」規劃您的本月開銷上限，系統將自動為您平攤計算每日可用金額！" else "Tap 'Set Budget' to set your monthly spending limit and unlock daily budget tracking!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun TransactionItem(
    transaction: Transaction,
    language: AppLanguage,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(transaction.date))
    val localizedCategory = AppStrings.getCategoryName(transaction.category, language)
    val localizedNote = if (transaction.note.isNotBlank()) {
        transaction.note.split(" • ").joinToString(" • ") { AppStrings.getSubCategoryName(it, language) }
    } else ""

    val locPrefix = if (transaction.locationName.isNotBlank()) "📍 ${transaction.locationName} • " else ""
    val noteDisplay = "$locPrefix${if (localizedNote.isNotBlank()) "$localizedNote • $timeStr" else timeStr}"

    Card(
        onClick = onEdit,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getCategoryIcon(transaction.category),
                        contentDescription = localizedCategory,
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = localizedCategory,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = noteDisplay,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "-$${String.format(Locale.getDefault(), "%,.0f", transaction.amount)}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.error
                )

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = AppStrings.get("edit_item", language),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = AppStrings.get("delete_item", language),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyTransactionsCard(
    language: AppLanguage,
    onAddClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.MoneyOff,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Text(
                text = AppStrings.get("no_records", language),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedButton(
                onClick = onAddClick,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(AppStrings.get("add_first", language))
            }
        }
    }
}
