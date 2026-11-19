package com.example.debit.ui.screens

import android.app.TimePickerDialog
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.ui.text.font.FontFamily
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
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.graphics.Color
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
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
import androidx.compose.ui.text.style.TextOverflow
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
import com.example.debit.ui.dialogs.HelpDialog
import com.example.debit.ui.dialogs.MonthSelectorDialog
import com.example.debit.ui.dialogs.MonthlyBreakdownDialog
import com.example.debit.ui.utils.AppStrings
import com.example.debit.ui.utils.BackupCompareInfo
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
    var showHelpDialog by remember { mutableStateOf(false) }
    var showMonthSelectorDialog by remember { mutableStateOf(false) }
    var showMonthlyBreakdownDialog by remember { mutableStateOf(false) }
    var showAddSubscriptionDialog by remember { mutableStateOf(false) }
    var editingTransaction by remember { mutableStateOf<Transaction?>(null) }
    var deletingTransaction by remember { mutableStateOf<Transaction?>(null) }
    var deletingSubscription by remember { mutableStateOf<Subscription?>(null) }
    var movingDateTransaction by remember { mutableStateOf<Transaction?>(null) }

    var draggingTransaction by remember { mutableStateOf<Transaction?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var hoveredDateKey by remember { mutableStateOf<String?>(null) }
    var draggedItemRect by remember { mutableStateOf<Rect?>(null) }
    val dateHeaderBoundsMap = remember { mutableStateMapOf<String, Rect>() }

    var pendingBackupInfo by remember { mutableStateOf<BackupCompareInfo?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                context.contentResolver.openInputStream(it)?.use { stream ->
                    val jsonContent = stream.bufferedReader().readText()
                    val compareInfo = BackupUtils.parseCompareInfo(jsonContent, uiState.allTransactions.ifEmpty { uiState.transactions })
                    pendingBackupInfo = compareInfo
                }
            } catch (_: Exception) {
                Toast.makeText(context, if (isZh) "無法讀取選取的備份檔案" else "Failed to read backup file", Toast.LENGTH_SHORT).show()
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
            if (selectedBottomNavIndex == 3) {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "Settings",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(22.dp)
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
                    actions = {
                        IconButton(onClick = { showHelpDialog = true }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                                contentDescription = if (isZh) "說明" else "Help",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            } else {
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
            }
        },
        bottomBar = {
            val navItemColors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                tonalElevation = 6.dp
            ) {
                NavigationBarItem(
                    selected = selectedBottomNavIndex == 0,
                    onClick = { selectedBottomNavIndex = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = if (isZh) "概覽" else "Overview") },
                    label = { Text(if (isZh) "概覽" else "Overview", fontWeight = FontWeight.Bold) },
                    colors = navItemColors
                )
                NavigationBarItem(
                    selected = selectedBottomNavIndex == 1,
                    onClick = { selectedBottomNavIndex = 1 },
                    icon = { Icon(Icons.Default.PieChart, contentDescription = if (isZh) "統計" else "Analytics") },
                    label = { Text(if (isZh) "統計" else "Analytics", fontWeight = FontWeight.Bold) },
                    colors = navItemColors
                )
                NavigationBarItem(
                    selected = selectedBottomNavIndex == 2,
                    onClick = { selectedBottomNavIndex = 2 },
                    icon = { Icon(Icons.Default.Savings, contentDescription = if (isZh) "規劃" else "Planning") },
                    label = { Text(if (isZh) "規劃" else "Planning", fontWeight = FontWeight.Bold) },
                    colors = navItemColors
                )
                NavigationBarItem(
                    selected = selectedBottomNavIndex == 3,
                    onClick = { selectedBottomNavIndex = 3 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = if (isZh) "設定" else "Settings") },
                    label = { Text(if (isZh) "設定" else "Settings", fontWeight = FontWeight.Bold) },
                    colors = navItemColors
                )
            }
        },
        floatingActionButton = {
            if (selectedBottomNavIndex != 3) {
                ExtendedFloatingActionButton(
                    onClick = { showAddDialog = true },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = if (isZh) "記一筆" else "Add Record",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    },
                    text = {
                        Text(
                            text = if (isZh) "記一筆" else "Add",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    },
                    shape = RoundedCornerShape(28.dp),
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

                        // Hero Budget Card (Style A: Linear / Apple Wallet Fintech Hero Asset Card)
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
                                            onDelete = {
                                                if (uiState.isConfirmDeleteEnabled) {
                                                    deletingTransaction = transaction
                                                } else {
                                                    viewModel.deleteTransaction(transaction)
                                                }
                                            }
                                        )
                                    }
                                }
                            }

                            // Bottom spacer so FAB "記一筆" button doesn't block bottom items
                            item {
                                Spacer(modifier = Modifier.height(96.dp))
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
                                    language = lang,
                                    selectedYearMonth = uiState.currentYearMonth
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
                                language = lang,
                                selectedYearMonth = uiState.currentYearMonth
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
                    // Tab 2: Planning (Subscriptions & Savings Goals) - Taste Skill Redesign
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item { Spacer(modifier = Modifier.height(2.dp)) }

                        // 1. Hero Monthly Fixed Cashflow Projection Card
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF121714)),
                                border = BorderStroke(1.dp, Color(0xFF1F2923))
                            ) {
                                Column(
                                    modifier = Modifier.padding(18.dp),
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    // Header: Title + Add Button
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = if (lang == AppLanguage.ZH) "固定收支與定期扣款" else "Recurring Cash Flow",
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = if (lang == AppLanguage.ZH) "自動掌握每月例行開銷、帳單與收入" else "Manage recurring bills & salary",
                                                fontSize = 12.sp,
                                                color = Color(0xFF94A3B8)
                                            )
                                        }

                                        Surface(
                                            shape = CircleShape,
                                            color = Color(0xFF184E38),
                                            border = BorderStroke(1.dp, Color(0xFF2DD4BF).copy(alpha = 0.35f)),
                                            modifier = Modifier
                                                .clickable { showAddSubscriptionDialog = true }
                                                .padding(start = 8.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Add,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(15.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = if (lang == AppLanguage.ZH) "新增項目" else "Add Item",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }

                                    // Split Cashflow Metrics Row (Two Clean Cards)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        // Left: Fixed Income
                                        Surface(
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(14.dp),
                                            color = Color(0xFF15221B),
                                            border = BorderStroke(1.dp, Color(0xFF1F3A2C))
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(12.dp),
                                                verticalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text(
                                                    text = if (lang == AppLanguage.ZH) "💵 固定月收入" else "Income / mo",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Color(0xFF6EE7B7)
                                                )
                                                Text(
                                                    text = "+$${String.format(Locale.getDefault(), "%,.0f", uiState.totalFixedIncomeMonthly)}",
                                                    fontSize = 20.sp,
                                                    fontWeight = FontWeight.Black,
                                                    fontFamily = FontFamily.Monospace,
                                                    color = Color(0xFF10B981)
                                                )
                                            }
                                        }

                                        // Right: Fixed Expense
                                        Surface(
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(14.dp),
                                            color = Color(0xFF26181B),
                                            border = BorderStroke(1.dp, Color(0xFF3F2026))
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(12.dp),
                                                verticalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text(
                                                    text = if (lang == AppLanguage.ZH) "💸 固定月支出" else "Expense / mo",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Color(0xFFFDA4AF)
                                                )
                                                Text(
                                                    text = "-$${String.format(Locale.getDefault(), "%,.0f", uiState.totalFixedExpenseMonthly)}",
                                                    fontSize = 20.sp,
                                                    fontWeight = FontWeight.Black,
                                                    fontFamily = FontFamily.Monospace,
                                                    color = Color(0xFFFB7185)
                                                )
                                            }
                                        }
                                    }

                                    // Net Projection Banner
                                    val netFixedMonthly = uiState.totalFixedIncomeMonthly - uiState.totalFixedExpenseMonthly
                                    val netSign = if (netFixedMonthly >= 0) "+" else ""
                                    val netColor = if (netFixedMonthly >= 0) Color(0xFF10B981) else Color(0xFFFB7185)

                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFF18201C),
                                        border = BorderStroke(1.dp, Color(0xFF233028)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 9.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = if (lang == AppLanguage.ZH) "預估每月固定淨結餘" else "Estimated Net Cash Flow",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFF94A3B8)
                                            )
                                            Text(
                                                text = "$netSign$${String.format(Locale.getDefault(), "%,.0f", netFixedMonthly)} /月",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace,
                                                color = netColor
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 2. Subscriptions & Recurring List Section
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (lang == AppLanguage.ZH) "定期扣款與固定收入項目 (${uiState.subscriptions.size})" else "Recurring Items (${uiState.subscriptions.size})",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFCBD5E1)
                                )
                            }
                        }

                        if (uiState.subscriptions.isEmpty()) {
                            item {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color(0xFF121714),
                                    border = BorderStroke(1.dp, Color(0xFF1F2923)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF162E22)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Repeat,
                                                contentDescription = null,
                                                tint = Color(0xFF10B981),
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = if (lang == AppLanguage.ZH) "尚無固定收支或定期扣款項目" else "No recurring cash flow items",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = if (lang == AppLanguage.ZH)
                                                "設定房租、水電費、電信費、串流影音等例行性收支，系統將於指定扣款日自動掌握，精準計算每日剩餘可用額度。"
                                            else
                                                "Track recurring bills and salary to automatically forecast monthly disposable budget.",
                                            fontSize = 12.sp,
                                            color = Color(0xFF94A3B8),
                                            lineHeight = 18.sp,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(14.dp))
                                        Surface(
                                            shape = CircleShape,
                                            color = Color(0xFF184E38),
                                            border = BorderStroke(1.dp, Color(0xFF2DD4BF).copy(alpha = 0.35f)),
                                            modifier = Modifier.clickable { showAddSubscriptionDialog = true }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Add,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = if (lang == AppLanguage.ZH) "新增第一筆固定收支" else "Add First Item",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            items(
                                items = uiState.subscriptions,
                                key = { it.id }
                            ) { sub ->
                                val isIncome = sub.type == TransactionType.INCOME
                                val timeFormatted = "%02d:%02d".format(sub.billingHour, sub.billingMinute)

                                val typeTag = if (isIncome) {
                                    if (sub.isCustomInterval) (if (lang == AppLanguage.ZH) "收入 (每 ${sub.intervalDays} 天)" else "Income (Every ${sub.intervalDays} Days)")
                                    else if (sub.isAnnual) (if (lang == AppLanguage.ZH) "年收入" else "Annual Income")
                                    else (if (lang == AppLanguage.ZH) "每月固定收入" else "Monthly Income")
                                } else {
                                    if (sub.isCustomInterval) (if (lang == AppLanguage.ZH) "支出 (每 ${sub.intervalDays} 天)" else "Expense (Every ${sub.intervalDays} Days)")
                                    else if (sub.isAnnual) (if (lang == AppLanguage.ZH) "年度扣款" else "Annual Expense")
                                    else (if (lang == AppLanguage.ZH) "每月定期扣款" else "Monthly Bill")
                                }

                                val dateDetailStr = if (sub.isCustomInterval) {
                                    if (isIncome) {
                                        if (lang == AppLanguage.ZH) "每 ${sub.intervalDays} 天於 $timeFormatted 入帳" else "Every ${sub.intervalDays} days at $timeFormatted"
                                    } else {
                                        if (lang == AppLanguage.ZH) "每 ${sub.intervalDays} 天於 $timeFormatted 扣款" else "Every ${sub.intervalDays} days at $timeFormatted"
                                    }
                                } else if (sub.isAnnual) {
                                    if (isIncome) {
                                        if (lang == AppLanguage.ZH) "每年 ${sub.billingMonth} 月 ${sub.billingDay} 日 $timeFormatted 入帳" else "Annual on ${sub.billingMonth}/${sub.billingDay} at $timeFormatted"
                                    } else {
                                        if (lang == AppLanguage.ZH) "每年 ${sub.billingMonth} 月 ${sub.billingDay} 日 $timeFormatted 扣款" else "Annual on ${sub.billingMonth}/${sub.billingDay} at $timeFormatted"
                                    }
                                } else {
                                    if (isIncome) {
                                        if (lang == AppLanguage.ZH) "每月 ${sub.billingDay} 日 $timeFormatted 入帳" else "Day ${sub.billingDay} at $timeFormatted"
                                    } else {
                                        if (lang == AppLanguage.ZH) "每月 ${sub.billingDay} 日 $timeFormatted 扣款" else "Day ${sub.billingDay} at $timeFormatted"
                                    }
                                }

                                val amountText = if (sub.isCustomInterval) {
                                    val monthlyAvg = sub.amount * (30.0 / sub.intervalDays.coerceAtLeast(1))
                                    val prefix = if (isIncome) "+" else "-"
                                    "$prefix$${String.format(Locale.getDefault(), "%,.0f", sub.amount)}/次"
                                } else if (sub.isAnnual) {
                                    val prefix = if (isIncome) "+" else "-"
                                    "$prefix$${String.format(Locale.getDefault(), "%,.0f", sub.amount)}/年"
                                } else {
                                    val prefix = if (isIncome) "+" else "-"
                                    "$prefix$${String.format(Locale.getDefault(), "%,.0f", sub.amount)}/月"
                                }

                                val amountColor = if (isIncome) Color(0xFF10B981) else Color(0xFFFB7185)

                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color(0xFF121714),
                                    border = BorderStroke(1.dp, Color(0xFF1F2923)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Category icon
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(if (isIncome) Color(0xFF162E22) else Color(0xFF26181B)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = getCategoryIcon(sub.category),
                                                contentDescription = sub.name,
                                                tint = if (isIncome) Color(0xFF10B981) else Color(0xFFFB7185),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(3.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = sub.name,
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = if (isIncome) Color(0xFF162E22) else Color(0xFF26181B)
                                                ) {
                                                    Text(
                                                        text = typeTag,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isIncome) Color(0xFF6EE7B7) else Color(0xFFFDA4AF),
                                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }

                                            Text(
                                                text = dateDetailStr,
                                                fontSize = 12.sp,
                                                color = Color(0xFF94A3B8)
                                            )
                                        }

                                        Column(
                                            horizontalAlignment = Alignment.End,
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = amountText,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace,
                                                color = amountColor
                                            )

                                            IconButton(
                                                onClick = {
                                                    if (uiState.isConfirmDeleteEnabled) {
                                                        deletingSubscription = sub
                                                    } else {
                                                        viewModel.deleteSubscription(sub)
                                                    }
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "刪除",
                                                    tint = Color(0xFF64748B),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 3. Savings Vault & Discipline Card
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF121714)),
                                border = BorderStroke(1.dp, Color(0xFF1F2923))
                            ) {
                                Column(
                                    modifier = Modifier.padding(18.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF162E22)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Savings,
                                                contentDescription = null,
                                                tint = Color(0xFF10B981),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (lang == AppLanguage.ZH) "小金庫儲蓄與目標規劃" else "Savings Vault Planning",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Bottom
                                    ) {
                                        Column {
                                            Text(
                                                text = if (lang == AppLanguage.ZH) "本月剩餘可用金庫" else "Remaining Vault",
                                                fontSize = 11.sp,
                                                color = Color(0xFF94A3B8)
                                            )
                                            Text(
                                                text = "$${String.format(Locale.getDefault(), "%,.0f", uiState.remainingBudget)}",
                                                fontSize = 24.sp,
                                                fontWeight = FontWeight.Black,
                                                fontFamily = FontFamily.Monospace,
                                                color = if (uiState.remainingBudget >= 0) Color(0xFF10B981) else Color(0xFFFB7185)
                                            )
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = if (lang == AppLanguage.ZH) "金庫健康度" else "Vault Health",
                                                fontSize = 11.sp,
                                                color = Color(0xFF94A3B8)
                                            )
                                            val healthPercent = ((1f - uiState.totalBudgetProgress) * 100).toInt().coerceIn(0, 100)
                                            Text(
                                                text = "$healthPercent%",
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF6EE7B7)
                                            )
                                        }
                                    }

                                    LinearProgressIndicator(
                                        progress = { (1f - uiState.totalBudgetProgress).coerceIn(0f, 1f) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = Color(0xFF10B981),
                                        trackColor = Color(0xFF1F2923)
                                    )

                                    Text(
                                        text = if (lang == AppLanguage.ZH)
                                            "💡 維持規律的記帳與日均配額紀律，每日未花費的預算將自動滾入金庫，累積為可觀的財富自由基金。"
                                        else
                                            "💡 Daily unspent allowances roll into your monthly vault to build wealth over time.",
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B),
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }

                        // Bottom spacer for FAB
                        item {
                            Spacer(modifier = Modifier.height(96.dp))
                        }
                    }
                }

                3 -> {
                    // Tab 3: Settings Screen
                    SettingsScreen(
                        initialTotalBudget = uiState.totalBudgetLimit,
                        currentThemeColor = uiState.themeColor,
                        currentThemePaletteEnabled = uiState.isThemePaletteEnabled,
                        currentCustomColorHex = uiState.customColorHex,
                        currentDarkMode = uiState.isDarkMode,
                        currentLanguage = uiState.appLanguage,
                        currentBetaTestingEnabled = uiState.isBetaTestingEnabled,
                        currentGooglePayListenerEnabled = uiState.isGooglePayListenerEnabled,
                        currentDailyReminderEnabled = uiState.isDailyReminderEnabled,
                        reminderTimes = uiState.reminderTimes,
                        currentConfirmDeleteEnabled = uiState.isConfirmDeleteEnabled,
                        currentIncomeTrackingEnabled = uiState.isIncomeTrackingEnabled,
                        currentSearchEnabled = uiState.isSearchEnabled,
                        currentSubscriptionEnabled = uiState.isSubscriptionEnabled,
                        currentMultiAccountEnabled = uiState.isMultiAccountEnabled,
                        currentModern3DUiEnabled = uiState.isModern3DUiEnabled,
                        currentDragDateReorderEnabled = uiState.isDragDateReorderEnabled,
                        currentAutoBackupEnabled = uiState.autoBackupEnabled,
                        lastAutoBackupTime = uiState.lastAutoBackupTime,
                        isEmbedded = true,
                        onDailyReminderMasterChange = { enabled ->
                            viewModel.setDailyReminderMasterEnabled(enabled)
                        },
                        onAddReminderTime = { h, m ->
                            viewModel.addReminderTime(h, m)
                        },
                        onDeleteReminderTime = { reminder ->
                            viewModel.deleteReminderTime(reminder)
                        },
                        onToggleReminderTime = { reminder, enabled ->
                            viewModel.toggleReminderTime(reminder, enabled)
                        },
                        onConfirmDeleteChange = { enabled ->
                            viewModel.setConfirmDeleteEnabled(enabled)
                        },
                        onDarkModeChange = { dark ->
                            viewModel.setDarkMode(dark)
                        },
                        onSaveSettings = { budgetLimit, color, language, betaTesting, gPay, incomeTracking, search, subscription, multiAccount, modern3D, dragDateReorder, autoBackup, themePalette, customColor ->
                            viewModel.saveSettings(
                                budgetLimit = budgetLimit,
                                color = color,
                                language = language,
                                betaTestingEnabled = betaTesting,
                                googlePayListenerEnabled = gPay,
                                incomeTrackingEnabled = incomeTracking,
                                searchEnabled = search,
                                subscriptionEnabled = subscription,
                                multiAccountEnabled = multiAccount,
                                modern3DUiEnabled = modern3D,
                                dragDateReorderEnabled = dragDateReorder,
                                autoBackupEnabled = autoBackup,
                                themePaletteEnabled = themePalette,
                                customColorHex = customColor
                            )
                        },
                        onExportReport = {
                            ExportUtils.exportTransactionsToCsv(context, uiState.allTransactions.ifEmpty { uiState.transactions }, uiState.appLanguage)
                        },
                        onBackupToGoogleDrive = {
                            viewModel.backupAllDataToGoogleDrive(context)
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

    if (showHelpDialog) {
        HelpDialog(
            language = lang,
            onDismissRequest = { showHelpDialog = false },
            onReplayOnboarding = {
                viewModel.resetOnboarding()
            }
        )
    }

    if (showAddDialog) {
        AddTransactionDialog(
            language = lang,
            initialTransaction = null,
            isMultiAccountEnabled = uiState.isMultiAccountEnabled,
            isIncomeTrackingEnabled = uiState.isIncomeTrackingEnabled,
            onGetCustomSubCategories = { category -> viewModel.getCustomSubCategories(category) },
            onAddCustomSubCategory = { category, sub -> viewModel.addCustomSubCategory(category, sub) },
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
            onGetCustomSubCategories = { category -> viewModel.getCustomSubCategories(category) },
            onAddCustomSubCategory = { category, sub -> viewModel.addCustomSubCategory(category, sub) },
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

    if (deletingTransaction != null) {
        val tx = deletingTransaction!!
        val isIncome = tx.type == TransactionType.INCOME
        val amountSign = if (isIncome) "+" else "-"
        AlertDialog(
            onDismissRequest = { deletingTransaction = null },
            title = {
                Text(
                    text = if (isZh) "確定要刪除這筆紀錄嗎？" else "Delete Record?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (isZh)
                        "刪除後將無法恢復：${tx.category} $amountSign$${tx.amount.toInt()}"
                    else
                        "This record will be permanently deleted: ${tx.category} $amountSign$${tx.amount.toInt()}"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteTransaction(tx)
                        deletingTransaction = null
                    }
                ) {
                    Text(
                        text = if (isZh) "確定刪除" else "Delete",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingTransaction = null }) {
                    Text(AppStrings.get("cancel", lang))
                }
            }
        )
    }

    if (deletingSubscription != null) {
        val sub = deletingSubscription!!
        val isIncome = sub.type == TransactionType.INCOME
        val amountSign = if (isIncome) "+" else "-"
        AlertDialog(
            onDismissRequest = { deletingSubscription = null },
            title = {
                Text(
                    text = if (isZh) "確定要刪除此固定收支嗎？" else "Delete Recurring Item?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (isZh)
                        "刪除後將停止自動扣款/入帳：${sub.name} $amountSign$${sub.amount.toInt()}"
                    else
                        "This recurring item will be deleted: ${sub.name} $amountSign$${sub.amount.toInt()}"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteSubscription(sub)
                        deletingSubscription = null
                    }
                ) {
                    Text(
                        text = if (isZh) "確定刪除" else "Delete",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingSubscription = null }) {
                    Text(AppStrings.get("cancel", lang))
                }
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
            onSaveSubscription = { name, amount, billingDay, billingMonth, isAnnual, type, category, isCustomInterval, intervalDays, billingHour, billingMinute ->
                viewModel.addSubscription(
                    name = name,
                    amount = amount,
                    billingDay = billingDay,
                    billingMonth = billingMonth,
                    isAnnual = isAnnual,
                    type = type,
                    category = category,
                    isCustomInterval = isCustomInterval,
                    intervalDays = intervalDays,
                    billingHour = billingHour,
                    billingMinute = billingMinute
                )
            }
        )
    }

    if (pendingBackupInfo != null) {
        val info = pendingBackupInfo!!
        val dateStr = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date(info.exportDate))

        AlertDialog(
            onDismissRequest = { pendingBackupInfo = null },
            title = {
                Text(
                    text = if (isZh) "雲端備份與本地資料比對" else "Backup Comparison",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (isZh) "備份建立時間：$dateStr" else "Backup Date: $dateStr",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(if (isZh) "項目" else "Item", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                                Text(if (isZh) "雲端備份" else "Cloud", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                Text(if (isZh) "本地資料" else "Local", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                            }

                            HorizontalDivider()

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(if (isZh) "總紀錄筆數" else "Total Records", style = MaterialTheme.typography.bodyMedium)
                                Text("${info.cloudTxCount} 筆", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium)
                                Text("${info.localTxCount} 筆", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(if (isZh) "總支出金額" else "Total Expense", style = MaterialTheme.typography.bodyMedium)
                                Text("$${String.format(Locale.getDefault(), "%,.0f", info.cloudTotalExpense)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium)
                                Text("$${String.format(Locale.getDefault(), "%,.0f", info.localTotalExpense)}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(if (isZh) "總收入金額" else "Total Income", style = MaterialTheme.typography.bodyMedium)
                                Text("$${String.format(Locale.getDefault(), "%,.0f", info.cloudTotalIncome)}", fontWeight = FontWeight.Bold, color = Color(0xFF059669), style = MaterialTheme.typography.bodyMedium)
                                Text("$${String.format(Locale.getDefault(), "%,.0f", info.localTotalIncome)}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }

                    if (info.hasSettings || info.cloudSubscriptionCount > 0) {
                        Text(
                            text = if (isZh)
                                "備份檔案包含：所有月份紀錄、固定收支(${info.cloudSubscriptionCount}筆) 及完整系統設定偏好。"
                            else
                                "Backup includes: all monthly history, recurring items (${info.cloudSubscriptionCount}), and system preferences.",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF059669),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (info.uniqueCloudCount > 0) {
                        Text(
                            text = if (isZh)
                                "比對結果：雲端包含 ${info.uniqueCloudCount} 筆本地尚未記錄的新資料。"
                            else
                                "Cloud backup contains ${info.uniqueCloudCount} new records not in local data.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            text = if (isZh)
                                "比對結果：本地資料與雲端備份紀錄一致。"
                            else
                                "Local data matches cloud backup.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            viewModel.restoreBackupJson(info.jsonContent, overwrite = false) { count ->
                                val msg = if (isZh) "已成功合併 $count 筆紀錄及系統設定！" else "Successfully merged $count records and settings!"
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            }
                            pendingBackupInfo = null
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (isZh) "合併雲端資料" else "Merge Cloud", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.restoreBackupJson(info.jsonContent, overwrite = true) { count ->
                                val msg = if (isZh) "已成功覆蓋還原 $count 筆紀錄及系統設定！" else "Successfully restored $count records and settings!"
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            }
                            pendingBackupInfo = null
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (isZh) "覆蓋本地資料" else "Overwrite Local", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingBackupInfo = null }) {
                    Text(AppStrings.get("cancel", lang))
                }
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
        Color.Transparent

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 4.dp)
            .onGloballyPositioned { coordinates ->
                onBoundsMeasured(coordinates.boundsInWindow())
            },
        color = backgroundColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = group.displayDate.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            val sign = if (group.totalDailyExpense > 0) "-" else "+"
            val absAmount = abs(group.totalDailyExpense)
            Text(
                text = "$sign$${String.format(Locale.getDefault(), "%,.0f", absAmount)}",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
    val amountPrefix = if (isExpense) "-" else "+"
    val amountColor = if (isExpense) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.primary

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

    // Style A: Edge-to-edge Flat Row with subtle mist background icon & borderless elegance
    Surface(
        modifier = modifierWithDrag
            .bouncyClickable { onEdit() },
        color = Color.Transparent
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Minimalist frosted/mist icon container
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier.size(46.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = getCategoryIcon(transaction.category),
                                contentDescription = transaction.category,
                                tint = if (isExpense) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f, fill = false),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        val localizedCategory = AppStrings.getCategoryName(transaction.category, language)
                        val localizedNote = transaction.note.split(" • ").joinToString(" • ") { part ->
                            AppStrings.getSubCategoryName(part, language)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = localizedCategory,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.2.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        val noteSubtitle = if (localizedNote.isNotBlank()) "$localizedNote • $timeStr" else timeStr
                        Text(
                            text = noteSubtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "$amountPrefix$${String.format(Locale.getDefault(), "%,.0f", transaction.amount)}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        ),
                        color = amountColor
                    )

                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "刪除這筆紀錄",
                            tint = MaterialTheme.colorScheme.outlineVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // High-end subtle edge-to-edge separator line
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                thickness = 0.7.dp,
                modifier = Modifier.padding(start = 60.dp)
            )
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
    val calendar = Calendar.getInstance()
    val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val remainingDays = (daysInMonth - currentDay + 1).coerceAtLeast(1)

    val dailyEstimatedUsable = if (remainingBudget > 0) (remainingBudget / remainingDays) else 0.0
    val progressClamped = progress.coerceIn(0f, 1f)

    // Style A: Premium Linear / Apple Wallet Fintech Hero Asset Card
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .bouncyClickable { onOpenMonthlyBreakdown() },
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Top Bar: Vault Label + Live Indicator Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                if (isOverBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                    )
                    Text(
                        text = (if (isZh) "本月總資產動態" else "Monthly Asset Overview").uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerLowest,
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (isOverBudget) "超出預算" else "${(progressClamped * 100).toInt()}% 已用",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            ),
                            color = if (isOverBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Big Headline Amount: Spent
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = if (isZh) "本月累積支出" else "Total Spent This Month",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "$",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Text(
                        text = String.format(Locale.getDefault(), "%,.0f", totalExpense),
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 44.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "TWD",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }

            // Smooth Fintech Progress Bar
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progressClamped)
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(
                                if (isOverBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                    )
                }
            }

            // Clean Divider & Pure Split Layout (已花費 / 剩餘金庫 / 日均配額)
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                thickness = 1.dp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Column 1: Remaining Vault
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = if (isZh) "剩餘金庫" else "Remaining Vault",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$${String.format(Locale.getDefault(), "%,.0f", remainingBudget)}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        ),
                        color = if (remainingBudget >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }

                // Vertical separator line
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(28.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                )

                // Column 2: Total Budget Limit
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = if (isZh) "預算目標" else "Budget Limit",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$${String.format(Locale.getDefault(), "%,.0f", totalBudget)}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Vertical separator line
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(28.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                )

                // Column 3: Daily Available
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = if (isZh) "日均配額 ($remainingDays 天)" else "Daily ($remainingDays d)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$${String.format(Locale.getDefault(), "%,.0f", dailyEstimatedUsable)}/天",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        ),
                        color = MaterialTheme.colorScheme.secondary
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
    onSaveSubscription: (
        name: String,
        amount: Double,
        billingDay: Int,
        billingMonth: Int,
        isAnnual: Boolean,
        type: TransactionType,
        category: String,
        isCustomInterval: Boolean,
        intervalDays: Int,
        billingHour: Int,
        billingMinute: Int
    ) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var billingMonthText by remember { mutableStateOf("1") }
    var billingDayText by remember { mutableStateOf("1") }
    var intervalDaysText by remember { mutableStateOf("14") }
    var frequencyMode by remember { mutableIntStateOf(0) } // 0: 按月, 1: 按年, 2: 自訂天數
    var billingHourState by remember { mutableIntStateOf(9) }
    var billingMinuteState by remember { mutableIntStateOf(0) }
    var transactionType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var isError by remember { mutableStateOf(false) }

    val isZh = language == AppLanguage.ZH

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                if (isZh) "新增固定收支" else "Add Recurring Item",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Income vs Expense
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = transactionType == TransactionType.EXPENSE,
                        onClick = { transactionType = TransactionType.EXPENSE },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                    ) {
                        Text(if (isZh) "固定支出" else "Expense")
                    }
                    SegmentedButton(
                        selected = transactionType == TransactionType.INCOME,
                        onClick = { transactionType = TransactionType.INCOME },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                    ) {
                        Text(if (isZh) "固定收入" else "Income")
                    }
                }

                // Frequency Mode: Monthly vs Annual vs Custom Interval
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = frequencyMode == 0,
                        onClick = { frequencyMode = 0 },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3)
                    ) {
                        Text(if (isZh) "按月" else "Monthly", fontSize = 12.sp)
                    }
                    SegmentedButton(
                        selected = frequencyMode == 1,
                        onClick = { frequencyMode = 1 },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3)
                    ) {
                        Text(if (isZh) "按年" else "Annual", fontSize = 12.sp)
                    }
                    SegmentedButton(
                        selected = frequencyMode == 2,
                        onClick = { frequencyMode = 2 },
                        shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3)
                    ) {
                        Text(if (isZh) "自訂天數" else "Custom", fontSize = 12.sp)
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; isError = false },
                    label = {
                        Text(
                            if (isZh) {
                                if (transactionType == TransactionType.INCOME) "項目名稱 (例如 薪水, 租金收入)" else "項目名稱 (例如 房租, Netflix)"
                            } else "Item Name"
                        )
                    },
                    singleLine = true,
                    isError = isError,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it; isError = false },
                    label = {
                        Text(
                            if (isZh) {
                                if (transactionType == TransactionType.INCOME) "收入金額 ($)" else "扣款金額 ($)"
                            } else "Amount ($)"
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = isError,
                    modifier = Modifier.fillMaxWidth()
                )

                if (frequencyMode == 2) {
                    OutlinedTextField(
                        value = intervalDaysText,
                        onValueChange = { intervalDaysText = it },
                        label = {
                            Text(
                                if (isZh) {
                                    if (transactionType == TransactionType.INCOME) "每幾天發放一次 (例如 14, 30)" else "每幾天扣款一次 (例如 14, 30)"
                                } else "Interval Days (e.g. 14, 30)"
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (frequencyMode == 1) {
                            OutlinedTextField(
                                value = billingMonthText,
                                onValueChange = { billingMonthText = it },
                                label = {
                                    Text(
                                        if (isZh) {
                                            if (transactionType == TransactionType.INCOME) "入帳月份 (1-12)" else "扣款月份 (1-12)"
                                        } else "Month (1-12)"
                                    )
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        OutlinedTextField(
                            value = billingDayText,
                            onValueChange = { billingDayText = it },
                            label = {
                                Text(
                                    if (isZh) {
                                        if (transactionType == TransactionType.INCOME) "入帳日期 (1-31)" else "扣款日期 (1-31)"
                                    } else "Day (1-31)"
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Time Picker Section
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isZh)
                            (if (transactionType == TransactionType.INCOME) "入帳時間：%02d:%02d".format(billingHourState, billingMinuteState) else "扣款時間：%02d:%02d".format(billingHourState, billingMinuteState))
                        else
                            "Time: %02d:%02d".format(billingHourState, billingMinuteState),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedButton(
                        onClick = {
                            TimePickerDialog(
                                context,
                                { _, hourOfDay, minute ->
                                    billingHourState = hourOfDay
                                    billingMinuteState = minute
                                },
                                billingHourState,
                                billingMinuteState,
                                true
                            ).show()
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (isZh) "變更時間" else "Change Time", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    val day = billingDayText.toIntOrNull()?.coerceIn(1, 31) ?: 1
                    val month = billingMonthText.toIntOrNull()?.coerceIn(1, 12) ?: 1
                    val intervalDays = intervalDaysText.toIntOrNull()?.coerceIn(1, 365) ?: 30
                    val isAnnual = frequencyMode == 1
                    val isCustom = frequencyMode == 2
                    val defaultCat = if (transactionType == TransactionType.INCOME) "薪水" else "日常"

                    if (name.isNotBlank() && amount > 0) {
                        onSaveSubscription(
                            name, amount, day, month, isAnnual, transactionType, defaultCat,
                            isCustom, intervalDays, billingHourState, billingMinuteState
                        )
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
