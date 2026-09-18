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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.debit.data.AppLanguage
import com.example.debit.data.Transaction
import com.example.debit.data.TransactionType
import com.example.debit.ui.utils.AppStrings
import com.example.debit.ui.utils.DateFormatUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

val pieChartColors = listOf(
    Color(0xFF4F46E5), // Indigo
    Color(0xFF0EA5E9), // Teal
    Color(0xFF10B981), // Emerald
    Color(0xFFF59E0B), // Amber
    Color(0xFFEC4899), // Pink
    Color(0xFF8B5CF6), // Purple
    Color(0xFF6366F1), // Violet
    Color(0xFF64748B)  // Slate
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensePieChartCard(
    categoryExpenses: Map<String, Double>,
    totalExpense: Double,
    totalBudget: Double,
    transactions: List<Transaction>,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    var selectedChartIndex by remember { mutableIntStateOf(0) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Chart Type Segmented Button
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = selectedChartIndex == 0,
                    onClick = { selectedChartIndex = 0 },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                ) {
                    Text(AppStrings.get("pie_chart", language))
                }
                SegmentedButton(
                    selected = selectedChartIndex == 1,
                    onClick = { selectedChartIndex = 1 },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                ) {
                    Text(AppStrings.get("daily_chart", language))
                }
            }

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
                        language = language
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

    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val remainingBudget = if (totalBudget > 0) maxOf(0.0, totalBudget - totalExpense) else 0.0

    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(categoryExpenses, totalBudget) {
        animationProgress.animateTo(1f, animationSpec = tween(durationMillis = 800))
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        // Donut Chart Canvas
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(140.dp)
        ) {
            Canvas(modifier = Modifier.size(130.dp)) {
                val strokeWidth = 26.dp.toPx()

                // Background track
                drawArc(
                    color = trackColor,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                )

                // Category slices
                var startAngle = -90f
                sortedEntries.forEachIndexed { index, entry ->
                    val sweepAngle = ((entry.value / baseAmount) * 360f * animationProgress.value).toFloat()
                    val color = pieChartColors[index % pieChartColors.size]

                    drawArc(
                        color = color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                    )
                    startAngle += sweepAngle
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (totalBudget > 0) {
                    val usagePercent = ((totalExpense / totalBudget) * 100).toInt()
                    Text(
                        text = AppStrings.get("budget_usage", language),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$usagePercent%",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = if (totalExpense > totalBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                } else {
                    Text(
                        text = AppStrings.get("total_expense", language),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$${String.format(Locale.getDefault(), "%,.0f", totalExpense)}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Category Legend
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.animateContentSize()
        ) {
            sortedEntries.take(5).forEachIndexed { index, entry ->
                val color = pieChartColors[index % pieChartColors.size]
                val percent = if (totalExpense > 0) ((entry.value / totalExpense) * 100).toInt() else 0
                val catName = AppStrings.getCategoryName(entry.key, language)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(color, CircleShape)
                    )
                    Text(
                        text = "$catName $percent%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "($${String.format(Locale.getDefault(), "%,.0f", entry.value)})",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (totalBudget > 0 && remainingBudget > 0) {
                val unusedLabel = AppStrings.get("unused_budget", language)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(trackColor, CircleShape)
                    )
                    Text(
                        text = unusedLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "($${String.format(Locale.getDefault(), "%,.0f", remainingBudget)})",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
    language: AppLanguage
) {
    val calendar = Calendar.getInstance()
    val today = calendar.get(Calendar.DAY_OF_MONTH)
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val currentYM = DateFormatUtils.formatYM(Date())

    val dailyMap = mutableMapOf<Int, Double>()
    for (day in 1..daysInMonth) {
        dailyMap[day] = 0.0
    }

    val calTx = Calendar.getInstance()
    transactions.forEach { tx ->
        if (tx.type == TransactionType.EXPENSE) {
            val txYM = DateFormatUtils.formatYM(Date(tx.date))
            if (txYM == currentYM) {
                calTx.timeInMillis = tx.date
                val dayInt = calTx.get(Calendar.DAY_OF_MONTH)
                dailyMap[dayInt] = (dailyMap[dayInt] ?: 0.0) + tx.amount
            }
        }
    }

    val maxDaily = maxOf(1.0, dailyMap.values.maxOrNull() ?: 1.0)
    val avgDaily = totalExpense / today

    val primaryColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val activeBarColor = MaterialTheme.colorScheme.tertiary

    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(transactions) {
        animationProgress.animateTo(1f, animationSpec = tween(durationMillis = 800))
    }

    val dailyAvgLabel = AppStrings.get("daily_avg", language)
    val dailyMaxLabel = AppStrings.get("daily_max", language)
    val dayUnitLabel = AppStrings.get("day_unit", language)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$dailyAvgLabel: $${String.format(Locale.getDefault(), "%,.0f", avgDaily)}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "$dailyMaxLabel: $${String.format(Locale.getDefault(), "%,.0f", maxDaily)}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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

            for (d in 1..daysInMonth) {
                val spent = dailyMap[d] ?: 0.0
                val barHeight = ((spent / maxDaily) * (canvasHeight - 16.dp.toPx()) * animationProgress.value).toFloat()
                val x = spacing + (d - 1) * (barWidth + spacing)
                val y = canvasHeight - barHeight

                val color = when {
                    d == today -> activeBarColor
                    spent > 0 -> primaryColor
                    else -> trackColor
                }

                drawRoundRect(
                    color = trackColor.copy(alpha = 0.4f),
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
            Text("${dayUnitLabel}1", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("${dayUnitLabel}10", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("${dayUnitLabel}20", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("${dayUnitLabel}${daysInMonth}", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseTrendLineChartCard(
    transactions: List<Transaction>,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    var isMonthlyMode by remember { mutableStateOf(false) }
    val isZh = language == AppLanguage.ZH

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isZh) "📈 消費走勢動態折線圖" else "📈 Expense Trend Chart",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                SingleChoiceSegmentedButtonRow {
                    SegmentedButton(
                        selected = !isMonthlyMode,
                        onClick = { isMonthlyMode = false },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                    ) {
                        Text(if (isZh) "日趨勢" else "Daily", fontSize = 12.sp)
                    }
                    SegmentedButton(
                        selected = isMonthlyMode,
                        onClick = { isMonthlyMode = true },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                    ) {
                        Text(if (isZh) "月趨勢" else "Monthly", fontSize = 12.sp)
                    }
                }
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                val calendar = Calendar.getInstance()
                val currentYear = calendar.get(Calendar.YEAR)
                val currentMonth = calendar.get(Calendar.MONTH)
                val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

                // Map data points
                val dataPoints: List<Pair<String, Double>> = if (!isMonthlyMode) {
                    val dailyMap = mutableMapOf<Int, Double>()
                    for (d in 1..daysInMonth) dailyMap[d] = 0.0

                    transactions.forEach { tx ->
                        if (tx.type == TransactionType.EXPENSE) {
                            val calTx = Calendar.getInstance().apply { timeInMillis = tx.date }
                            if (calTx.get(Calendar.YEAR) == currentYear && calTx.get(Calendar.MONTH) == currentMonth) {
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
                        val calPast = Calendar.getInstance().apply {
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

                val primaryColor = MaterialTheme.colorScheme.primary
                val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)

                val animProgress = remember { Animatable(0f) }
                LaunchedEffect(isMonthlyMode, transactions) {
                    animProgress.animateTo(1f, animationSpec = tween(700))
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isZh) "最高支出: $${String.format(Locale.getDefault(), "%,.0f", maxVal)} (${peakPoint?.first ?: ""})" else "Peak: $${String.format(Locale.getDefault(), "%,.0f", maxVal)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isZh) "平均: $${String.format(Locale.getDefault(), "%,.0f", avgVal)}" else "Avg: $${String.format(Locale.getDefault(), "%,.0f", avgVal)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    ) {
                        val w = size.width
                        val h = size.height
                        val pad = 16.dp.toPx()
                        val chartW = w - pad * 2
                        val chartH = h - pad * 2

                        // Grid horizontal lines
                        for (i in 0..3) {
                            val yLine = pad + (chartH / 3f) * i
                            drawLine(
                                color = gridColor,
                                start = Offset(pad, yLine),
                                end = Offset(w - pad, yLine),
                                strokeWidth = 1.dp.toPx()
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

                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(primaryColor.copy(alpha = 0.35f), Color.Transparent),
                                    startY = pad,
                                    endY = pad + chartH
                                )
                            )

                            drawPath(
                                path = linePath,
                                color = primaryColor,
                                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                            )

                            points.forEachIndexed { idx, pt ->
                                val valAmount = dataPoints[idx].second
                                if (valAmount > 0) {
                                    drawCircle(
                                        color = primaryColor,
                                        radius = 4.dp.toPx(),
                                        center = pt
                                    )
                                    drawCircle(
                                        color = Color.White,
                                        radius = 2.dp.toPx(),
                                        center = pt
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
