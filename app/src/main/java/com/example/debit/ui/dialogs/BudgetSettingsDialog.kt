package com.example.debit.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun BudgetSettingsDialog(
    initialTotalBudget: Double,
    onDismissRequest: () -> Unit,
    onSaveBudget: (amountLimit: Double) -> Unit
) {
    var totalBudgetInput by remember {
        mutableStateOf(if (initialTotalBudget > 0) initialTotalBudget.toInt().toString() else "")
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(text = "設定月度預算", style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "設定本月總預算上限，系統將會自動計算總支出並提供進度警示。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = totalBudgetInput,
                    onValueChange = { totalBudgetInput = it },
                    label = { Text("全月總預算 ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val totalLimit = totalBudgetInput.toDoubleOrNull() ?: 0.0
                    onSaveBudget(totalLimit)
                    onDismissRequest()
                }
            ) {
                Text("完成設定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("取消")
            }
        }
    )
}
