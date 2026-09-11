package com.example.debit.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.debit.data.AppLanguage
import com.example.debit.data.Transaction
import com.example.debit.data.TransactionType
import com.example.debit.ui.utils.AppStrings
import com.example.debit.ui.utils.OrganicShapeChip
import com.example.debit.ui.utils.OrganicShapeMedium
import com.example.debit.ui.utils.bouncyClickable
import com.example.debit.ui.utils.getCategoryIcon
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Hand-Drawn Style Dashed Organic Border
 */
@Composable
fun HandDrawnBorder(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
) {
    Canvas(modifier = modifier) {
        val stroke = Stroke(
            width = 2.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
        )
        drawRoundRect(
            color = color,
            style = stroke,
            cornerRadius = CornerRadius(20.dp.toPx(), 20.dp.toPx())
        )
    }
}

/**
 * 1. Top Hero Card: Forest Green Estimated Savings Card
 */
@Composable
fun Modern3DSavingsHeroCard(
    totalBudget: Double,
    totalExpense: Double,
    remainingBudget: Double,
    language: AppLanguage,
    onOpenMonthlyBreakdown: () -> Unit
) {
    val isZh = language == AppLanguage.ZH
    val estimatedSavings = if (totalBudget > 0) maxOf(0.0, remainingBudget) else maxOf(0.0, totalBudget - totalExpense)
    val progress = if (totalBudget > 0) (totalExpense / totalBudget).toFloat().coerceIn(0f, 1f) else 0f
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "savingsProgress")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(16.dp, shape = RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF1A3E2C),
                        Color(0xFF143223),
                        Color(0xFF0E2419)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = Color(0xFF34D399).copy(alpha = 0.25f),
                shape = RoundedCornerShape(24.dp)
            )
            .bouncyClickable { onOpenMonthlyBreakdown() }
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isZh) "預估本月儲蓄 Estimated Savings" else "Estimated Savings",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = Color(0xFF34D399),
                    letterSpacing = 0.5.sp
                )
            }

            // Big Savings Amount
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "$${String.format(Locale.getDefault(), "%,.0f", estimatedSavings)}",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 38.sp
                    ),
                    color = Color.White
                )
                Text(
                    text = "TWD",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF34D399).copy(alpha = 0.8f),
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            // Progress Bar Track
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isZh) "儲蓄容量進度" else "Savings Capacity",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFD1FAE5).copy(alpha = 0.9f)
                    )
                    Text(
                        text = if (isZh) "剩餘 ${100 - (progress * 100).toInt()}%" else "Left ${100 - (progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF6EE7B7)
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF132A1E))
                        .border(1.dp, Color.White.copy(alpha = 0.05f), CircleShape)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedProgress)
                            .height(10.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF10B981), Color(0xFF34D399))
                                )
                            )
                            .shadow(12.dp, spotColor = Color(0xFF34D399), ambientColor = Color(0xFF34D399))
                    )
                }
            }

            // Footer Metrics Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.35f))
                    .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isZh) "已花費 Total Spent" else "Total Spent",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFA7F3D0).copy(alpha = 0.7f)
                    )
                    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "$${String.format(Locale.getDefault(), "%,.0f", totalExpense)}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "(${(progress * 100).toInt()}%)",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF6EE7B7).copy(alpha = 0.7f),
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }
                }

                Box(modifier = Modifier.width(1.dp).height(28.dp).background(Color.White.copy(alpha = 0.1f)))

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (isZh) "預算金庫 Total Budget" else "Total Budget",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFA7F3D0).copy(alpha = 0.7f)
                    )
                    Text(
                        text = "$${String.format(Locale.getDefault(), "%,.0f", totalBudget)}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }
        }
    }
}

/**
 * 2. Side-by-Side Stat Cards: Avg Spend & End Balance
 */
