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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import com.example.debit.data.TransactionType
import com.example.debit.ui.DebitViewModel
import com.example.debit.ui.components.ExpensePieChartCard
import com.example.debit.ui.components.Modern3DBudgetCard
import com.example.debit.ui.components.Modern3DTransactionItem
import com.example.debit.ui.dialogs.AddTransactionDialog
import com.example.debit.ui.dialogs.BudgetSettingsDialog
import com.example.debit.ui.dialogs.MonthSelectorDialog
import com.example.debit.ui.dialogs.MonthlyBreakdownDialog
import com.example.debit.ui.dialogs.SettingsDialog
import com.example.debit.ui.utils.AppStrings
import com.example.debit.ui.utils.BackupUtils
import com.example.debit.ui.utils.ExportUtils
import com.example.debit.ui.utils.OrganicShapeChip
import com.example.debit.ui.utils.OrganicShapeLarge
import com.example.debit.ui.utils.OrganicShapeMedium
import com.example.debit.ui.utils.bouncyClickable
import com.example.debit.ui.utils.getCategoryIcon
import com.example.debit.ui.utils.getWarmTimeGreeting
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

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

fun parseDateKeyToMillis(dateKey: String, originalTimestamp: Long): Long {
    return try {
        val keyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val parsedDate = keyFormat.parse(dateKey) ?: return originalTimestamp

        val calendar = Calendar.getInstance().apply { timeInMillis = parsedDate.time }
        val origCal = Calendar.getInstance().apply { timeInMillis = originalTimestamp }

        calendar.set(Calendar.HOUR_OF_DAY, origCal.get(Calendar.HOUR_OF_DAY))
        calendar.set(Calendar.MINUTE, origCal.get(Calendar.MINUTE))
        calendar.set(Calendar.SECOND, origCal.get(Calendar.SECOND))

        calendar.timeInMillis
    } catch (_: Exception) {
        originalTimestamp
    }
}

