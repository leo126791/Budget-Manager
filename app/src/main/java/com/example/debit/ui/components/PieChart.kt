package com.example.debit.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.debit.data.AppLanguage
import com.example.debit.data.Transaction
import com.example.debit.data.TransactionType
import com.example.debit.ui.utils.AppStrings
import com.example.debit.ui.utils.DateFormatUtils
import com.example.debit.ui.utils.getCategoryIcon
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

val pieChartColors = listOf(
    Color(0xFF10B981), // Emerald
    Color(0xFF8B5CF6), // Violet / Purple
    Color(0xFF06B6D4), // Sky / Cyan
    Color(0xFFF59E0B), // Amber Gold
    Color(0xFFF43F5E), // Rose Coral
    Color(0xFF3B82F6), // Blue
    Color(0xFFEC4899), // Pink
    Color(0xFF64748B)  // Slate
)

@Composable
fun FintechSegmentedTabs(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = CircleShape,
        color = Color(0xFF18201C),
        border = BorderStroke(1.dp, Color(0xFF26332B)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            options.forEachIndexed { index, title ->
                val isSelected = selectedIndex == index
                Surface(
                    shape = CircleShape,
                    color = if (isSelected) Color(0xFF184E38) else Color.Transparent,
                    border = if (isSelected) BorderStroke(1.dp, Color(0xFF2DD4BF).copy(alpha = 0.35f)) else null,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelect(index) }
                ) {
                    Text(
                        text = title,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else Color(0xFF94A3B8),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 7.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ExpensePieChartCard(
    categoryExpenses: Map<String, Double>,
    totalExpense: Double,
    totalBudget: Double,
    transactions: List<Transaction>,
    language: AppLanguage,
    selectedYearMonth: String = "",
    modifier: Modifier = Modifier
) {
    var selectedChartIndex by remember { mutableIntStateOf(0) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF121714)
        ),
        border = BorderStroke(1.dp, Color(0xFF1F2923))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Linear-Style Segmented Capsule Switch
            FintechSegmentedTabs(
                options = listOf(
                    AppStrings.get("pie_chart", language),
                    AppStrings.get("daily_chart", language)
                ),
                selectedIndex = selectedChartIndex,
                onSelect = { selectedChartIndex = it },
                modifier = Modifier.fillMaxWidth()
            )

            AnimatedContent(
                targetState = selectedChartIndex,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally { width -> width / 3 } + fadeIn(tween(250))) togetherWith
                                (slideOutHorizontally { width -> -width / 3 } + fadeOut(tween(250)))
                    } else {
                        (slideInHorizontally { width -> -width / 3 } + fadeIn(tween(250))) togetherWith
                                (slideOutHorizontally { width -> width / 3 } + fadeOut(tween(250)))
                    }.using(SizeTransform(clip = false))
                },
                label = "ChartSwitchAnimation"
            ) { chartIndex ->
                if (chartIndex == 0) {
                    DonutChartContent(
                        categoryExpenses = categoryExpenses,
                        totalExpense = totalExpense,
                        totalBudget = totalBudget,
                        language = language
                    )
                } else {
                    DailyUsageChartContent(
                        transactions = transactions,
                        totalExpense = totalExpense,
                        language = language,
                        selectedYearMonth = selectedYearMonth
                    )
                }
            }
        }
    }
}

