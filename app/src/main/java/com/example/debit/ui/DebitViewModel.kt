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
import com.example.debit.data.SavingsGoal
import com.example.debit.data.SettingsPreferences
import com.example.debit.data.Subscription
import com.example.debit.data.Transaction
import com.example.debit.data.TransactionType
import com.example.debit.ui.utils.BackupUtils
import com.example.debit.ui.utils.DateFormatUtils
import com.example.debit.widget.BudgetWidgetProvider
import com.example.debit.widget.QuickAddWidgetProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private data class DataTuple(
    val transactions: List<Transaction>,
    val globalBudget: Budget?,
    val subscriptions: List<Subscription>,
    val savingsGoals: List<SavingsGoal>
)

private data class PrefsTuple(
    val selectedYM: String,
    val themeColor: AppThemeColor,
    val language: AppLanguage,
    val searchQuery: String
)

data class DebitUiState(
    val currentYearMonth: String = "",
    val availableMonths: List<String> = emptyList(),
    val isCurrentMonth: Boolean = true,
    val transactions: List<Transaction> = emptyList(),
    val filteredTransactions: List<Transaction> = emptyList(),
    val totalExpense: Double = 0.0,
    val totalIncome: Double = 0.0,
    val netSavings: Double = 0.0,
    val todayExpense: Double = 0.0,
    val totalBudgetLimit: Double = 0.0,
    val categoryExpenses: Map<String, Double> = emptyMap(),
    val themeColor: AppThemeColor = AppThemeColor.INDIGO,
    val appLanguage: AppLanguage = AppLanguage.ZH,
    val searchQuery: String = "",
    val subscriptions: List<Subscription> = emptyList(),
    val totalSubscriptionsMonthly: Double = 0.0,
    val savingsGoals: List<SavingsGoal> = emptyList(),
    val isBetaTestingEnabled: Boolean = false,
    val isLivingExpensePoolEnabled: Boolean = false,
    val livingExpensePool: Double = 0.0,
    val isIncomeTrackingEnabled: Boolean = true,
    val isSearchEnabled: Boolean = true,
    val isSubscriptionEnabled: Boolean = true,
    val isMultiAccountEnabled: Boolean = true,
    val isSavingsGoalsEnabled: Boolean = true,
    val isModern3DUiEnabled: Boolean = true,
    val isDragDateReorderEnabled: Boolean = true,
    val autoBackupEnabled: Boolean = true,
    val lastAutoBackupTime: Long = 0L,
    val isInitialized: Boolean = true
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
        get() = DateFormatUtils.formatYM(Date())

    private val _selectedYearMonth = MutableStateFlow(systemYearMonthStr)
    val selectedYearMonth: StateFlow<String> = _selectedYearMonth

    private val _themeColor = MutableStateFlow(settingsPrefs.getThemeColor())
    val themeColor: StateFlow<AppThemeColor> = _themeColor

    private val _appLanguage = MutableStateFlow(settingsPrefs.getLanguage())
    val appLanguage: StateFlow<AppLanguage> = _appLanguage

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _betaTestingEnabled = MutableStateFlow(settingsPrefs.isBetaTestingEnabled())
    val betaTestingEnabled: StateFlow<Boolean> = _betaTestingEnabled

    private val _livingExpensePoolEnabled = MutableStateFlow(settingsPrefs.isLivingExpensePoolEnabled())
    val livingExpensePoolEnabled: StateFlow<Boolean> = _livingExpensePoolEnabled

    private val _incomeTrackingEnabled = MutableStateFlow(settingsPrefs.isIncomeTrackingEnabled())
    private val _searchEnabled = MutableStateFlow(settingsPrefs.isSearchEnabled())
    private val _subscriptionEnabled = MutableStateFlow(settingsPrefs.isSubscriptionEnabled())
    private val _multiAccountEnabled = MutableStateFlow(settingsPrefs.isMultiAccountEnabled())
    private val _savingsGoalsEnabled = MutableStateFlow(settingsPrefs.isSavingsGoalsEnabled())
    private val _modern3DUiEnabled = MutableStateFlow(settingsPrefs.isModern3DUiEnabled())
    private val _dragDateReorderEnabled = MutableStateFlow(settingsPrefs.isDragDateReorderEnabled())

    private val _autoBackupEnabled = MutableStateFlow(settingsPrefs.isAutoBackupEnabled())
    val autoBackupEnabled: StateFlow<Boolean> = _autoBackupEnabled

    private val _lastAutoBackupTime = MutableStateFlow(settingsPrefs.getLastAutoBackupTime())
    val lastAutoBackupTime: StateFlow<Long> = _lastAutoBackupTime

    private val _isInitialized = MutableStateFlow(settingsPrefs.isInitialized())
    val isInitialized: StateFlow<Boolean> = _isInitialized

    val uiState: StateFlow<DebitUiState> = combine(
        combine(
            repository.allTransactions,
            repository.globalBudget,
            repository.allSubscriptions,
            repository.allSavingsGoals
        ) { txs, budget, subs, goals ->
            DataTuple(txs, budget, subs, goals)
        },
        combine(
            _selectedYearMonth,
            _themeColor,
            _appLanguage,
            _searchQuery
        ) { ym, theme, lang, query ->
            PrefsTuple(ym, theme, lang, query)
        }
    ) { dataTuple, prefsTuple ->
        val (transactions, globalBudget, subscriptions, savingsGoals) = dataTuple
        val (selectedYM, themeColor, language, query) = prefsTuple

        val betaEnabled = _betaTestingEnabled.value
        val poolEnabled = betaEnabled && _livingExpensePoolEnabled.value
        val incomeEnabled = betaEnabled && _incomeTrackingEnabled.value
        val searchEnabled = betaEnabled && _searchEnabled.value
        val subscriptionEnabled = betaEnabled && _subscriptionEnabled.value
        val multiAccountEnabled = betaEnabled && _multiAccountEnabled.value
        val savingsGoalsEnabled = betaEnabled && _savingsGoalsEnabled.value
        val modern3DUiEnabled = betaEnabled && _modern3DUiEnabled.value
        val dragDateReorderEnabled = betaEnabled && _dragDateReorderEnabled.value

        val autoBackupOn = _autoBackupEnabled.value
        val lastBackupTime = _lastAutoBackupTime.value
        val initialized = _isInitialized.value

        val currentMonthTransactions = transactions.filter {
            DateFormatUtils.formatYM(Date(it.date)) == selectedYM
        }

        val filteredTx = if (searchEnabled && query.isNotBlank()) {
            val q = query.trim().lowercase(Locale.getDefault())
            currentMonthTransactions.filter {
                it.category.lowercase(Locale.getDefault()).contains(q) ||
                it.note.lowercase(Locale.getDefault()).contains(q) ||
                it.locationName.lowercase(Locale.getDefault()).contains(q) ||
                it.accountName.lowercase(Locale.getDefault()).contains(q)
            }
        } else {
            currentMonthTransactions
        }

        val todayStr = DateFormatUtils.formatYMD(Date())
        var totalExp = 0.0
        var totalInc = 0.0
        var todayExp = 0.0
        var pastDaysExpense = 0.0
        var poolSpent = 0.0
        val catExpenses = mutableMapOf<String, Double>()

        val calendar = Calendar.getInstance()
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        for (tx in currentMonthTransactions) {
            if (tx.type == TransactionType.INCOME) {
                if (incomeEnabled) {
                    totalInc += tx.amount
                }
            } else {
                catExpenses[tx.category] = (catExpenses[tx.category] ?: 0.0) + tx.amount

                if (tx.deductFromPool) {
                    poolSpent += tx.amount
                } else {
                    totalExp += tx.amount

                    val txDate = Date(tx.date)
                    val txDayStr = DateFormatUtils.formatYMD(txDate)
                    if (txDayStr == todayStr) {
                        todayExp += tx.amount
                    } else {
                        val txCal = Calendar.getInstance().apply { time = txDate }
                        if (txCal.get(Calendar.DAY_OF_MONTH) < currentDay) {
                            pastDaysExpense += tx.amount
                        }
                    }
                }
            }
        }

        val totalBgt = globalBudget?.amountLimit ?: 0.0
        val dailyLimit = if (totalBgt > 0) totalBgt / daysInMonth else 0.0
        val pastAllocated = maxOf(0, currentDay - 1) * dailyLimit
        val poolAmount = if (poolEnabled && totalBgt > 0) (pastAllocated - pastDaysExpense - poolSpent) else 0.0
        val monthlySubs = subscriptions.sumOf { if (it.isAnnual) it.amount / 12.0 else it.amount }

        val monthsInDb = transactions.map {
            DateFormatUtils.formatYM(Date(it.date))
        }
        val allMonths = (monthsInDb + systemYearMonthStr).distinct().sortedDescending()

        DebitUiState(
            currentYearMonth = selectedYM,
            availableMonths = allMonths,
            isCurrentMonth = selectedYM == systemYearMonthStr,
            transactions = currentMonthTransactions,
            filteredTransactions = filteredTx,
            totalExpense = totalExp,
            totalIncome = totalInc,
            netSavings = totalInc - totalExp,
            todayExpense = todayExp,
            totalBudgetLimit = totalBgt,
            categoryExpenses = catExpenses,
            themeColor = themeColor,
            appLanguage = language,
            searchQuery = query,
            subscriptions = subscriptions,
            totalSubscriptionsMonthly = monthlySubs,
            savingsGoals = savingsGoals,
            isBetaTestingEnabled = betaEnabled,
            isLivingExpensePoolEnabled = poolEnabled,
            livingExpensePool = poolAmount,
            isIncomeTrackingEnabled = incomeEnabled,
            isSearchEnabled = searchEnabled,
            isSubscriptionEnabled = subscriptionEnabled,
            isMultiAccountEnabled = multiAccountEnabled,
            isSavingsGoalsEnabled = savingsGoalsEnabled,
            isModern3DUiEnabled = modern3DUiEnabled,
            isDragDateReorderEnabled = dragDateReorderEnabled,
            autoBackupEnabled = autoBackupOn,
            lastAutoBackupTime = lastBackupTime,
            isInitialized = initialized
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DebitUiState(
            currentYearMonth = systemYearMonthStr,
            themeColor = settingsPrefs.getThemeColor(),
            appLanguage = settingsPrefs.getLanguage(),
            isBetaTestingEnabled = settingsPrefs.isBetaTestingEnabled(),
            isLivingExpensePoolEnabled = settingsPrefs.isBetaTestingEnabled() && settingsPrefs.isLivingExpensePoolEnabled(),
            isIncomeTrackingEnabled = settingsPrefs.isBetaTestingEnabled() && settingsPrefs.isIncomeTrackingEnabled(),
            isSearchEnabled = settingsPrefs.isBetaTestingEnabled() && settingsPrefs.isSearchEnabled(),
            isSubscriptionEnabled = settingsPrefs.isBetaTestingEnabled() && settingsPrefs.isSubscriptionEnabled(),
            isMultiAccountEnabled = settingsPrefs.isBetaTestingEnabled() && settingsPrefs.isMultiAccountEnabled(),
            isSavingsGoalsEnabled = settingsPrefs.isBetaTestingEnabled() && settingsPrefs.isSavingsGoalsEnabled(),
            isModern3DUiEnabled = settingsPrefs.isBetaTestingEnabled() && settingsPrefs.isModern3DUiEnabled(),
            isDragDateReorderEnabled = settingsPrefs.isBetaTestingEnabled() && settingsPrefs.isDragDateReorderEnabled(),
            autoBackupEnabled = settingsPrefs.isAutoBackupEnabled(),
            lastAutoBackupTime = settingsPrefs.getLastAutoBackupTime(),
            isInitialized = settingsPrefs.isInitialized()
        )
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
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
        livingExpensePoolEnabled: Boolean,
        incomeTrackingEnabled: Boolean,
        searchEnabled: Boolean,
        subscriptionEnabled: Boolean,
        multiAccountEnabled: Boolean,
        savingsGoalsEnabled: Boolean,
        modern3DUiEnabled: Boolean,
        dragDateReorderEnabled: Boolean,
        autoBackupEnabled: Boolean
    ) {
        viewModelScope.launch {
            val effPool = betaTestingEnabled && livingExpensePoolEnabled
            val effIncome = betaTestingEnabled && incomeTrackingEnabled
            val effSearch = betaTestingEnabled && searchEnabled
            val effSub = betaTestingEnabled && subscriptionEnabled
            val effMultiAcc = betaTestingEnabled && multiAccountEnabled
            val effSavings = betaTestingEnabled && savingsGoalsEnabled
            val effModern3D = betaTestingEnabled && modern3DUiEnabled
            val effDragDate = betaTestingEnabled && dragDateReorderEnabled

            settingsPrefs.setThemeColor(color)
            settingsPrefs.setLanguage(language)
            settingsPrefs.setBetaTestingEnabled(betaTestingEnabled)
            settingsPrefs.setLivingExpensePoolEnabled(effPool)
            settingsPrefs.setIncomeTrackingEnabled(effIncome)
            settingsPrefs.setSearchEnabled(effSearch)
            settingsPrefs.setSubscriptionEnabled(effSub)
            settingsPrefs.setMultiAccountEnabled(effMultiAcc)
            settingsPrefs.setSavingsGoalsEnabled(effSavings)
            settingsPrefs.setModern3DUiEnabled(effModern3D)
            settingsPrefs.setDragDateReorderEnabled(effDragDate)
            settingsPrefs.setAutoBackupEnabled(autoBackupEnabled)

            _themeColor.value = color
            _appLanguage.value = language
            _betaTestingEnabled.value = betaTestingEnabled
            _livingExpensePoolEnabled.value = effPool
            _incomeTrackingEnabled.value = effIncome
            _searchEnabled.value = effSearch
            _subscriptionEnabled.value = effSub
            _multiAccountEnabled.value = effMultiAcc
            _savingsGoalsEnabled.value = effSavings
            _modern3DUiEnabled.value = effModern3D
            _dragDateReorderEnabled.value = effDragDate
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
        locationName: String = "",
        deductFromPool: Boolean = false,
        accountName: String = "現金",
        type: TransactionType = TransactionType.EXPENSE
    ) {
        viewModelScope.launch {
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
        locationName: String = "",
        deductFromPool: Boolean = false,
        accountName: String = "現金",
        type: TransactionType = TransactionType.EXPENSE
    ) {
        viewModelScope.launch {
            repository.updateTransaction(
                Transaction(
                    id = id,
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
            BudgetWidgetProvider.updateAllWidgets(getApplication())
            triggerAutoBackup()
        }
    }

    fun moveTransactionToDate(transaction: Transaction, newDateMillis: Long) {
        viewModelScope.launch {
            val updated = transaction.copy(date = newDateMillis)
            repository.updateTransaction(updated)
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

    fun addSubscription(name: String, amount: Double, billingDay: Int, billingMonth: Int = 1, isAnnual: Boolean = false) {
        viewModelScope.launch {
            repository.addSubscription(Subscription(name = name, amount = amount, billingDay = billingDay, billingMonth = billingMonth, isAnnual = isAnnual))
        }
    }

    fun deleteSubscription(subscription: Subscription) {
        viewModelScope.launch {
            repository.deleteSubscription(subscription)
        }
    }

    fun addSavingsGoal(title: String, targetAmount: Double, initialAmount: Double) {
        viewModelScope.launch {
            repository.addSavingsGoal(SavingsGoal(title = title, targetAmount = targetAmount, currentAmount = initialAmount))
        }
    }

    fun depositToSavingsGoal(savingsGoal: SavingsGoal, amount: Double) {
        viewModelScope.launch {
            val updated = savingsGoal.copy(currentAmount = savingsGoal.currentAmount + amount)
            repository.updateSavingsGoal(updated)
        }
    }

    fun deleteSavingsGoal(savingsGoal: SavingsGoal) {
        viewModelScope.launch {
            repository.deleteSavingsGoal(savingsGoal)
        }
    }

    fun getCustomSubCategories(category: String): List<String> {
        return settingsPrefs.getCustomSubCategories(category)
    }

    fun addCustomSubCategory(category: String, subCategory: String) {
        settingsPrefs.addCustomSubCategory(category, subCategory)
    }

    fun setBudgetLimit(amountLimit: Double) {
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

    fun setThemeColor(color: AppThemeColor) {
        viewModelScope.launch {
            settingsPrefs.setThemeColor(color)
            _themeColor.value = color
            BudgetWidgetProvider.updateAllWidgets(getApplication())
        }
    }

    fun setAppLanguage(language: AppLanguage) {
        viewModelScope.launch {
            settingsPrefs.setLanguage(language)
            _appLanguage.value = language
        }
    }

    fun setBetaTestingEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsPrefs.setBetaTestingEnabled(enabled)
            _betaTestingEnabled.value = enabled
        }
    }

    fun setLivingExpensePoolEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsPrefs.setLivingExpensePoolEnabled(enabled)
            _livingExpensePoolEnabled.value = enabled
        }
    }

    fun setIncomeTrackingEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsPrefs.setIncomeTrackingEnabled(enabled)
            _incomeTrackingEnabled.value = enabled
        }
    }

    fun setSearchEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsPrefs.setSearchEnabled(enabled)
            _searchEnabled.value = enabled
        }
    }

    fun setSubscriptionEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsPrefs.setSubscriptionEnabled(enabled)
            _subscriptionEnabled.value = enabled
        }
    }

    fun setMultiAccountEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsPrefs.setMultiAccountEnabled(enabled)
            _multiAccountEnabled.value = enabled
        }
    }

    fun setSavingsGoalsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsPrefs.setSavingsGoalsEnabled(enabled)
            _savingsGoalsEnabled.value = enabled
        }
    }

    fun setModern3DUiEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsPrefs.setModern3DUiEnabled(enabled)
            _modern3DUiEnabled.value = enabled
        }
    }

    fun setAutoBackupEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsPrefs.setAutoBackupEnabled(enabled)
            _autoBackupEnabled.value = enabled
        }
    }

    fun completeOnboarding(
        budgetLimit: Double,
        color: AppThemeColor,
        language: AppLanguage,
        betaTestingEnabled: Boolean = true
    ) {
        viewModelScope.launch {
            settingsPrefs.setThemeColor(color)
            settingsPrefs.setLanguage(language)
            settingsPrefs.setBetaTestingEnabled(betaTestingEnabled)
            settingsPrefs.setInitialized(true)

            _themeColor.value = color
            _appLanguage.value = language
            _betaTestingEnabled.value = betaTestingEnabled
            _isInitialized.value = true

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
