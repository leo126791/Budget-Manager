package com.example.debit.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType {
    EXPENSE,
    INCOME
}

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val category: String,
    val type: TransactionType,
    val date: Long = System.currentTimeMillis(),
    val note: String = "",
    @ColumnInfo(defaultValue = "")
    val locationName: String = ""
)