@Composable
private fun DonutChartContent(
    categoryExpenses: Map<String, Double>,
    totalExpense: Double,
    totalBudget: Double,
    language: AppLanguage
) {
    val baseAmount = if (totalBudget > 0) maxOf(totalBudget, totalExpense) else totalExpense
    val sortedEntries = categoryExpenses.entries
        .filter { it.value > 0 }
        .sortedByDescending { it.value }

    val trackColor = Color(0xFF222B26)
    val remainingBudget = if (totalBudget > 0) maxOf(0.0, totalBudget - totalExpense) else 0.0

    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(categoryExpenses, totalBudget) {
        animationProgress.animateTo(1f, animationSpec = tween(durationMillis = 800))
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Donut Chart Canvas
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(136.dp)
        ) {
            Canvas(modifier = Modifier.size(126.dp)) {
                val strokeWidth = 22.dp.toPx()

                // Background track
                drawArc(
                    color = trackColor,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // Category slices
                var startAngle = -90f
                sortedEntries.forEachIndexed { index, entry ->
                    val sweepAngle = ((entry.value / baseAmount) * 360f * animationProgress.value).toFloat()
                    val color = pieChartColors[index % pieChartColors.size]

                    if (sweepAngle > 0.5f) {
                        drawArc(
                            color = color,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                    startAngle += sweepAngle
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (totalBudget > 0) {
                    val usagePercent = ((totalExpense / totalBudget) * 100).toInt()
                    Text(
                        text = AppStrings.get("budget_usage", language),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF94A3B8)
                    )
                    Text(
                        text = "$usagePercent%",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.SansSerif,
                        color = if (totalExpense > totalBudget) Color(0xFFF43F5E) else Color(0xFF10B981)
                    )
                } else {
                    Text(
                        text = AppStrings.get("total_expense", language),
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                    Text(
                        text = "$${String.format(Locale.getDefault(), "%,.0f", totalExpense)}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = Color.White
                    )
                }
            }
        }

        // Category Breakdown in Edge-to-edge Flat Rows
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .weight(1f)
                .animateContentSize()
        ) {
            sortedEntries.take(4).forEachIndexed { index, entry ->
                val color = pieChartColors[index % pieChartColors.size]
                val percent = if (totalExpense > 0) ((entry.value / totalExpense) * 100).toInt() else 0
                val catName = AppStrings.getCategoryName(entry.key, language)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Category icon with soft color pill
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(color.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getCategoryIcon(entry.key),
                            contentDescription = catName,
                            tint = color,
                            modifier = Modifier.size(13.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = catName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFE2E8F0),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    // Percentage pill badge
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = color.copy(alpha = 0.15f),
                        modifier = Modifier.padding(horizontal = 6.dp)
                    ) {
                        Text(
                            text = "$percent%",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = color,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }

                    // Bold Tabular Amount
                    Text(
                        text = "$${String.format(Locale.getDefault(), "%,.0f", entry.value)}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Color.White
                    )
                }
            }

            if (totalBudget > 0 && remainingBudget > 0) {
                val unusedLabel = AppStrings.get("unused_budget", language)
                val unusedPercent = ((remainingBudget / totalBudget) * 100).toInt()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF26322C)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF64748B))
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = unusedLabel,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF94A3B8),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF26322C),
                        modifier = Modifier.padding(horizontal = 6.dp)
                    ) {
                        Text(
                            text = "$unusedPercent%",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF94A3B8),
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }

                    Text(
                        text = "$${String.format(Locale.getDefault(), "%,.0f", remainingBudget)}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyUsageChartContent(
    transactions: List<Transaction>,
    totalExpense: Double,
    language: AppLanguage,
    selectedYearMonth: String
) {
    val ymParts = selectedYearMonth.split("-")
    val calTarget = Calendar.getInstance().apply {
        if (ymParts.size == 2) {
            val y = ymParts[0].toIntOrNull()
            val m = ymParts[1].toIntOrNull()
            if (y != null && m != null) {
                set(Calendar.YEAR, y)
                set(Calendar.MONTH, m - 1)
                set(Calendar.DAY_OF_MONTH, 1)
            }
        } else if (transactions.isNotEmpty()) {
            timeInMillis = transactions.first().date
        }
    }

    val targetYear = calTarget.get(Calendar.YEAR)
    val targetMonth = calTarget.get(Calendar.MONTH)
    val daysInMonth = calTarget.getActualMaximum(Calendar.DAY_OF_MONTH)

    val dailyMap = remember(transactions, selectedYearMonth) {
        val map = mutableMapOf<Int, Double>()
        for (i in 1..daysInMonth) map[i] = 0.0
        val calTx = Calendar.getInstance()
        transactions.forEach { tx ->
            if (tx.type == TransactionType.EXPENSE) {
                calTx.timeInMillis = tx.date
                if (calTx.get(Calendar.YEAR) == targetYear && calTx.get(Calendar.MONTH) == targetMonth) {
                    val d = calTx.get(Calendar.DAY_OF_MONTH)
                    map[d] = (map[d] ?: 0.0) + tx.amount
                }
            }
        }
        map
    }

    val maxDaily = maxOf(1.0, dailyMap.values.maxOrNull() ?: 1.0)
    val activeDays = if (selectedYearMonth.isBlank() || selectedYearMonth == DateFormatUtils.formatYM(Date())) {
        Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
    } else {
        daysInMonth
    }
    val avgDaily = if (activeDays > 0) totalExpense / activeDays else 0.0

    val primaryColor = Color(0xFF10B981)
    val trackColor = Color(0xFF1F2923)
    val activeBarColor = Color(0xFF34D399)

    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(transactions) {
        animationProgress.animateTo(1f, animationSpec = tween(durationMillis = 800))
    }

    val dailyAvgLabel = AppStrings.get("daily_avg", language)
    val dailyMaxLabel = AppStrings.get("daily_max", language)
    val dayUnitLabel = AppStrings.get("day_unit", language)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF17241D),
                border = BorderStroke(1.dp, Color(0xFF233B2E))
            ) {
                Text(
                    text = "$dailyAvgLabel: $${String.format(Locale.getDefault(), "%,.0f", avgDaily)}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF6EE7B7),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF1C2420),
                border = BorderStroke(1.dp, Color(0xFF28362E))
            ) {
                Text(
                    text = "$dailyMaxLabel: $${String.format(Locale.getDefault(), "%,.0f", maxDaily)}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF94A3B8),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        // Daily Bar Chart Canvas
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val barCount = daysInMonth
            val spacing = 2.dp.toPx()
            val totalSpacing = spacing * (barCount + 1)
            val barWidth = maxOf(2f, (canvasWidth - totalSpacing) / barCount)

            val isCurrentMonth = selectedYearMonth.isBlank() || selectedYearMonth == DateFormatUtils.formatYM(Date())
            val today = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

            for (d in 1..daysInMonth) {
                val spent = dailyMap[d] ?: 0.0
                val barHeight = ((spent / maxDaily) * (canvasHeight - 16.dp.toPx()) * animationProgress.value).toFloat()
                val x = spacing + (d - 1) * (barWidth + spacing)
                val y = canvasHeight - barHeight

                val color = when {
                    isCurrentMonth && d == today -> activeBarColor
                    spent > 0 -> primaryColor
                    else -> trackColor
                }

                drawRoundRect(
                    color = trackColor.copy(alpha = 0.5f),
                    topLeft = Offset(x, 0f),
                    size = Size(barWidth, canvasHeight),
                    cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                )

                if (barHeight > 0) {
                    drawRoundRect(
                        color = color,
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("${dayUnitLabel}1", fontSize = 10.sp, color = Color(0xFF64748B))
            Text("${dayUnitLabel}10", fontSize = 10.sp, color = Color(0xFF64748B))
            Text("${dayUnitLabel}20", fontSize = 10.sp, color = Color(0xFF64748B))
            Text("${dayUnitLabel}${daysInMonth}", fontSize = 10.sp, color = Color(0xFF64748B))
        }
    }
}

@Composable
fun ExpenseTrendLineChartCard(
    transactions: List<Transaction>,
    language: AppLanguage,
    selectedYearMonth: String = "",
    modifier: Modifier = Modifier
) {
    var isMonthlyMode by remember { mutableStateOf(false) }
    val isZh = language == AppLanguage.ZH

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF121714)
        ),
        border = BorderStroke(1.dp, Color(0xFF1F2923))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row: Title + Linear Segmented Capsule
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isZh) "消費走勢圖" else "Expense Trend Chart",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                FintechSegmentedTabs(
                    options = listOf(if (isZh) "日趨勢" else "Daily", if (isZh) "月趨勢" else "Monthly"),
                    selectedIndex = if (!isMonthlyMode) 0 else 1,
                    onSelect = { isMonthlyMode = it == 1 },
                    modifier = Modifier.width(160.dp)
                )
            }

            if (transactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isZh) "尚無消費數據可繪製折線圖" else "No expense data for trend chart",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF64748B)
                    )
                }
            } else {
                val ymParts = selectedYearMonth.split("-")
                val calTarget = Calendar.getInstance().apply {
                    if (ymParts.size == 2) {
                        val y = ymParts[0].toIntOrNull()
                        val m = ymParts[1].toIntOrNull()
                        if (y != null && m != null) {
                            set(Calendar.YEAR, y)
                            set(Calendar.MONTH, m - 1)
                            set(Calendar.DAY_OF_MONTH, 1)
                        }
                    } else if (transactions.isNotEmpty()) {
                        timeInMillis = transactions.first().date
                    }
                }

                val targetYear = calTarget.get(Calendar.YEAR)
                val targetMonth = calTarget.get(Calendar.MONTH)
                val daysInMonth = calTarget.getActualMaximum(Calendar.DAY_OF_MONTH)

                // Map data points
                val dataPoints: List<Pair<String, Double>> = if (!isMonthlyMode) {
                    val dailyMap = mutableMapOf<Int, Double>()
                    for (d in 1..daysInMonth) dailyMap[d] = 0.0

                    val calTx = Calendar.getInstance()
                    transactions.forEach { tx ->
                        if (tx.type == TransactionType.EXPENSE) {
                            calTx.timeInMillis = tx.date
                            if (calTx.get(Calendar.YEAR) == targetYear && calTx.get(Calendar.MONTH) == targetMonth) {
                                val day = calTx.get(Calendar.DAY_OF_MONTH)
                                dailyMap[day] = (dailyMap[day] ?: 0.0) + tx.amount
                            }
                        }
                    }
                    dailyMap.entries.map { "${it.key}日" to it.value }
                } else {
                    val monthlyMap = mutableMapOf<String, Double>()
                    val ymFormat = SimpleDateFormat("yyyy/MM", Locale.getDefault())
                    for (i in 5 downTo 0) {
                        val calPast = (calTarget.clone() as Calendar).apply {
                            add(Calendar.MONTH, -i)
                        }
                        monthlyMap[ymFormat.format(calPast.time)] = 0.0
                    }
                    transactions.forEach { tx ->
                        if (tx.type == TransactionType.EXPENSE) {
                            val ym = ymFormat.format(Date(tx.date))
                            if (monthlyMap.containsKey(ym)) {
                                monthlyMap[ym] = (monthlyMap[ym] ?: 0.0) + tx.amount
                            }
                        }
                    }
                    monthlyMap.entries.map { (ym, amount) ->
                        val monthLabel = ym.takeLast(2) + "月"
                        monthLabel to amount
                    }
                }

                val maxVal = maxOf(10.0, dataPoints.maxOfOrNull { it.second } ?: 10.0)
                val avgVal = if (dataPoints.isNotEmpty()) dataPoints.map { it.second }.average() else 0.0
                val peakPoint = dataPoints.maxByOrNull { it.second }

                val primaryColor = Color(0xFF10B981)
                val gridColor = Color(0xFF222B26)

                val animProgress = remember { Animatable(0f) }
                LaunchedEffect(isMonthlyMode, transactions) {
                    animProgress.animateTo(1f, animationSpec = tween(700))
                }

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // KPI Callout Badges
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF16251E),
                            border = BorderStroke(1.dp, Color(0xFF243B2F))
                        ) {
                            Text(
                                text = if (isZh) "單日最高: $${String.format(Locale.getDefault(), "%,.0f", maxVal)} (${peakPoint?.first ?: ""})" else "Peak: $${String.format(Locale.getDefault(), "%,.0f", maxVal)}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6EE7B7),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF1A221E),
                            border = BorderStroke(1.dp, Color(0xFF26332C))
                        ) {
                            Text(
                                text = if (isZh) "平均支出: $${String.format(Locale.getDefault(), "%,.0f", avgVal)}" else "Avg: $${String.format(Locale.getDefault(), "%,.0f", avgVal)}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF94A3B8),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Cubic Bezier Area Chart Canvas
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(145.dp)
                    ) {
                        val w = size.width
                        val h = size.height
                        val pad = 12.dp.toPx()
                        val chartW = w - pad * 2
                        val chartH = h - pad * 2

                        // Dashed Grid horizontal lines
                        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        for (i in 0..3) {
                            val yLine = pad + (chartH / 3f) * i
                            drawLine(
                                color = gridColor,
                                start = Offset(pad, yLine),
                                end = Offset(w - pad, yLine),
                                strokeWidth = 1.dp.toPx(),
                                pathEffect = dashEffect
                            )
                        }

                        if (dataPoints.size > 1) {
                            val stepX = chartW / (dataPoints.size - 1)
                            val points = dataPoints.mapIndexed { idx, pair ->
                                val x = pad + idx * stepX
                                val normalizedY = (pair.second / maxVal).toFloat().coerceIn(0f, 1f)
                                val y = pad + chartH * (1f - normalizedY * animProgress.value)
                                Offset(x, y)
                            }

                            val linePath = Path().apply {
                                moveTo(points.first().x, points.first().y)
                                for (i in 0 until points.size - 1) {
                                    val p1 = points[i]
                                    val p2 = points[i + 1]
                                    val cx1 = (p1.x + p2.x) / 2f
                                    val cy1 = p1.y
                                    val cx2 = (p1.x + p2.x) / 2f
                                    val cy2 = p2.y
                                    cubicTo(cx1, cy1, cx2, cy2, p2.x, p2.y)
                                }
                            }

                            val fillPath = Path().apply {
                                addPath(linePath)
                                lineTo(points.last().x, pad + chartH)
                                lineTo(points.first().x, pad + chartH)
                                close()
                            }

                            // Soft gradient area fill
                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        primaryColor.copy(alpha = 0.40f),
                                        primaryColor.copy(alpha = 0.08f),
                                        Color.Transparent
                                    ),
                                    startY = pad,
                                    endY = pad + chartH
                                )
                            )

                            // Main spline line
                            drawPath(
                                path = linePath,
                                color = Color(0xFF34D399),
                                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                            )

                            // Highlight peak point with glowing aura
                            val peakIdx = dataPoints.indexOfFirst { it.second == maxVal }
                            if (peakIdx >= 0 && maxVal > 0) {
                                val peakPt = points[peakIdx]
                                // Glow ring
                                drawCircle(
                                    color = Color(0xFF10B981).copy(alpha = 0.25f),
                                    radius = 12.dp.toPx(),
                                    center = peakPt
                                )
                                // Outer point
                                drawCircle(
                                    color = Color(0xFF10B981),
                                    radius = 5.dp.toPx(),
                                    center = peakPt
                                )
                                // Inner white center
                                drawCircle(
                                    color = Color.White,
                                    radius = 2.5.dp.toPx(),
                                    center = peakPt
                                )
                            }
                        }
                    }

                    // X-Axis Date distribution
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (!isMonthlyMode) {
                            Text("1日", fontSize = 10.sp, color = Color(0xFF64748B))
                            Text("10日", fontSize = 10.sp, color = Color(0xFF64748B))
                            Text("20日", fontSize = 10.sp, color = Color(0xFF64748B))
                            Text("${daysInMonth}日", fontSize = 10.sp, color = Color(0xFF64748B))
                        } else {
                            dataPoints.forEach { pt ->
                                Text(pt.first, fontSize = 10.sp, color = Color(0xFF64748B))
                            }
                        }
                    }
                }
            }
        }
    }
}
