package com.example.debit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.debit.data.AppDatabase
import com.example.debit.data.DebitRepository
import com.example.debit.ui.DebitViewModel
import com.example.debit.ui.DebitViewModelFactory
import com.example.debit.ui.screens.DashboardScreen
import com.example.debit.ui.screens.OnboardingScreen
import com.example.debit.ui.theme.DebitTheme
import com.example.debit.ui.utils.requestHighRefreshRate

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestHighRefreshRate(this)

        val database = AppDatabase.getDatabase(applicationContext)
        val repository = DebitRepository(
            database.transactionDao(),
            database.budgetDao(),
            database.subscriptionDao(),
            database.savingsGoalDao()
        )
        val viewModelFactory = DebitViewModelFactory(application, repository)

        setContent {
            val viewModel: DebitViewModel = viewModel(factory = viewModelFactory)
            val uiState by viewModel.uiState.collectAsState()

            DebitTheme(themeColor = uiState.themeColor) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (!uiState.isInitialized) {
                        OnboardingScreen(viewModel = viewModel)
                    } else {
                        DashboardScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}
