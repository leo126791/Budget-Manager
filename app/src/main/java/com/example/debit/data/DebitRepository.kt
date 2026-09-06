package com.example.debit.data

import kotlinx.coroutines.flow.Flow

class DebitRepository(
    private val transactionDao: TransactionDao,
    private val budgetDao: BudgetDao
) {
    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()
    val globalBudget: Flow<Budget?> = budgetDao.getGlobalTotalBudget()

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
}
