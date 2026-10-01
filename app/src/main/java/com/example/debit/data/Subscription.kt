package com.example.debit.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subscriptions")
data class Subscription(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val amount: Double,
    val billingDay: Int = 1, // Day of month (1-31)
    @ColumnInfo(defaultValue = "1")
    val billingMonth: Int = 1, // Month of year (1-12) for annual
    val category: String = "日常",
    @ColumnInfo(defaultValue = "0")
    val isAnnual: Boolean = false,
    @ColumnInfo(defaultValue = "EXPENSE")
    val type: TransactionType = TransactionType.EXPENSE,
    @ColumnInfo(defaultValue = "0")
    val isCustomInterval: Boolean = false,
    @ColumnInfo(defaultValue = "30")
    val intervalDays: Int = 30,
    @ColumnInfo(defaultValue = "9")
    val billingHour: Int = 9,
    @ColumnInfo(defaultValue = "0")
    val billingMinute: Int = 0,
    @ColumnInfo(defaultValue = "0")
    val lastProcessedDate: Long = 0L
)