@Composable
fun Modern3DStatRow(
    totalExpense: Double,
    remainingBudget: Double,
    language: AppLanguage
) {
    val isZh = language == AppLanguage.ZH

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Left Stat Card: Total Expense
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isZh) "總花費 Spent" else "Spent",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "$${String.format(Locale.getDefault(), "%,.0f", totalExpense)}",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        // Right Stat Card: Remaining Balance
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.AccountBalanceWallet,
                        contentDescription = null,
                        tint = if (remainingBudget >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isZh) "預算剩餘 Remaining" else "Remaining",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "$${String.format(Locale.getDefault(), "%,.0f", remainingBudget)}",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = if (remainingBudget >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

/**
 * 3. Expenditure Trend Smooth Line Chart
 */
@Composable
fun Modern3DTrendChartCard(
    language: AppLanguage
) {
    val isZh = language == AppLanguage.ZH
    val primaryColor = MaterialTheme.colorScheme.primary
    val fillColor = primaryColor.copy(alpha = 0.15f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
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
                Text(
                    text = if (isZh) "消費趨勢 Expenditure Trend" else "Expenditure Trend",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "vs 3mo Avg",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Smooth Curve Canvas
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
            ) {
                val w = size.width
                val h = size.height
                val points = listOf(
                    Offset(0f, h * 0.4f),
                    Offset(w * 0.33f, h * 0.5f),
                    Offset(w * 0.66f, h * 0.35f),
                    Offset(w, h * 0.42f)
                )

                val linePath = Path().apply {
                    moveTo(points[0].x, points[0].y)
                    for (i in 0 until points.size - 1) {
                        val p1 = points[i]
                        val p2 = points[i + 1]
                        val controlPoint1 = Offset(p1.x + (p2.x - p1.x) / 2f, p1.y)
                        val controlPoint2 = Offset(p1.x + (p2.x - p1.x) / 2f, p2.y)
                        cubicTo(controlPoint1.x, controlPoint1.y, controlPoint2.x, controlPoint2.y, p2.x, p2.y)
                    }
                }

                val fillPath = Path().apply {
                    addPath(linePath)
                    lineTo(w, h)
                    lineTo(0f, h)
                    close()
                }

                drawPath(fillPath, brush = Brush.verticalGradient(listOf(fillColor, Color.Transparent)))
                drawPath(linePath, color = primaryColor, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))

                // Draw Dots
                points.forEach { pt ->
                    drawCircle(color = primaryColor, radius = 5.dp.toPx(), center = pt)
                    drawCircle(color = Color.White, radius = 2.5.dp.toPx(), center = pt)
                }
            }

            // X Axis Labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("12月 Dec", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("1月 Jan", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("2月 Feb", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("平均 Avg", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

val paletteColors = listOf(
    Color(0xFF386641),
    Color(0xFF2A9D8F),
    Color(0xFFE76F51),
    Color(0xFFE9C46A),
    Color(0xFF457B9D),
    Color(0xFF9D4EDD),
    Color(0xFFF4A261)
)

/**
 * 4. Spending Allocation Donut Chart (Dynamic Real Categories)
 */
@Composable
fun Modern3DAllocationCard(
    categoryExpenses: Map<String, Double>,
    totalExpense: Double,
    language: AppLanguage
) {
    val isZh = language == AppLanguage.ZH

    val activeCategories = categoryExpenses.filter { it.value > 0 }.entries.sortedByDescending { it.value }
    val effectiveTotal = if (totalExpense > 0) totalExpense else activeCategories.sumOf { it.value }

    val slices = if (activeCategories.isNotEmpty() && effectiveTotal > 0) {
        activeCategories.mapIndexed { index, entry ->
            val localizedName = AppStrings.getCategoryName(entry.key, language)
            val percent = (entry.value / effectiveTotal).toFloat()
            val color = paletteColors[index % paletteColors.size]
            Triple(localizedName, percent, color)
        }
    } else {
        listOf(
            Triple(if (isZh) "尚無花費" else "No Data", 1.0f, MaterialTheme.colorScheme.outlineVariant)
        )
    }

    val monthStr = SimpleDateFormat("MMMM", Locale.getDefault()).format(Date())

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
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
                Text(
                    text = if (isZh) "類別佔比 Spending Allocation" else "Spending Allocation",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$monthStr Actual",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                // Donut Ring Canvas with Center Label
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(130.dp)
                ) {
                    Canvas(modifier = Modifier.size(130.dp)) {
                        val stroke = 20.dp.toPx()
                        var startAngle = -90f

                        slices.forEach { (_, percent, color) ->
                            val sweep = percent * 360f
                            drawArc(
                                color = color,
                                startAngle = startAngle,
                                sweepAngle = if (slices.size > 1) maxOf(1f, sweep - 4f) else sweep,
                                useCenter = false,
                                style = Stroke(width = stroke, cap = StrokeCap.Round)
                            )
                            startAngle += sweep
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isZh) "總支出" else "Spent",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$${String.format(Locale.getDefault(), "%,.0f", totalExpense)}",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Legend Column
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    slices.take(4).forEach { (label, percent, color) ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(color, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "$label ${(percent * 100).toInt()}%",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 3D Modern Transaction Card Item
 */
@Composable
fun Modern3DTransactionItem(
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
            modifier = Modifier
                .fillMaxWidth()
                .shadow(3.dp, shape = OrganicShapeMedium)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    shape = OrganicShapeMedium
                ),
            shape = OrganicShapeMedium,
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
                    Surface(
                        shape = OrganicShapeChip,
                        color = if (isIncome) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                        shadowElevation = 3.dp
                    ) {
                        Box(
                            modifier = Modifier.size(42.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getCategoryIcon(transaction.category),
                                contentDescription = localizedCategory,
                                tint = if (isIncome) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(22.dp)
                            )
                        }
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
                                    shape = OrganicShapeChip,
                                    color = MaterialTheme.colorScheme.tertiaryContainer,
                                    shadowElevation = 2.dp
                                ) {
                                    Text(
                                        text = if (language == AppLanguage.ZH) "💰 生活費" else "💰 Pool",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            if (transaction.accountName.isNotBlank() && transaction.accountName != "現金") {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = OrganicShapeChip,
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    shadowElevation = 2.dp
                                ) {
                                    Text(
                                        text = transaction.accountName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
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