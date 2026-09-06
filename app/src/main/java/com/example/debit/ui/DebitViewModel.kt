package com.example.debit.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.debit.data.AppLanguage
import com.example.debit.data.AppThemeColor
import com.example.debit.data.Budget
import com.example.debit.data.DebitRepository
import com.example.debit.data.SettingsPreferences
import com.example.debit.data.Transaction
import com.example.debit.data.TransactionType
import com.example.debit.ui.utils.BackupUtils
import com.example.debit.widget.BudgetWidgetProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DebitUiState(
    val currentYearMonth: String = "",
    val availableMonths: List<String> = emptyList(),
    val isCurrentMonth: Boolean = true,
    val transactions: List<Transaction> = emptyList(),
    val totalExpense: Double = 0.0,
    val todayExpense: Double = 0.0,
    val totalBudgetLimit: Double = 0.0,
    val categoryExpenses: Map<String, Double> = emptyMap(),
    val themeColor: AppThemeColor = AppThemeColor.INDIGO,
    val appLanguage: AppLanguage = AppLanguage.ZH,
    val isBetaTestingEnabled: Boolean = false,
    val locationPredictionEnabled: Boolean = false,
    val recentLocations: List<String> = emptyList(),
    val autoBackupEnabled: Boolean = true,
    val lastAutoBackupTime: Long = 0L
) {
    val remainingBudget: Double
        get() = totalBudgetLimit - totalExpense

    val totalBudgetProgress: Float
        get() = if (totalBudgetLimit > 0) (totalExpense / totalBudgetLimit).toFloat().coerceIn(0f, 1f) else 0f

    val isOverBudget: Boolean
        get() = totalBudgetLimit > 0 && totalExpense > totalBudgetLimit

    val isNearBudgetLimit: Boolean
        get() = totalBudgetLimit > 0 && totalBudgetProgress >= 0.8f && !isOverBudget
}

