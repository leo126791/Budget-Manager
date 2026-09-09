package com.example.debit.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class TransactionType {
    EXPENSE,
    INCOME
}

@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["date"]),
        Index(value = ["category"])
    ]
)
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val category: String,
    val type: TransactionType = TransactionType.EXPENSE,
    val date: Long = System.currentTimeMillis(),
    val note: String = "",
    @ColumnInfo(defaultValue = "")
    val locationName: String = "",
    @ColumnInfo(defaultValue = "0")
    val deductFromPool: Boolean = false,
    @ColumnInfo(defaultValue = "現金")
    val accountName: String = "現金"
)
