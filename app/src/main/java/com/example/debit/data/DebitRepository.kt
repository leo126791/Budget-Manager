package com.example.debit.data

import kotlinx.coroutines.flow.Flow

class DebitRepository(
    private val transactionDao: TransactionDao,
    private val budgetDao: BudgetDao,
    private val subscriptionDao: SubscriptionDao,
    private val savingsGoalDao: SavingsGoalDao
) {
    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()
    val globalBudget: Flow<Budget?> = budgetDao.getGlobalTotalBudget()
    val allSubscriptions: Flow<List<Subscription>> = subscriptionDao.getAllSubscriptions()
    val allSavingsGoals: Flow<List<SavingsGoal>> = savingsGoalDao.getAllSavingsGoals()

    fun getBudgetsForMonth(yearMonth: String): Flow<List<Budget>> {
        return budgetDao.getBudgetsByMonth(yearMonth)
    }

    suspend fun addTransaction(transaction: Transaction) {
        transactionDao.insertTransaction(transaction)
    }

    suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction)
    }

    suspend fun setBudget(budget: Budget) {
        budgetDao.insertOrUpdateBudget(budget)
    }

    suspend fun addSubscription(subscription: Subscription) {
        subscriptionDao.insertSubscription(subscription)
    }

    suspend fun deleteSubscription(subscription: Subscription) {
        subscriptionDao.deleteSubscription(subscription)
    }

    suspend fun addSavingsGoal(savingsGoal: SavingsGoal) {
        savingsGoalDao.insertSavingsGoal(savingsGoal)
    }

    suspend fun updateSavingsGoal(savingsGoal: SavingsGoal) {
        savingsGoalDao.updateSavingsGoal(savingsGoal)
    }

    suspend fun deleteSavingsGoal(savingsGoal: SavingsGoal) {
        savingsGoalDao.deleteSavingsGoal(savingsGoal)
    }
}