class DebitViewModel(
    application: Application,
    private val repository: DebitRepository
) : AndroidViewModel(application) {

    private val settingsPrefs = SettingsPreferences(application)

    val systemYearMonthStr: String
        get() = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())

    private val _selectedYearMonth = MutableStateFlow(systemYearMonthStr)
    val selectedYearMonth: StateFlow<String> = _selectedYearMonth

    private val _themeColor = MutableStateFlow(settingsPrefs.getThemeColor())
    val themeColor: StateFlow<AppThemeColor> = _themeColor

    private val _appLanguage = MutableStateFlow(settingsPrefs.getLanguage())
    val appLanguage: StateFlow<AppLanguage> = _appLanguage

    private val _betaTestingEnabled = MutableStateFlow(settingsPrefs.isBetaTestingEnabled())
    val betaTestingEnabled: StateFlow<Boolean> = _betaTestingEnabled

    private val _locationPredictionEnabled = MutableStateFlow(settingsPrefs.isLocationPredictionEnabled())
    val locationPredictionEnabled: StateFlow<Boolean> = _locationPredictionEnabled

    private val _autoBackupEnabled = MutableStateFlow(settingsPrefs.isAutoBackupEnabled())
    val autoBackupEnabled: StateFlow<Boolean> = _autoBackupEnabled

    private val _lastAutoBackupTime = MutableStateFlow(settingsPrefs.getLastAutoBackupTime())
    val lastAutoBackupTime: StateFlow<Long> = _lastAutoBackupTime

    val uiState: StateFlow<DebitUiState> = combine(
        repository.allTransactions,
        repository.globalBudget,
        _selectedYearMonth,
        _themeColor,
        _appLanguage
    ) { transactions, globalBudget, selectedYM, themeColor, language ->
        val betaEnabled = _betaTestingEnabled.value
        val locationEnabled = betaEnabled && _locationPredictionEnabled.value
        val autoBackupOn = _autoBackupEnabled.value
        val lastBackupTime = _lastAutoBackupTime.value

        val currentMonthTransactions = transactions.filter {
            SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date(it.date)) == selectedYM
        }

        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        var totalExp = 0.0
        var todayExp = 0.0
        val catExpenses = mutableMapOf<String, Double>()

        for (tx in currentMonthTransactions) {
            totalExp += tx.amount
            catExpenses[tx.category] = (catExpenses[tx.category] ?: 0.0) + tx.amount

            val txDayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(tx.date))
            if (txDayStr == todayStr) {
                todayExp += tx.amount
            }
        }

        val totalBgt = globalBudget?.amountLimit ?: 0.0

        val monthsInDb = transactions.map {
            SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date(it.date))
        }
        val allMonths = (monthsInDb + systemYearMonthStr).distinct().sortedDescending()

        val recentLocs = transactions
            .map { it.locationName.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .take(6)

        DebitUiState(
            currentYearMonth = selectedYM,
            availableMonths = allMonths,
            isCurrentMonth = selectedYM == systemYearMonthStr,
            transactions = currentMonthTransactions,
            totalExpense = totalExp,
            todayExpense = todayExp,
            totalBudgetLimit = totalBgt,
            categoryExpenses = catExpenses,
            themeColor = themeColor,
            appLanguage = language,
            isBetaTestingEnabled = betaEnabled,
            locationPredictionEnabled = locationEnabled,
            recentLocations = recentLocs,
            autoBackupEnabled = autoBackupOn,
            lastAutoBackupTime = lastBackupTime
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DebitUiState(
            currentYearMonth = systemYearMonthStr,
            themeColor = settingsPrefs.getThemeColor(),
            appLanguage = settingsPrefs.getLanguage(),
            isBetaTestingEnabled = settingsPrefs.isBetaTestingEnabled(),
            locationPredictionEnabled = settingsPrefs.isBetaTestingEnabled() && settingsPrefs.isLocationPredictionEnabled(),
            autoBackupEnabled = settingsPrefs.isAutoBackupEnabled(),
            lastAutoBackupTime = settingsPrefs.getLastAutoBackupTime()
        )
    )

    fun getLocationEstimate(locationQuery: String): Pair<Double, Int> {
        if (locationQuery.isBlank()) return Pair(0.0, 0)
        val query = locationQuery.trim().lowercase(Locale.getDefault())
        val allTx = uiState.value.transactions
        val matches = allTx.filter {
            it.locationName.trim().lowercase(Locale.getDefault()) == query ||
            (it.locationName.isBlank() && it.note.trim().lowercase(Locale.getDefault()).contains(query))
        }
        if (matches.isEmpty()) return Pair(0.0, 0)
        val avg = matches.sumOf { it.amount } / matches.size
        return Pair(avg, matches.size)
    }

    fun selectYearMonth(yearMonth: String) {
        _selectedYearMonth.value = yearMonth
    }

    fun resetToCurrentMonth() {
        _selectedYearMonth.value = systemYearMonthStr
    }

    private fun triggerAutoBackup() {
        if (settingsPrefs.isAutoBackupEnabled()) {
            viewModelScope.launch(Dispatchers.IO) {
                val allTx = repository.allTransactions.first()
                val monthBgts = repository.getBudgetsForMonth("GLOBAL").first()
                BackupUtils.performAutoBackup(getApplication(), allTx, monthBgts)
                _lastAutoBackupTime.value = settingsPrefs.getLastAutoBackupTime()
            }
        }
    }

    fun restoreBackupJson(jsonContent: String, onResult: (txCount: Int) -> Unit) {
        viewModelScope.launch {
            try {
                val (txList, bgtList) = BackupUtils.parseBackupJson(jsonContent)
                txList.forEach { repository.addTransaction(it) }
                bgtList.forEach { repository.setBudget(it) }
                BudgetWidgetProvider.updateAllWidgets(getApplication())
                triggerAutoBackup()
                onResult(txList.size)
            } catch (_: Exception) {
                onResult(-1)
            }
        }
    }

    fun saveSettings(
        budgetLimit: Double,
        color: AppThemeColor,
        language: AppLanguage,
        betaTestingEnabled: Boolean,
        locationPredictionEnabled: Boolean,
        autoBackupEnabled: Boolean
    ) {
        viewModelScope.launch {
            val effectiveLocationPrediction = betaTestingEnabled && locationPredictionEnabled

            settingsPrefs.setThemeColor(color)
            settingsPrefs.setLanguage(language)
            settingsPrefs.setBetaTestingEnabled(betaTestingEnabled)
            settingsPrefs.setLocationPredictionEnabled(effectiveLocationPrediction)
            settingsPrefs.setAutoBackupEnabled(autoBackupEnabled)

            _themeColor.value = color
            _appLanguage.value = language
            _betaTestingEnabled.value = betaTestingEnabled
            _locationPredictionEnabled.value = effectiveLocationPrediction
            _autoBackupEnabled.value = autoBackupEnabled

            repository.setBudget(
                Budget(
                    category = "TOTAL",
                    amountLimit = budgetLimit,
                    yearMonth = "GLOBAL"
                )
            )
            BudgetWidgetProvider.updateAllWidgets(getApplication())
            triggerAutoBackup()
        }
    }

    fun addTransaction(
        amount: Double,
        category: String,
        note: String,
        date: Long = System.currentTimeMillis(),
        locationName: String = ""
    ) {
        viewModelScope.launch {
            repository.addTransaction(
                Transaction(
                    amount = amount,
                    category = category,
                    type = TransactionType.EXPENSE,
                    note = note,
                    date = date,
                    locationName = locationName
                )
            )
            BudgetWidgetProvider.updateAllWidgets(getApplication())
            triggerAutoBackup()
        }
    }

    fun updateTransaction(
        id: Long,
        amount: Double,
        category: String,
        note: String,
        date: Long,
        locationName: String = ""
    ) {
        viewModelScope.launch {
            repository.updateTransaction(
                Transaction(
                    id = id,
                    amount = amount,
                    category = category,
                    type = TransactionType.EXPENSE,
                    note = note,
                    date = date,
                    locationName = locationName
                )
            )
            BudgetWidgetProvider.updateAllWidgets(getApplication())
            triggerAutoBackup()
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
            BudgetWidgetProvider.updateAllWidgets(getApplication())
            triggerAutoBackup()
        }
    }

    fun setBudget(amountLimit: Double) {
        viewModelScope.launch {
            repository.setBudget(
                Budget(
                    category = "TOTAL",
                    amountLimit = amountLimit,
                    yearMonth = "GLOBAL"
                )
            )
            BudgetWidgetProvider.updateAllWidgets(getApplication())
            triggerAutoBackup()
        }
    }
}

class DebitViewModelFactory(
    private val application: Application,
    private val repository: DebitRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DebitViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DebitViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
