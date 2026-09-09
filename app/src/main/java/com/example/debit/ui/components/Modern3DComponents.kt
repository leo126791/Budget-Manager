package com.example.debit.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.geometry.Offset
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.debit.data.AppLanguage
import com.example.debit.data.Transaction
import com.example.debit.data.TransactionType
import com.example.debit.ui.utils.AppStrings
import com.example.debit.ui.utils.OrganicShapeChip
import com.example.debit.ui.utils.OrganicShapeLarge
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
 * 3D Modern Layered Budget Card
 */
@Composable
fun Modern3DBudgetCard(
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
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "modern3dProgress")

    val progressColor by animateColorAsState(
        targetValue = when {
            isOverBudget -> MaterialTheme.colorScheme.error
            isNearLimit -> Color(0xFFFF9800)
            else -> MaterialTheme.colorScheme.primary
        },
        label = "modern3dColor"
    )

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
            MaterialTheme.colorScheme.surfaceContainerHigh
        )
    )

    ElevatedCard(
        onClick = onOpenMonthlyBreakdown,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, shape = OrganicShapeLarge)
            .border(
                width = 1.5.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                shape = OrganicShapeLarge
            ),
        shape = OrganicShapeLarge,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = OrganicShapeChip,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shadowElevation = 4.dp
                    ) {
                        Box(
                            modifier = Modifier.padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AccountBalanceWallet,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = AppStrings.get("monthly_status", language),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (totalBudget <= 0) {
                    OutlinedButton(
                        onClick = onOpenBudgetSettings,
                        shape = OrganicShapeChip
                    ) {
                        Text(AppStrings.get("set_budget", language), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                } else if (isOverBudget) {
                    Surface(
                        color = MaterialTheme.colorScheme.error,
                        shape = OrganicShapeChip,
                        shadowElevation = 4.dp
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

                // 3D Progress Bar Track
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .shadow(2.dp, shape = RoundedCornerShape(6.dp))
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedProgress)
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(progressColor)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "已使用 ${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "小金庫目標 $${String.format(Locale.getDefault(), "%,.0f", totalBudget)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
