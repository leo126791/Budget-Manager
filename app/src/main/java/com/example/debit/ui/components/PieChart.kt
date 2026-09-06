package com.example.debit.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.debit.data.AppLanguage
import com.example.debit.data.Transaction
import com.example.debit.ui.utils.AppStrings
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

val pieChartColors = listOf(
    Color(0xFFFF5722), // Deep Orange
    Color(0xFF2196F3), // Blue
    Color(0xFFFFEB3B), // Yellow
    Color(0xFF9C27B0), // Purple
    Color(0xFF4CAF50), // Green
    Color(0xFFE91E63), // Pink
    Color(0xFF00BCD4), // Cyan
    Color(0xFFFF9800), // Orange
    Color(0xFF607D8B)  // Blue Grey
)

@Composable
fun ExpensePieChartCard(
    categoryExpenses: Map<String, Double>,
    totalExpense: Double,
    modifier: Modifier = Modifier,
    transactions: List<Transaction> = emptyList(),
    totalBudget: Double = 0.0,
    language: AppLanguage = AppLanguage.ZH
) {
    if ((totalExpense <= 0 && totalBudget <= 0) || (categoryExpenses.isEmpty() && totalBudget <= 0)) return

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

            if (selectedChartIndex == 0) {
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
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }

        // Legend Items Column
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(start = 12.dp)
        ) {
            sortedEntries.take(4).forEachIndexed { index, entry ->
                val color = pieChartColors[index % pieChartColors.size]
                val percentage = ((entry.value / baseAmount) * 100).toInt()
                val catName = AppStrings.getCategoryName(entry.key, language)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(color, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$catName ($percentage%)",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$${String.format(Locale.getDefault(), "%,.0f", entry.value)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (totalBudget > 0 && remainingBudget > 0) {
                val remainingPercent = ((remainingBudget / totalBudget) * 100).toInt()
                val unusedLabel = AppStrings.get("unused_budget", language)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(trackColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$unusedLabel ($remainingPercent%)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$${String.format(Locale.getDefault(), "%,.0f", remainingBudget)}",
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
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val today = calendar.get(Calendar.DAY_OF_MONTH)
    val currentYM = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())

    val dailyMap = mutableMapOf<Int, Double>()
    for (day in 1..daysInMonth) {
        dailyMap[day] = 0.0
    }

    val dayFormat = SimpleDateFormat("d", Locale.getDefault())

    transactions.forEach { tx ->
        val txYM = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date(tx.date))
        if (txYM == currentYM) {
            val dayInt = dayFormat.format(Date(tx.date)).toIntOrNull() ?: 1
            dailyMap[dayInt] = (dailyMap[dayInt] ?: 0.0) + tx.amount
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
