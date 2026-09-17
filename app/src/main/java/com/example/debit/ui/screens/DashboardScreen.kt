package com.example.debit.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import com.example.debit.ui.utils.getCategoryIcon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.debit.R
import com.example.debit.data.AppLanguage
import com.example.debit.data.Subscription
import com.example.debit.data.Transaction
import com.example.debit.data.TransactionType
import com.example.debit.ui.DebitViewModel
import androidx.compose.ui.draw.clip
import com.example.debit.ui.components.ExpensePieChartCard
import com.example.debit.ui.components.ExpenseTrendLineChartCard
import com.example.debit.ui.components.Modern3DAllocationCard
import com.example.debit.ui.components.Modern3DSavingsHeroCard
import com.example.debit.ui.components.Modern3DStatRow
import com.example.debit.ui.components.Modern3DTransactionItem
import com.example.debit.ui.dialogs.AddTransactionDialog
import com.example.debit.ui.dialogs.BudgetSettingsDialog
import com.example.debit.ui.dialogs.MonthSelectorDialog
import com.example.debit.ui.dialogs.MonthlyBreakdownDialog
import com.example.debit.ui.utils.AppStrings
import com.example.debit.ui.utils.BackupUtils
import com.example.debit.ui.utils.ExportUtils
import com.example.debit.ui.utils.OrganicShapeChip
import com.example.debit.ui.utils.OrganicShapeLarge
import com.example.debit.ui.utils.OrganicShapeMedium
import com.example.debit.ui.utils.bouncyClickable
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

data class DateGroup(
    val dateKey: String,
    val displayDate: String,
    val totalDailyExpense: Double,
    val transactions: List<Transaction>
)

fun groupTransactionsByDate(
    transactions: List<Transaction>,
    language: AppLanguage
): List<DateGroup> {
    val grouped = transactions.groupBy { tx ->
        val cal = Calendar.getInstance().apply { timeInMillis = tx.date }
        val y = cal.get(Calendar.YEAR)
        val m = cal.get(Calendar.MONTH) + 1
        val d = cal.get(Calendar.DAY_OF_MONTH)
        String.format(Locale.US, "%04d-%02d-%02d", y, m, d)
    }

    return grouped.map { (dateKey, list) ->
        val sum = list.sumOf { if (it.type == TransactionType.EXPENSE) it.amount else -it.amount }
        val firstMillis = list.firstOrNull()?.date ?: System.currentTimeMillis()
        val displayStr = formatDisplayDateHeader(firstMillis, language)
        DateGroup(
            dateKey = dateKey,
            displayDate = displayStr,
            totalDailyExpense = sum,
            transactions = list
        )
    }.sortedByDescending { it.dateKey }
}

fun formatDisplayDateHeader(timeMillis: Long, language: AppLanguage): String {
    val nowCal = Calendar.getInstance()
    val targetCal = Calendar.getInstance().apply { timeInMillis = timeMillis }

    val isToday = nowCal.get(Calendar.YEAR) == targetCal.get(Calendar.YEAR) &&
            nowCal.get(Calendar.DAY_OF_YEAR) == targetCal.get(Calendar.DAY_OF_YEAR)

    val tempCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
    val isYesterday = tempCal.get(Calendar.YEAR) == targetCal.get(Calendar.YEAR) &&
            tempCal.get(Calendar.DAY_OF_YEAR) == targetCal.get(Calendar.DAY_OF_YEAR)

    val isZh = language == AppLanguage.ZH
    val prefix = when {
        isToday -> if (isZh) "今天 " else "Today "
        isYesterday -> if (isZh) "昨日 " else "Yesterday "
        else -> ""
    }

    return if (isZh) {
        val pattern = SimpleDateFormat("MM月dd日 EEEE", Locale.TAIWAN)
        prefix + pattern.format(Date(timeMillis))
    } else {
        val pattern = SimpleDateFormat("MMM dd, EEEE", Locale.US)
        prefix + pattern.format(Date(timeMillis))
    }
}