fun findClosestDateHeaderKey(
    touchWindowY: Float,
    dateHeaderBoundsMap: Map<String, Rect>
): String? {
    if (dateHeaderBoundsMap.isEmpty()) return null

    var closestKey: String? = null
    var minDistance = Float.MAX_VALUE

    for ((key, rect) in dateHeaderBoundsMap) {
        val centerY = rect.center.y
        val distance = abs(centerY - touchWindowY)
        if (distance < minDistance) {
            minDistance = distance
            closestKey = key
        }
    }

    return if (minDistance <= 500f) closestKey else null
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
    var showMonthlyBreakdownDialog by remember { mutableStateOf(false) }
    var showAddSubscriptionDialog by remember { mutableStateOf(false) }
    var showAddSavingsGoalDialog by remember { mutableStateOf(false) }
    var editingTransaction by remember { mutableStateOf<Transaction?>(null) }
    var movingDateTransaction by remember { mutableStateOf<Transaction?>(null) }

    var draggingTransaction by remember { mutableStateOf<Transaction?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var hoveredDateKey by remember { mutableStateOf<String?>(null) }
    var draggedItemRect by remember { mutableStateOf<Rect?>(null) }
    val dateHeaderBoundsMap = remember { mutableStateMapOf<String, Rect>() }

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

    val groupedTransactions = remember(uiState.filteredTransactions, lang) {
        groupTransactionsByDate(uiState.filteredTransactions, lang)
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
                        modifier = Modifier.bouncyClickable { showMonthSelectorDialog = true },
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
                shape = OrganicShapeChip,
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

            // 0. Search Bar (Beta Feature 2)
            if (uiState.isSearchEnabled) {
                item {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = { Text(if (lang == AppLanguage.ZH) "🔍 搜尋備註、地點、類別或帳戶..." else "🔍 Search notes, locations, accounts...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "搜尋", tint = MaterialTheme.colorScheme.primary)
                        },
                        trailingIcon = {
                            if (uiState.searchQuery.isNotBlank()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "清除")
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }

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
                if (uiState.isModern3DUiEnabled) {
                    Modern3DBudgetCard(
                        totalBudget = uiState.totalBudgetLimit,
                        totalExpense = uiState.totalExpense,
                        todayExpense = uiState.todayExpense,
                        remainingBudget = uiState.remainingBudget,
                        progress = uiState.totalBudgetProgress,
                        isOverBudget = uiState.isOverBudget,
                        isNearLimit = uiState.isNearBudgetLimit,
                        isLivingExpensePoolEnabled = uiState.isLivingExpensePoolEnabled,
                        livingExpensePool = uiState.livingExpensePool,
                        language = lang,
                        onOpenBudgetSettings = { showSettingsDialog = true },
                        onOpenMonthlyBreakdown = { showMonthlyBreakdownDialog = true }
                    )
                } else {
                    BudgetOverviewCard(
                        totalBudget = uiState.totalBudgetLimit,
                        totalExpense = uiState.totalExpense,
                        todayExpense = uiState.todayExpense,
                        remainingBudget = uiState.remainingBudget,
                        progress = uiState.totalBudgetProgress,
                        isOverBudget = uiState.isOverBudget,
                        isNearLimit = uiState.isNearBudgetLimit,
                        isLivingExpensePoolEnabled = uiState.isLivingExpensePoolEnabled,
                        livingExpensePool = uiState.livingExpensePool,
                        language = lang,
                        onOpenBudgetSettings = { showSettingsDialog = true },
                        onOpenMonthlyBreakdown = { showMonthlyBreakdownDialog = true }
                    )
                }
            }

            // 2. Net Savings Overview Card (Beta Feature 1)
            if (uiState.isIncomeTrackingEnabled) {
                item {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(if (lang == AppLanguage.ZH) "💵 總收入" else "Income", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$${String.format(Locale.getDefault(), "%,.0f", uiState.totalIncome)}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(if (lang == AppLanguage.ZH) "💸 總支出" else "Expenses", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("$${String.format(Locale.getDefault(), "%,.0f", uiState.totalExpense)}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.error)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(if (lang == AppLanguage.ZH) "🏦 本月淨儲蓄" else "Net Savings", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                val netColor = if (uiState.netSavings >= 0) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
                                val signStr = if (uiState.netSavings >= 0) "+" else ""
                                Text("$signStr$${String.format(Locale.getDefault(), "%,.0f", uiState.netSavings)}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = netColor)
                            }
                        }
                    }
                }
            }

            // 3. Category Expense Pie Chart & Daily Chart Card
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

            // 4. Subscriptions & Fixed Bills Card (Beta Feature 3)
            if (uiState.isSubscriptionEnabled) {
                item {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (lang == AppLanguage.ZH) "🔁 訂閱與定期扣款 ($${String.format(Locale.getDefault(), "%,.0f", uiState.totalSubscriptionsMonthly)}/月)" else "Subscriptions ($${String.format(Locale.getDefault(), "%,.0f", uiState.totalSubscriptionsMonthly)}/mo)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                TextButton(onClick = { showAddSubscriptionDialog = true }) {
                                    Text(if (lang == AppLanguage.ZH) "➕ 新增訂閱" else "+ Add", fontSize = 12.sp)
                                }
                            }

                            if (uiState.subscriptions.isEmpty()) {
                                Text(
                                    text = if (lang == AppLanguage.ZH) "尚無固定訂閱或定期扣款項目" else "No active subscriptions",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                uiState.subscriptions.forEach { sub ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("${sub.name} (每月 ${sub.billingDay} 日)", style = MaterialTheme.typography.bodySmall)
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("$${String.format(Locale.getDefault(), "%,.0f", sub.amount)}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                            IconButton(onClick = { viewModel.deleteSubscription(sub) }, modifier = Modifier.size(24.dp)) {
                                                Icon(Icons.Default.Delete, contentDescription = "刪除", modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 5. Savings Goals Card (Beta Feature 6)
            if (uiState.isSavingsGoalsEnabled) {
                item {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (lang == AppLanguage.ZH) "🎯 夢想儲蓄目標箱" else "Savings Goals",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                TextButton(onClick = { showAddSavingsGoalDialog = true }) {
                                    Text(if (lang == AppLanguage.ZH) "➕ 新增目標" else "+ Add Goal", fontSize = 12.sp)
                                }
                            }

                            if (uiState.savingsGoals.isEmpty()) {
                                Text(
                                    text = if (lang == AppLanguage.ZH) "點擊上方新增您的第一筆儲蓄願望！" else "Tap add to create your first goal",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                uiState.savingsGoals.forEach { goal ->
                                    val progress = (goal.currentAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f)
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(goal.title, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                            Text("$${String.format(Locale.getDefault(), "%,.0f", goal.currentAmount)} / $${String.format(Locale.getDefault(), "%,.0f", goal.targetAmount)}", style = MaterialTheme.typography.labelSmall)
                                        }
                                        LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
                                    }
                                }
                            }
                        }
                    }
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
                        DateHeaderItem(
                            group = group,
                            language = lang,
                            isHovered = hoveredDateKey == group.dateKey,
                            onBoundsMeasured = { rect ->
                                dateHeaderBoundsMap[group.dateKey] = rect
                            }
                        )
                    }

                    items(
                        items = group.transactions,
                        key = { it.id }
                    ) { transaction ->
                        val isBeingDragged = draggingTransaction?.id == transaction.id

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .onGloballyPositioned { coordinates ->
                                    if (isBeingDragged && draggedItemRect == null) {
                                        draggedItemRect = coordinates.boundsInWindow()
                                    }
                                }
                                .graphicsLayer {
                                    if (isBeingDragged) {
                                        translationX = dragOffset.x
                                        translationY = dragOffset.y
                                        alpha = 0.65f
                                        scaleX = 1.03f
                                        scaleY = 1.03f
                                        shadowElevation = 16f
                                    }
                                }
                        ) {
                            if (uiState.isModern3DUiEnabled) {
                                Modern3DTransactionItem(
                                    transaction = transaction,
                                    language = lang,
                                    isDragDateReorderEnabled = uiState.isDragDateReorderEnabled,
                                    onStartDrag = {
                                        draggingTransaction = transaction
                                        dragOffset = Offset.Zero
                                        hoveredDateKey = null
                                        draggedItemRect = null
                                    },
                                    onDrag = { dragAmount ->
                                        dragOffset += dragAmount
                                        val startRect = draggedItemRect
                                        if (startRect != null) {
                                            val currentTouchY = startRect.center.y + dragOffset.y
                                            hoveredDateKey = findClosestDateHeaderKey(currentTouchY, dateHeaderBoundsMap)
                                        }
                                    },
                                    onEndDrag = {
                                        if (draggingTransaction != null && hoveredDateKey != null) {
                                            val newDateMillis = parseDateKeyToMillis(hoveredDateKey!!, draggingTransaction!!.date)
                                            viewModel.moveTransactionToDate(draggingTransaction!!, newDateMillis)
                                            val msg = if (isZh) "已移至 $hoveredDateKey ✨" else "Moved to $hoveredDateKey ✨"
                                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                        }
                                        draggingTransaction = null
                                        dragOffset = Offset.Zero
                                        hoveredDateKey = null
                                        draggedItemRect = null
                                    },
                                    onCancelDrag = {
                                        draggingTransaction = null
                                        dragOffset = Offset.Zero
                                        hoveredDateKey = null
                                        draggedItemRect = null
                                    },
                                    onEdit = { editingTransaction = transaction },
                                    onDelete = { viewModel.deleteTransaction(transaction) }
                                )
                            } else {
                                TransactionItem(
                                    transaction = transaction,
                                    language = lang,
                                    isDragDateReorderEnabled = uiState.isDragDateReorderEnabled,
                                    onStartDrag = {
                                        draggingTransaction = transaction
                                        dragOffset = Offset.Zero
                                        hoveredDateKey = null
                                        draggedItemRect = null
                                    },
                                    onDrag = { dragAmount ->
                                        dragOffset += dragAmount
                                        val startRect = draggedItemRect
                                        if (startRect != null) {
                                            val currentTouchY = startRect.center.y + dragOffset.y
                                            hoveredDateKey = findClosestDateHeaderKey(currentTouchY, dateHeaderBoundsMap)
                                        }
                                    },
                                    onEndDrag = {
                                        if (draggingTransaction != null && hoveredDateKey != null) {
                                            val newDateMillis = parseDateKeyToMillis(hoveredDateKey!!, draggingTransaction!!.date)
                                            viewModel.moveTransactionToDate(draggingTransaction!!, newDateMillis)
                                            val msg = if (isZh) "已移至 $hoveredDateKey ✨" else "Moved to $hoveredDateKey ✨"
                                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                        }
                                        draggingTransaction = null
                                        dragOffset = Offset.Zero
                                        hoveredDateKey = null
                                        draggedItemRect = null
                                    },
                                    onCancelDrag = {
                                        draggingTransaction = null
                                        dragOffset = Offset.Zero
                                        hoveredDateKey = null
                                        draggedItemRect = null
                                    },
                                    onEdit = { editingTransaction = transaction },
                                    onDelete = { viewModel.deleteTransaction(transaction) }
                                )
                            }
                        }
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
            isLivingExpensePoolEnabled = uiState.isLivingExpensePoolEnabled,
            livingExpensePoolAmount = uiState.livingExpensePool,
            isIncomeTrackingEnabled = uiState.isIncomeTrackingEnabled,
            isMultiAccountEnabled = uiState.isMultiAccountEnabled,
            locationPredictionEnabled = false,
            onGetCustomSubCategories = { cat -> viewModel.getCustomSubCategories(cat) },
            onAddCustomSubCategory = { cat, sub -> viewModel.addCustomSubCategory(cat, sub) },
            onDismissRequest = { showAddDialog = false },
            onConfirm = { amount, category, note, date, locationName, deductFromPool, accountName, type ->
                viewModel.addTransaction(amount, category, note, date, locationName, deductFromPool, accountName, type)
            }
        )
    }

    if (editingTransaction != null) {
        AddTransactionDialog(
            initialTransaction = editingTransaction,
            language = lang,
            isLivingExpensePoolEnabled = uiState.isLivingExpensePoolEnabled,
            livingExpensePoolAmount = uiState.livingExpensePool,
            isIncomeTrackingEnabled = uiState.isIncomeTrackingEnabled,
            isMultiAccountEnabled = uiState.isMultiAccountEnabled,
            locationPredictionEnabled = false,
            onGetCustomSubCategories = { cat -> viewModel.getCustomSubCategories(cat) },
            onAddCustomSubCategory = { cat, sub -> viewModel.addCustomSubCategory(cat, sub) },
            onDismissRequest = { editingTransaction = null },
            onConfirm = { amount, category, note, date, locationName, deductFromPool, accountName, type ->
                viewModel.updateTransaction(
                    id = editingTransaction!!.id,
                    amount = amount,
                    category = category,
                    note = note,
                    date = date,
                    locationName = locationName,
                    deductFromPool = deductFromPool,
                    accountName = accountName,
                    type = type
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
            currentLivingExpensePoolEnabled = uiState.isLivingExpensePoolEnabled,
            currentIncomeTrackingEnabled = uiState.isIncomeTrackingEnabled,
            currentSearchEnabled = uiState.isSearchEnabled,
            currentSubscriptionEnabled = uiState.isSubscriptionEnabled,
            currentMultiAccountEnabled = uiState.isMultiAccountEnabled,
            currentSavingsGoalsEnabled = uiState.isSavingsGoalsEnabled,
            currentModern3DUiEnabled = uiState.isModern3DUiEnabled,
            currentDragDateReorderEnabled = uiState.isDragDateReorderEnabled,
            currentAutoBackupEnabled = uiState.autoBackupEnabled,
            lastAutoBackupTime = uiState.lastAutoBackupTime,
            onSaveSettings = { budgetLimit, color, language, betaTesting, livingExpensePool, incomeTracking, search, subscription, multiAccount, savingsGoals, modern3D, dragDateReorder, autoBackup ->
                viewModel.saveSettings(
                    budgetLimit, color, language, betaTesting, livingExpensePool,
                    incomeTracking, search, subscription, multiAccount, savingsGoals, modern3D, dragDateReorder, autoBackup
                )
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

    if (showMonthlyBreakdownDialog) {
        MonthlyBreakdownDialog(
            currentYearMonth = uiState.currentYearMonth,
            dateGroups = groupedTransactions,
            totalExpense = uiState.totalExpense,
            language = lang,
            onDismissRequest = { showMonthlyBreakdownDialog = false }
        )
    }

    if (movingDateTransaction != null) {
        MoveDateDialog(
            transaction = movingDateTransaction!!,
            language = lang,
            onDismissRequest = { movingDateTransaction = null },
            onConfirmMove = { newDateMillis ->
                viewModel.moveTransactionToDate(movingDateTransaction!!, newDateMillis)
                movingDateTransaction = null
            }
        )
    }

    if (showAddSubscriptionDialog) {
        AddSubscriptionDialog(
            language = lang,
            onDismissRequest = { showAddSubscriptionDialog = false },
            onConfirm = { name, amount, day ->
                viewModel.addSubscription(name, amount, day)
            }
        )
    }

    if (showAddSavingsGoalDialog) {
        AddSavingsGoalDialog(
            language = lang,
            onDismissRequest = { showAddSavingsGoalDialog = false },
            onConfirm = { title, target, initial ->
                viewModel.addSavingsGoal(title, target, initial)
            }
        )
    }
}

@Composable
fun AddSubscriptionDialog(
    language: AppLanguage,
    onDismissRequest: () -> Unit,
    onConfirm: (name: String, amount: Double, billingDay: Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var billingDayText by remember { mutableStateOf("1") }
    var isError by remember { mutableStateOf(false) }

    val isZh = language == AppLanguage.ZH

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(if (isZh) "新增定期扣款 / 訂閱項目" else "Add Subscription") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; isError = false },
                    label = { Text(if (isZh) "項目名稱 (如 Netflix, 房租)" else "Subscription Name") },
                    isError = isError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text(if (isZh) "每月扣款金額 ($)" else "Monthly Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = billingDayText,
                    onValueChange = { billingDayText = it },
                    label = { Text(if (isZh) "每月扣款日期 (1-31 日)" else "Billing Day (1-31)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val trimmed = name.trim()
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    val day = billingDayText.toIntOrNull()?.coerceIn(1, 31) ?: 1
                    if (trimmed.isBlank() || amt <= 0) {
                        isError = true
                    } else {
                        onConfirm(trimmed, amt, day)
                        onDismissRequest()
                    }
                }
            ) {
                Text(AppStrings.get("confirm", language))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(AppStrings.get("cancel", language))
            }
        }
    )
}

@Composable
fun AddSavingsGoalDialog(
    language: AppLanguage,
    onDismissRequest: () -> Unit,
    onConfirm: (title: String, targetAmount: Double, initialAmount: Double) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var targetAmountText by remember { mutableStateOf("") }
    var initialAmountText by remember { mutableStateOf("0") }
    var isError by remember { mutableStateOf(false) }

    val isZh = language == AppLanguage.ZH

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(if (isZh) "新增儲蓄目標" else "Add Savings Goal") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; isError = false },
                    label = { Text(if (isZh) "目標名稱 (如: 日本旅遊, 買新筆電)" else "Goal Title") },
                    isError = isError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = targetAmountText,
                    onValueChange = { targetAmountText = it },
                    label = { Text(if (isZh) "目標金額 ($)" else "Target Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = initialAmountText,
                    onValueChange = { initialAmountText = it },
                    label = { Text(if (isZh) "初始已存金額 ($)" else "Initial Saved Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val trimmed = title.trim()
                    val target = targetAmountText.toDoubleOrNull() ?: 0.0
                    val initial = initialAmountText.toDoubleOrNull() ?: 0.0
                    if (trimmed.isBlank() || target <= 0) {
                        isError = true
                    } else {
                        onConfirm(trimmed, target, initial)
                        onDismissRequest()
                    }
                }
            ) {
                Text(AppStrings.get("confirm", language))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(AppStrings.get("cancel", language))
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MoveDateDialog(
    transaction: Transaction,
    language: AppLanguage,
    onDismissRequest: () -> Unit,
    onConfirmMove: (newDateMillis: Long) -> Unit
) {
    val isZh = language == AppLanguage.ZH
    var showDatePicker by remember { mutableStateOf(false) }

    val currentFormatted = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date(transaction.date))

    val yesterdayCal = Calendar.getInstance().apply { timeInMillis = transaction.date; add(Calendar.DAY_OF_YEAR, -1) }
    val dayBeforeCal = Calendar.getInstance().apply { timeInMillis = transaction.date; add(Calendar.DAY_OF_YEAR, -2) }
    val tomorrowCal = Calendar.getInstance().apply { timeInMillis = transaction.date; add(Calendar.DAY_OF_YEAR, 1) }

    val yesterdayStr = SimpleDateFormat("MM/dd", Locale.getDefault()).format(yesterdayCal.time)
    val dayBeforeStr = SimpleDateFormat("MM/dd", Locale.getDefault()).format(dayBeforeCal.time)
    val tomorrowStr = SimpleDateFormat("MM/dd", Locale.getDefault()).format(tomorrowCal.time)

    val itemLabel = AppStrings.getCategoryName(transaction.category, language)
    val amountFormatted = String.format(Locale.getDefault(), "%,.0f", transaction.amount)

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = if (isZh) "📅 移動『$itemLabel $$amountFormatted』紀錄日期" else "Move Transaction Date",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = if (isZh) "當前紀錄日期：$currentFormatted" else "Current Date: $currentFormatted",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = if (isZh) "快捷選擇目標日期：" else "Quick Select Target Date:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = false,
                        onClick = {
                            onConfirmMove(yesterdayCal.timeInMillis)
                            onDismissRequest()
                        },
                        label = { Text(if (isZh) "前一天 ($yesterdayStr)" else "Yesterday ($yesterdayStr)") }
                    )

                    FilterChip(
                        selected = false,
                        onClick = {
                            onConfirmMove(dayBeforeCal.timeInMillis)
                            onDismissRequest()
                        },
                        label = { Text(if (isZh) "前兩天 ($dayBeforeStr)" else "2 Days Ago ($dayBeforeStr)") }
                    )

                    FilterChip(
                        selected = false,
                        onClick = {
                            onConfirmMove(tomorrowCal.timeInMillis)
                            onDismissRequest()
                        },
                        label = { Text(if (isZh) "後一天 ($tomorrowStr)" else "Tomorrow ($tomorrowStr)") }
                    )
                }

                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isZh) "📅 選擇任意指定日期..." else "Select Any Date...")
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(AppStrings.get("cancel", language))
            }
        }
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = transaction.date)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            onConfirmMove(it)
                        }
                        showDatePicker = false
                        onDismissRequest()
                    }
                ) {
                    Text(AppStrings.get("confirm", language))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(AppStrings.get("cancel", language))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun DateHeaderItem(
    group: DateGroup,
    language: AppLanguage,
    isHovered: Boolean = false,
    onBoundsMeasured: (Rect) -> Unit = {}
) {
    val subtotalLabel = AppStrings.get("subtotal", language)
    val containerColor = when {
        isHovered -> MaterialTheme.colorScheme.primary
        group.isToday -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val textColor = when {
        isHovered -> MaterialTheme.colorScheme.onPrimary
        group.isToday -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { coordinates ->
                onBoundsMeasured(coordinates.boundsInWindow())
            }
            .padding(top = 10.dp, bottom = 2.dp, start = 4.dp, end = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = containerColor
        ) {
            Text(
                text = if (isHovered) "✨ 放下移至此日期 (${group.dateKey})" else group.displayDate,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = textColor,
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
    isLivingExpensePoolEnabled: Boolean = false,
    livingExpensePool: Double = 0.0,
    language: AppLanguage,
    onOpenBudgetSettings: () -> Unit,
    onOpenMonthlyBreakdown: () -> Unit
) {
    val pageCount = if (isLivingExpensePoolEnabled) 3 else 2
    val pagerState = rememberPagerState { pageCount }

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
        onClick = onOpenMonthlyBreakdown,
        modifier = Modifier.fillMaxWidth(),
        shape = OrganicShapeLarge,
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
                when (page) {
                    0 -> MonthlyBudgetView(
                        totalBudget = totalBudget,
                        totalExpense = totalExpense,
                        remainingBudget = remainingBudget,
                        progress = progress,
                        isOverBudget = isOverBudget,
                        isNearLimit = isNearLimit,
                        language = language,
                        onOpenBudgetSettings = onOpenBudgetSettings
                    )
                    1 -> DailyBudgetView(
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
                    else -> LivingExpensePoolView(
                        totalBudget = totalBudget,
                        todayExpense = todayExpense,
                        livingExpensePool = livingExpensePool,
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
                repeat(pageCount) { index ->
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
    isLivingExpensePoolEnabled: Boolean = false,
    livingExpensePool: Double = 0.0,
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
        if (isLivingExpensePoolEnabled && livingExpensePool != 0.0) {
            val isDeficit = livingExpensePool < 0
            val poolText = if (isDeficit) {
                val amt = String.format(Locale.getDefault(), "%,.0f", -livingExpensePool)
                if (language == AppLanguage.ZH) "💰 生活費預算池 (透支赤字): -$amt" else "💰 Living Expense Pool (Deficit): -$amt"
            } else {
                val amt = String.format(Locale.getDefault(), "%,.0f", livingExpensePool)
                if (language == AppLanguage.ZH) "💰 生活費累積池 (過往天數未花完): +$amt" else "💰 Living Expense Pool (Rollover): +$amt"
            }
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isDeficit) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = poolText,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isDeficit) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
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
    isLivingExpensePoolEnabled: Boolean = false,
    livingExpensePool: Double = 0.0,
    language: AppLanguage,
    onOpenBudgetSettings: () -> Unit
) {
    val animatedProgress by animateFloatAsState(targetValue = todayProgress, label = "dailyProgress")

    val progressColor by animateColorAsState(
        targetValue = when {
            isTodayOver -> MaterialTheme.colorScheme.error
            todayProgress >= 0.8f -> MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
            else -> MaterialTheme.colorScheme.primary
        },
        label = "dailyProgressColor"
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (isLivingExpensePoolEnabled && livingExpensePool != 0.0) {
            val isDeficit = livingExpensePool < 0
            val poolText = if (isDeficit) {
                val amt = String.format(Locale.getDefault(), "%,.0f", -livingExpensePool)
                if (language == AppLanguage.ZH) "💰 生活費預算池 (透支赤字): -$amt" else "💰 Living Expense Pool (Deficit): -$amt"
            } else {
                val amt = String.format(Locale.getDefault(), "%,.0f", livingExpensePool)
                if (language == AppLanguage.ZH) "💰 生活費預算池 (過往天數未花完): +$amt" else "💰 Living Expense Pool (Rollover): +$amt"
            }
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isDeficit) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = poolText,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isDeficit) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
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
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape
                ) {
                    Text(
                        text = AppStrings.get("today_ample", language),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
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
private fun LivingExpensePoolView(
    totalBudget: Double,
    todayExpense: Double,
    livingExpensePool: Double,
    language: AppLanguage,
    onOpenBudgetSettings: () -> Unit
) {
    val isZh = language == AppLanguage.ZH
    val isDeficit = livingExpensePool < 0

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Savings,
                    contentDescription = null,
                    tint = if (isDeficit) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isZh) "生活費預算池 (Beta)" else "Living Expense Pool",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Surface(
                color = if (isDeficit) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                shape = CircleShape
            ) {
                Text(
                    text = if (isDeficit) (if (isZh) "赤字透支" else "Overspent") else (if (isZh) "滾存積累" else "Rollover Pool"),
                    color = if (isDeficit) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
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
                        text = if (isDeficit)
                            (if (isZh) "累積透支生活費" else "Overspent Deficit")
                        else
                            (if (isZh) "過往天數累積可用生活費" else "Cumulative Living Expenses"),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val poolAmountFormatted = if (isDeficit)
                        "-$${String.format(Locale.getDefault(), "%,.0f", -livingExpensePool)}"
                    else
                        "$${String.format(Locale.getDefault(), "%,.0f", livingExpensePool)}"

                    Text(
                        text = poolAmountFormatted,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isDeficit) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (isZh) "配額狀態" else "Status",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (isDeficit)
                            (if (isZh) "已超額透支" else "Over Limit")
                        else
                            (if (isZh) "前段天數省下即滾存" else "Past Unspent Rollover"),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isDeficit) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isDeficit) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                val hintText = if (isDeficit) {
                    val deficitFormatted = String.format(Locale.getDefault(), "%,.0f", -livingExpensePool)
                    if (isZh)
                        "⚠️ 生活費預算池目前已透支赤字 -$deficitFormatted，請注意控制後續日常開銷！"
                    else
                        "⚠️ Living Expense Pool is currently overspent by -$deficitFormatted."
                } else {
                    if (isZh)
                        "💡 前幾天未用完的每日預算已自動滾存累積為「生活費」，可用於靈活扣抵日常消費！"
                    else
                        "💡 Unspent budget from past days is accumulated into this Living Expense pool."
                }

                Text(
                    text = hintText,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDeficit) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(10.dp)
                )
            }
        } else {
            Text(
                text = if (isZh) "點擊「設定預算」規劃您的本月開銷上限，系統將自動為您解鎖生活費滾存池！" else "Tap 'Set Budget' to set your monthly spending limit and unlock Living Expense Pool tracking!",
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
    isDragDateReorderEnabled: Boolean = false,
    onStartDrag: () -> Unit = {},
    onDrag: (Offset) -> Unit = {},
    onEndDrag: () -> Unit = {},
    onCancelDrag: () -> Unit = {},
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

    val isIncome = transaction.type == TransactionType.INCOME
    val amountColor = if (isIncome) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    val amountPrefix = if (isIncome) "+" else "-"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(transaction, isDragDateReorderEnabled) {
                if (isDragDateReorderEnabled) {
                    detectDragGesturesAfterLongPress(
                        onDragStart = { onStartDrag() },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            onDrag(dragAmount)
                        },
                        onDragEnd = { onEndDrag() },
                        onDragCancel = { onCancelDrag() }
                    )
                }
            }
    ) {
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
                            color = if (isIncome) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getCategoryIcon(transaction.category),
                        contentDescription = localizedCategory,
                        tint = if (isIncome) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = localizedCategory,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        if (transaction.deductFromPool) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.tertiaryContainer
                            ) {
                                Text(
                                    text = if (language == AppLanguage.ZH) "💰 生活費" else "💰 Pool",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        if (transaction.accountName.isNotBlank() && transaction.accountName != "現金") {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    text = transaction.accountName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = noteDisplay,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$amountPrefix$${String.format(Locale.getDefault(), "%,.0f", transaction.amount)}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = amountColor
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
