package com.example.debit.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: String, // "TOTAL" represents overall monthly budget, or specific category name like "餐飲"
    val amountLimit: Double,
    val yearMonth: String // e.g., "2026-09"
)