fun parseDateKeyToMillis(dateKey: String, originalMillis: Long): Long {
    val parts = dateKey.split("-")
    if (parts.size != 3) return originalMillis
    val y = parts[0].toIntOrNull() ?: return originalMillis
    val m = parts[1].toIntOrNull() ?: return originalMillis
    val d = parts[2].toIntOrNull() ?: return originalMillis

    val calendar = Calendar.getInstance().apply { timeInMillis = originalMillis }
    calendar.set(Calendar.YEAR, y)
    calendar.set(Calendar.MONTH, m - 1)
    calendar.set(Calendar.DAY_OF_MONTH, d)
    return calendar.timeInMillis
}

fun findClosestDateHeaderKey(
    touchWindowY: Float,
    boundsMap: Map<String, Rect>
): String? {
    var closestKey: String? = null
    var minDistance = Float.MAX_VALUE

    for ((key, rect) in boundsMap) {
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

    var selectedBottomNavIndex by remember { mutableIntStateOf(0) }

    var showAddDialog by remember { mutableStateOf(false) }
    var showMonthSelectorDialog by remember { mutableStateOf(false) }
    var showMonthlyBreakdownDialog by remember { mutableStateOf(false) }
    var showAddSubscriptionDialog by remember { mutableStateOf(false) }
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
                        onClick = { selectedBottomNavIndex = 3 },
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
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                tonalElevation = 6.dp
            ) {
                NavigationBarItem(
                    selected = selectedBottomNavIndex == 0,
                    onClick = { selectedBottomNavIndex = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = if (isZh) "概覽" else "Overview") },
                    label = { Text(if (isZh) "概覽" else "Overview", fontWeight = FontWeight.Bold) }
                )
                NavigationBarItem(
                    selected = selectedBottomNavIndex == 1,
                    onClick = { selectedBottomNavIndex = 1 },
                    icon = { Icon(Icons.Default.PieChart, contentDescription = if (isZh) "統計" else "Analytics") },
                    label = { Text(if (isZh) "統計" else "Analytics", fontWeight = FontWeight.Bold) }
                )
                NavigationBarItem(
                    selected = selectedBottomNavIndex == 2,
                    onClick = { selectedBottomNavIndex = 2 },
                    icon = { Icon(Icons.Default.Savings, contentDescription = if (isZh) "規劃" else "Planning") },
                    label = { Text(if (isZh) "規劃" else "Planning", fontWeight = FontWeight.Bold) }
                )
                NavigationBarItem(
                    selected = selectedBottomNavIndex == 3,
                    onClick = { selectedBottomNavIndex = 3 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = if (isZh) "設定" else "Settings") },
                    label = { Text(if (isZh) "設定" else "Settings", fontWeight = FontWeight.Bold) }
                )
            }
        },
        floatingActionButton = {
            if (selectedBottomNavIndex == 0 || selectedBottomNavIndex == 2) {
                ExtendedFloatingActionButton(
                    onClick = { showAddDialog = true },
                    icon = { Icon(Icons.Default.Add, contentDescription = AppStrings.get("add_record", lang)) },
                    text = { Text(AppStrings.get("add_record", lang), fontWeight = FontWeight.Bold) },
                    shape = OrganicShapeChip,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = selectedBottomNavIndex,
            transitionSpec = {
                if (targetState > initialState) {
                    (slideInHorizontally { width -> width / 3 } + fadeIn(tween(250))) togetherWith
                            (slideOutHorizontally { width -> -width / 3 } + fadeOut(tween(250)))
                } else {
                    (slideInHorizontally { width -> -width / 3 } + fadeIn(tween(250))) togetherWith
                            (slideOutHorizontally { width -> width / 3 } + fadeOut(tween(250)))
                }.using(SizeTransform(clip = false))
            },
            label = "BottomNavTabTransition",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) { tabIndex ->
            when (tabIndex) {
                0 -> {
                    // Tab 0: Overview & Transactions
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
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

                        // Hero Budget Card
                        if (uiState.isModern3DUiEnabled) {
                            item {
                                Modern3DSavingsHeroCard(
                                    totalBudget = uiState.totalBudgetLimit,
                                    totalExpense = uiState.totalExpense,
                                    remainingBudget = uiState.remainingBudget,
                                    language = lang,
                                    onOpenMonthlyBreakdown = { showMonthlyBreakdownDialog = true }
                                )
                            }
                        } else {
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
                                    onOpenBudgetSettings = { selectedBottomNavIndex = 3 },
                                    onOpenMonthlyBreakdown = { showMonthlyBreakdownDialog = true }
                                )
                            }
                        }

                        // Recent Transactions Section Header
                        item {
                            Text(
                                text = AppStrings.get("records_breakdown", lang),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        // Grouped Transactions By Date
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
                                                    }
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
                                                    }
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
                    }
                }

                1 -> {
                    // Tab 1: Analytics & Charts
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item { Spacer(modifier = Modifier.height(2.dp)) }

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
                        } else {
                            item {
                                EmptyTransactionsCard(language = lang, onAddClick = { showAddDialog = true })
                            }
                        }

                        // Trend Line Chart Card
                        item {
                            ExpenseTrendLineChartCard(
                                transactions = uiState.transactions,
                                language = lang
                            )
                        }

                        // Net Savings Overview Card (Beta Feature 1)
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

                        if (uiState.isModern3DUiEnabled) {
                            item {
                                Modern3DStatRow(
                                    totalExpense = uiState.totalExpense,
                                    totalBudget = uiState.totalBudgetLimit,
                                    remainingBudget = uiState.remainingBudget,
                                    language = lang
                                )
                            }

                            item {
                                Modern3DAllocationCard(
                                    categoryExpenses = uiState.categoryExpenses,
                                    totalExpense = uiState.totalExpense,
                                    language = lang
                                )
                            }
                        }
                    }
                }

                2 -> {
                    // Tab 2: Planning (Subscriptions & Savings Goals)
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        item { Spacer(modifier = Modifier.height(4.dp)) }

                        // Subscriptions Card
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                            ) {
                                Column(
                                    modifier = Modifier.padding(18.dp),
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = if (lang == AppLanguage.ZH) "🔁 訂閱與定期扣款" else "🔁 Subscriptions",
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = if (lang == AppLanguage.ZH) "月估算總額: $${String.format(Locale.getDefault(), "%,.0f", uiState.totalSubscriptionsMonthly)}/月" else "Est. Monthly: $${String.format(Locale.getDefault(), "%,.0f", uiState.totalSubscriptionsMonthly)}/mo",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        FilledTonalButton(
                                            onClick = { showAddSubscriptionDialog = true },
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text(
                                                text = if (lang == AppLanguage.ZH) "➕ 新增扣款" else "➕ Add",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                        }
                                    }

                                    if (uiState.subscriptions.isEmpty()) {
                                        Surface(
                                            shape = RoundedCornerShape(14.dp),
                                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(20.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = if (lang == AppLanguage.ZH) "尚無固定訂閱或定期扣款項目" else "No active subscriptions",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    } else {
                                        uiState.subscriptions.forEach { sub ->
                                            val cycleTag = if (sub.isAnnual) (if (lang == AppLanguage.ZH) "年繳" else "Annual") else (if (lang == AppLanguage.ZH) "月繳" else "Monthly")
                                            val dateDetailStr = if (sub.isAnnual) {
                                                if (lang == AppLanguage.ZH) "每年 ${sub.billingMonth} 月 ${sub.billingDay} 日扣款" else "Annual billing on ${sub.billingMonth}/${sub.billingDay}"
                                            } else {
                                                if (lang == AppLanguage.ZH) "每月 ${sub.billingDay} 日扣款" else "Monthly billing on day ${sub.billingDay}"
                                            }
                                            val amountText = if (sub.isAnnual) {
                                                val monthlyAvg = sub.amount / 12.0
                                                "$${String.format(Locale.getDefault(), "%,.0f", sub.amount)}/年 (約 $${String.format(Locale.getDefault(), "%,.0f", monthlyAvg)}/月)"
                                            } else {
                                                "$${String.format(Locale.getDefault(), "%,.0f", sub.amount)}/月"
                                            }

                                            Surface(
                                                shape = RoundedCornerShape(16.dp),
                                                color = MaterialTheme.colorScheme.surfaceContainerLow,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(16.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column(
                                                        modifier = Modifier.weight(1f),
                                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Surface(
                                                                shape = RoundedCornerShape(6.dp),
                                                                color = if (sub.isAnnual) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.secondaryContainer
                                                            ) {
                                                                Text(
                                                                    text = cycleTag,
                                                                    style = MaterialTheme.typography.labelSmall,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = if (sub.isAnnual) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSecondaryContainer,
                                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                                )
                                                            }
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            Text(
                                                                text = sub.name,
                                                                style = MaterialTheme.typography.titleMedium,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }
                                                        Text(
                                                            text = dateDetailStr,
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                        Text(
                                                            text = amountText,
                                                            style = MaterialTheme.typography.titleMedium,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.primary
                                                        )
                                                    }

                                                    IconButton(
                                                        onClick = { viewModel.deleteSubscription(sub) },
                                                        modifier = Modifier.size(36.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Delete,
                                                            contentDescription = "刪除",
                                                            tint = MaterialTheme.colorScheme.error,
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                3 -> {
                    // Tab 3: Settings Screen
                    SettingsScreen(
                        initialTotalBudget = uiState.totalBudgetLimit,
                        currentThemeColor = uiState.themeColor,
                        currentLanguage = uiState.appLanguage,
                        currentBetaTestingEnabled = uiState.isBetaTestingEnabled,
                        currentGooglePayListenerEnabled = uiState.isGooglePayListenerEnabled,
                        currentDailyReminderEnabled = uiState.isDailyReminderEnabled,
                        currentReminderHour = uiState.reminderHour,
                        currentReminderMinute = uiState.reminderMinute,
                        currentIncomeTrackingEnabled = uiState.isIncomeTrackingEnabled,
                        currentSearchEnabled = uiState.isSearchEnabled,
                        currentSubscriptionEnabled = uiState.isSubscriptionEnabled,
                        currentMultiAccountEnabled = uiState.isMultiAccountEnabled,
                        currentModern3DUiEnabled = uiState.isModern3DUiEnabled,
                        currentDragDateReorderEnabled = uiState.isDragDateReorderEnabled,
                        currentAutoBackupEnabled = uiState.autoBackupEnabled,
                        lastAutoBackupTime = uiState.lastAutoBackupTime,
                        isEmbedded = true,
                        onDailyReminderChange = { enabled, h, m ->
                            viewModel.setDailyReminder(enabled, h, m)
                        },
                        onSaveSettings = { budgetLimit, color, language, betaTesting, gPay, incomeTracking, search, subscription, multiAccount, modern3D, dragDateReorder, autoBackup ->
                            viewModel.saveSettings(
                                budgetLimit, color, language, betaTesting, gPay,
                                incomeTracking, search, subscription, multiAccount, modern3D, dragDateReorder, autoBackup
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
                        onReplayOnboarding = {
                            viewModel.resetOnboarding()
                        },
                        onDismissRequest = { selectedBottomNavIndex = 0 }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddTransactionDialog(
            language = lang,
            initialTransaction = null,
            isMultiAccountEnabled = uiState.isMultiAccountEnabled,
            isIncomeTrackingEnabled = uiState.isIncomeTrackingEnabled,
            onDismissRequest = { showAddDialog = false },
            onConfirm = { amount, category, note, date, locationName, deductFromPool, accountName, type ->
                viewModel.addTransaction(
                    amount = amount,
                    category = category,
                    note = note,
                    date = date,
                    locationName = locationName,
                    accountName = accountName,
                    type = type
                )
            }
        )
    }

    if (editingTransaction != null) {
        AddTransactionDialog(
            language = lang,
            initialTransaction = editingTransaction,
            isMultiAccountEnabled = uiState.isMultiAccountEnabled,
            isIncomeTrackingEnabled = uiState.isIncomeTrackingEnabled,
            onDismissRequest = { editingTransaction = null },
            onConfirm = { amount, category, note, date, locationName, deductFromPool, accountName, type ->
                viewModel.updateTransaction(
                    id = editingTransaction!!.id,
                    amount = amount,
                    category = category,
                    note = note,
                    date = date,
                    locationName = locationName,
                    accountName = accountName,
                    type = type
                )
                editingTransaction = null
            }
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

    if (showAddSubscriptionDialog) {
        AddSubscriptionDialog(
            language = lang,
            onDismissRequest = { showAddSubscriptionDialog = false },
            onSaveSubscription = { name, amount, billingDay, billingMonth, isAnnual ->
                viewModel.addSubscription(
                    name = name,
                    amount = amount,
                    billingDay = billingDay,
                    billingMonth = billingMonth,
                    isAnnual = isAnnual
                )
            }
        )
    }
}

@Composable
fun DateHeaderItem(
    group: DateGroup,
    language: AppLanguage,
    isHovered: Boolean,
    onBoundsMeasured: (Rect) -> Unit
) {
    val backgroundColor = if (isHovered)
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
    else
        MaterialTheme.colorScheme.surface

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp)
            .onGloballyPositioned { coordinates ->
                onBoundsMeasured(coordinates.boundsInWindow())
            },
        color = backgroundColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Text(
                    text = group.displayDate,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            val sign = if (group.totalDailyExpense > 0) "-" else "+"
            val absAmount = abs(group.totalDailyExpense)
            Text(
                text = "${AppStrings.get("daily_subtotal", language)}: $sign$${String.format(Locale.getDefault(), "%,.0f", absAmount)}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (group.totalDailyExpense > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun TransactionItem(
    transaction: Transaction,
    language: AppLanguage,
    isDragDateReorderEnabled: Boolean = true,
    onStartDrag: () -> Unit = {},
    onDrag: (Offset) -> Unit = {},
    onEndDrag: () -> Unit = {},
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isExpense = transaction.type == TransactionType.EXPENSE
    val iconBgColor = if (isExpense) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
    val iconTintColor = if (isExpense) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
    val amountPrefix = if (isExpense) "-" else "+"
    val amountColor = if (isExpense) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary

    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val timeStr = timeFormat.format(Date(transaction.date))

    val modifierWithDrag = if (isDragDateReorderEnabled) {
        Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { onStartDrag() },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        onDrag(dragAmount)
                    },
                    onDragEnd = { onEndDrag() },
                    onDragCancel = { onEndDrag() }
                )
            }
    } else {
        Modifier.fillMaxWidth()
    }

    Card(
        modifier = modifierWithDrag,
        shape = OrganicShapeMedium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = iconBgColor,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = getCategoryIcon(transaction.category),
                            contentDescription = transaction.category,
                            tint = iconTintColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    val localizedCategory = AppStrings.getCategoryName(transaction.category, language)
                    val localizedNote = transaction.note.split(" • ").joinToString(" • ") { part ->
                        AppStrings.getSubCategoryName(part, language)
                    }

                    Text(
                        text = localizedCategory,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (localizedNote.isNotBlank()) {
                            Text(
                                text = "$localizedNote • $timeStr",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text(
                                text = timeStr,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "$amountPrefix$${String.format(Locale.getDefault(), "%,.0f", transaction.amount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = amountColor
                )

                IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "修改這筆紀錄",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "刪除這筆紀錄",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
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
    onOpenBudgetSettings: () -> Unit,
    onOpenMonthlyBreakdown: () -> Unit
) {
    val isZh = language == AppLanguage.ZH
    val pagerState = rememberPagerState(pageCount = { 2 })

    val calendar = Calendar.getInstance()
    val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val remainingDays = (daysInMonth - currentDay + 1).coerceAtLeast(1)

    val dailyEstimatedUsable = if (remainingBudget > 0) (remainingBudget / remainingDays) else 0.0
    val todayEstimatedRemaining = dailyEstimatedUsable - todayExpense

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .bouncyClickable { onOpenMonthlyBreakdown() },
        shape = OrganicShapeLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth()
            ) { page ->
                when (page) {
                    0 -> {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.AccountBalanceWallet,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = AppStrings.get("vault_overview", language),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = AppStrings.get("spent_this_month", language),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "$${String.format(Locale.getDefault(), "%,.0f", totalExpense)}",
                                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = AppStrings.get("remaining_vault", language),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    val remColor = if (isOverBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                    Text(
                                        text = "$${String.format(Locale.getDefault(), "%,.0f", remainingBudget)}",
                                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                        color = remColor
                                    )
                                }
                            }

                            val barColor = when {
                                isOverBudget -> MaterialTheme.colorScheme.error
                                isNearLimit -> MaterialTheme.colorScheme.tertiary
                                else -> MaterialTheme.colorScheme.primary
                            }

                            LinearProgressIndicator(
                                progress = { progress.coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp),
                                color = barColor,
                                trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${AppStrings.get("vault_usage", language)}: ${(progress * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Text(
                                    text = "${AppStrings.get("target_budget", language)}: $${String.format(Locale.getDefault(), "%,.0f", totalBudget)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    1 -> {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Today,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = if (isZh) "每日配額概況" else "Daily Budget Status",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = if (isZh) "目前已使用" else "Spent Today",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "$${String.format(Locale.getDefault(), "%,.0f", todayExpense)}",
                                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = if (isZh) "今日預估尚可支出" else "Today's Remaining",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    val todayRemColor = if (todayEstimatedRemaining < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary
                                    Text(
                                        text = "$${String.format(Locale.getDefault(), "%,.0f", todayEstimatedRemaining)}",
                                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                        color = todayRemColor
                                    )
                                }
                            }

                            val todayProgress = if (dailyEstimatedUsable > 0) (todayExpense / dailyEstimatedUsable).toFloat().coerceIn(0f, 1f) else 0f
                            val todayBarColor = if (todayExpense > dailyEstimatedUsable) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary

                            LinearProgressIndicator(
                                progress = { todayProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp),
                                color = todayBarColor,
                                trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isZh) "本月還剩 $remainingDays 天" else "$remainingDays days left",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Text(
                                    text = if (isZh) "每日預估可用: $${String.format(Locale.getDefault(), "%,.0f", dailyEstimatedUsable)}/天" else "Daily est: $${String.format(Locale.getDefault(), "%,.0f", dailyEstimatedUsable)}/day",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(2) { index ->
                    val color = if (pagerState.currentPage == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    val width = if (pagerState.currentPage == index) 18.dp else 6.dp
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
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
fun EmptyTransactionsCard(language: AppLanguage, onAddClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = OrganicShapeMedium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Today,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(40.dp)
            )
            Text(
                text = AppStrings.get("no_records_title", language),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = AppStrings.get("no_records_subtitle", language),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedButton(onClick = onAddClick, shape = RoundedCornerShape(12.dp)) {
                Text(AppStrings.get("start_add", language), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AddSubscriptionDialog(
    language: AppLanguage,
    onDismissRequest: () -> Unit,
    onSaveSubscription: (name: String, amount: Double, billingDay: Int, billingMonth: Int, isAnnual: Boolean) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var billingMonthText by remember { mutableStateOf("1") }
    var billingDayText by remember { mutableStateOf("1") }
    var isAnnual by remember { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false) }

    val isZh = language == AppLanguage.ZH

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(if (isZh) "➕ 新增固定訂閱扣款" else "Add Subscription", style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = !isAnnual,
                        onClick = { isAnnual = false },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                    ) {
                        Text(if (isZh) "按月扣款" else "Monthly")
                    }
                    SegmentedButton(
                        selected = isAnnual,
                        onClick = { isAnnual = true },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                    ) {
                        Text(if (isZh) "按年扣款" else "Annual")
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; isError = false },
                    label = { Text(if (isZh) "訂閱服務名稱 (例如 Netflix)" else "Service Name") },
                    singleLine = true,
                    isError = isError,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it; isError = false },
                    label = { Text(if (isZh) "扣款金額 ($)" else "Amount ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = isError,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (isAnnual) {
                        OutlinedTextField(
                            value = billingMonthText,
                            onValueChange = { billingMonthText = it },
                            label = { Text(if (isZh) "扣款月份 (1-12)" else "Month (1-12)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = billingDayText,
                        onValueChange = { billingDayText = it },
                        label = { Text(if (isZh) "扣款日期 (1-31)" else "Day (1-31)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    val day = billingDayText.toIntOrNull()?.coerceIn(1, 31) ?: 1
                    val month = billingMonthText.toIntOrNull()?.coerceIn(1, 12) ?: 1

                    if (name.isNotBlank() && amount > 0) {
                        onSaveSubscription(name, amount, day, month, isAnnual)
                        onDismissRequest()
                    } else {
                        isError = true
                    }
                }
            ) {
                Text(AppStrings.get("save", language))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(AppStrings.get("cancel", language))
            }
        }
    )
}
