package com.example.debit.ui.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocalLaundryService
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.vector.ImageVector

fun getCategoryIcon(category: String): ImageVector {
    return when {
        category.contains("洗衣") || category.contains("烘衣") -> Icons.Default.LocalLaundryService
        category == "餐飲" -> Icons.Default.Restaurant
        category == "交通" -> Icons.Default.DirectionsBus
        category == "娛樂" -> Icons.Default.SportsEsports
        category == "購物" -> Icons.Default.ShoppingBag
        category == "日常" || category == "居住" -> Icons.Default.Home
        category == "醫療" -> Icons.Default.LocalHospital
        category == "薪水" || category == "兼職" -> Icons.Default.Work
        category == "獎金" -> Icons.Default.Payments
        category == "投資" -> Icons.Default.Savings
        else -> Icons.Default.Category
    }
}
