package com.example.debit.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE date >= :startTime AND date <= :endTime ORDER BY date DESC")
    fun getTransactionsByDateRange(startTime: Long, endTime: Long): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE date >= :startTime AND date <= :endTime")
    suspend fun getRecentTransactions(startTime: Long, endTime: Long): List<Transaction>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction)

    @Update
    suspend fun updateTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)
}
