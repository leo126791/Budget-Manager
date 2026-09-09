package com.example.debit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.lifecycle.lifecycleScope
import com.example.debit.data.AppDatabase
import com.example.debit.data.DebitRepository
import com.example.debit.data.Transaction
import com.example.debit.data.TransactionType
import com.example.debit.ui.dialogs.AddTransactionDialog
import com.example.debit.ui.theme.DebitTheme
import com.example.debit.ui.utils.requestHighRefreshRate
import com.example.debit.widget.BudgetWidgetProvider
import kotlinx.coroutines.launch

class QuickAddActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestHighRefreshRate(this)

        val database = AppDatabase.getDatabase(applicationContext)
        val repository = DebitRepository(
            database.transactionDao(),
            database.budgetDao(),
            database.subscriptionDao(),
            database.savingsGoalDao()
        )

        setContent {
            DebitTheme {
                Surface(
                    color = MaterialTheme.colorScheme.background.copy(alpha = 0f)
                ) {
                    AddTransactionDialog(
                        onDismissRequest = { finish() },
                        onConfirm = { amount, category, note, date, locationName, deductFromPool, accountName, type ->
                            lifecycleScope.launch {
                                repository.addTransaction(
                                    Transaction(
                                        amount = amount,
                                        category = category,
                                        type = type,
                                        note = note,
                                        date = date,
                                        locationName = locationName,
                                        deductFromPool = deductFromPool,
                                        accountName = accountName
                                    )
                                )
                                BudgetWidgetProvider.updateAllWidgets(applicationContext)
                                finish()
                            }
                        }
                    )
                }
            }
        }
    }
}
