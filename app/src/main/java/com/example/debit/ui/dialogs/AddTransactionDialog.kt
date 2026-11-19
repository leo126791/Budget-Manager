package com.example.debit.ui.dialogs

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.debit.data.AppLanguage
import com.example.debit.data.Transaction
import com.example.debit.data.TransactionType
import com.example.debit.ui.utils.AppStrings
import com.example.debit.ui.utils.getCategoryIcon
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

val defaultExpenseCategories = listOf("餐飲", "交通", "購物", "娛樂", "居家", "醫療", "進修", "更多")
val defaultIncomeCategories = listOf("薪水", "兼職", "獎金", "投資", "零用錢", "其他")
val defaultAccounts = listOf("LINE Pay (中信)", "現金", "信用卡", "銀行帳戶", "電子支付")

val defaultSubCategoriesMap = mapOf(
    "餐飲" to listOf("商業午餐", "外帶", "聚餐", "早餐", "晚餐", "飲料"),
    "交通" to listOf("捷運/公車", "加油", "計程車", "停車費"),
    "購物" to listOf("日常用品", "服飾", "3C家電", "網購"),
    "娛樂" to listOf("電影", "遊戲", "聚會", "活動門票"),
    "居家" to listOf("水電費", "洗衣", "房租", "日常雜貨"),
    "醫療" to listOf("掛號費", "藥品", "健檢", "保健品"),
    "進修" to listOf("書籍", "線上課程", "講座", "文具"),
    "更多" to listOf("雜項", "禮物", "公益捐款")
)

private fun evaluateExpression(expr: String): Double? {
    try {
        val clean = expr.replace("×", "*").replace("÷", "/")
        val tokens = mutableListOf<String>()
        var currentNumber = StringBuilder()
        for (char in clean) {
            if (char.isDigit() || char == '.') {
                currentNumber.append(char)
            } else if (char in listOf('+', '-', '*', '/')) {
                if (currentNumber.isNotEmpty()) {
                    tokens.add(currentNumber.toString())
                    currentNumber = StringBuilder()
                }
                tokens.add(char.toString())
            }
        }
        if (currentNumber.isNotEmpty()) {
            tokens.add(currentNumber.toString())
        }
        if (tokens.isEmpty()) return null

        val intermediateTokens = mutableListOf<String>()
        var i = 0
        while (i < tokens.size) {
            val token = tokens[i]
            if (token == "*" || token == "/") {
                if (intermediateTokens.isNotEmpty() && i + 1 < tokens.size) {
                    val prev = intermediateTokens.removeAt(intermediateTokens.size - 1).toDoubleOrNull() ?: return null
                    val next = tokens[i + 1].toDoubleOrNull() ?: return null
                    val res = if (token == "*") prev * next else if (next != 0.0) prev / next else 0.0
                    intermediateTokens.add(res.toString())
                    i += 2
                    continue
                }
            }
            intermediateTokens.add(token)
            i++
        }

        var result = intermediateTokens.firstOrNull()?.toDoubleOrNull() ?: return null
        var j = 1
        while (j < intermediateTokens.size) {
            val op = intermediateTokens[j]
            val next = intermediateTokens.getOrNull(j + 1)?.toDoubleOrNull() ?: break
            if (op == "+") result += next
            if (op == "-") result -= next
            j += 2
        }
        return result
    } catch (_: Exception) {
        return null
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    initialTransaction: Transaction? = null,
    language: AppLanguage = AppLanguage.ZH,
    isIncomeTrackingEnabled: Boolean = false,
    isMultiAccountEnabled: Boolean = true,
    locationPredictionEnabled: Boolean = false,
    recentLocations: List<String> = emptyList(),
    onGetCustomSubCategories: (String) -> List<String> = { emptyList() },
    onAddCustomSubCategory: (String, String) -> Unit = { _, _ -> },
    onGetLocationEstimate: (String) -> Pair<Double, Int> = { Pair(0.0, 0) },
    onDismissRequest: () -> Unit,
    onConfirm: (amount: Double, category: String, note: String, date: Long, locationName: String, deductFromPool: Boolean, accountName: String, type: TransactionType) -> Unit
) {
    val isZh = language == AppLanguage.ZH

    // Transaction Type: EXPENSE, INCOME, TRANSFER
    var selectedTypeTab by remember {
        mutableStateOf(
            when (initialTransaction?.type) {
                TransactionType.INCOME -> "INCOME"
                else -> if (initialTransaction?.note?.startsWith("[轉帳]") == true) "TRANSFER" else "EXPENSE"
            }
        )
    }

    var accountName by remember {
        mutableStateOf(initialTransaction?.accountName ?: "LINE Pay (中信)")
    }
    var showAccountMenu by remember { mutableStateOf(false) }

    var category by remember {
        mutableStateOf(
            initialTransaction?.category ?: defaultExpenseCategories.first()
        )
    }

    // Formula & Calculator state
    var expression by remember {
        mutableStateOf(
            initialTransaction?.amount?.let {
                if (it % 1.0 == 0.0) it.toInt().toString() else it.toString()
            } ?: ""
        )
    }

    var note by remember {
        mutableStateOf(
            initialTransaction?.note?.removePrefix("[轉帳] ") ?: ""
        )
    }
    var excludeFromBudget by remember { mutableStateOf(false) }

    var selectedDateMillis by remember {
        mutableLongStateOf(initialTransaction?.date ?: System.currentTimeMillis())
    }
    var showDatePicker by remember { mutableStateOf(false) }
    var showCustomCategoryDialog by remember { mutableStateOf(false) }
    var customCategoryInput by remember { mutableStateOf("") }
    var customSubVersion by remember { mutableIntStateOf(0) }

    // Calculated amount
    val calculatedAmount = remember(expression) {
        if (expression.isBlank()) 0.0
        else evaluateExpression(expression) ?: 0.0
    }

    val displayAmountString = remember(calculatedAmount, expression) {
        if (expression.isBlank()) "0"
        else if (calculatedAmount % 1.0 == 0.0) calculatedAmount.toInt().toString()
        else String.format(Locale.US, "%.2f", calculatedAmount)
    }

    // Date formatting: "今天 MM/dd" or "yyyy/MM/dd"
    val dateDisplay = remember(selectedDateMillis) {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
        val isToday = now.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)
        if (isToday) {
            "今天 " + SimpleDateFormat("MM/dd", Locale.getDefault()).format(Date(selectedDateMillis))
        } else {
            SimpleDateFormat("MM/dd", Locale.getDefault()).format(Date(selectedDateMillis))
        }
    }

    // Cursor animation
    val infiniteTransition = rememberInfiniteTransition(label = "cursor")
    val cursorAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursorAlpha"
    )

    // Save helper
    val doSave: (Boolean) -> Unit = { keepOpen ->
        val finalAmount = if (calculatedAmount > 0) calculatedAmount else (expression.toDoubleOrNull() ?: 0.0)
        if (finalAmount > 0) {
            val transType = when (selectedTypeTab) {
                "INCOME" -> TransactionType.INCOME
                else -> TransactionType.EXPENSE
            }
            val finalNote = if (selectedTypeTab == "TRANSFER") {
                if (note.isNotBlank()) "[轉帳] $note" else "[轉帳]"
            } else note.trim()

            onConfirm(
                finalAmount,
                category,
                finalNote,
                selectedDateMillis,
                "", // location
                excludeFromBudget,
                accountName,
                transType
            )

            if (keepOpen) {
                expression = ""
                note = ""
            } else {
                onDismissRequest()
            }
        }
    }

    // Dialog layout matching user screenshot
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.45f))
                .clickable(onClick = onDismissRequest),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.94f)
                    .clickable(enabled = false) {}, // Prevent dismiss when tapping dialog content
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = Color(0xFFFBFDFB)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // 1. Top Bar: [✕ 記一筆]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onDismissRequest,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "關閉",
                                tint = Color(0xFF1E293B)
                            )
                        }

                        Text(
                            text = if (initialTransaction == null) "記一筆" else "編輯紀錄",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A),
                            modifier = Modifier.padding(start = 6.dp)
                        )
                    }

                    // 2. Type Selector Tabs (支出 / 收入 / 轉帳)
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFF1F5F2),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(3.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            val tabs = listOf(
                                Triple("EXPENSE", "支出", Icons.AutoMirrored.Filled.TrendingDown),
                                Triple("INCOME", "收入", Icons.AutoMirrored.Filled.TrendingUp),
                                Triple("TRANSFER", "轉帳", Icons.Default.SwapHoriz)
                            )
                            tabs.forEach { (typeKey, title, icon) ->
                                val isSelected = selectedTypeTab == typeKey
                                Surface(
                                    shape = CircleShape,
                                    color = if (isSelected) Color(0xFF184E38) else Color.Transparent,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            selectedTypeTab = typeKey
                                            if (typeKey == "INCOME") {
                                                category = defaultIncomeCategories.first()
                                            } else if (typeKey == "EXPENSE") {
                                                category = defaultExpenseCategories.first()
                                            }
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 7.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = title,
                                            tint = if (isSelected) Color.White else Color(0xFF475569),
                                            modifier = Modifier.size(17.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = title,
                                            color = if (isSelected) Color.White else Color(0xFF475569),
                                            fontSize = 14.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 3. Expression Preview & Big Amount Display
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Formula expression preview (e.g. "150 + 30")
                        val hasFormula = expression.contains("+") || expression.contains("-") ||
                                expression.contains("×") || expression.contains("÷")
                        val formattedExpression = remember(expression) {
                            expression
                                .replace("+", " + ")
                                .replace("-", " - ")
                                .replace("×", " × ")
                                .replace("÷", " ÷ ")
                        }
                        Text(
                            text = if (hasFormula) formattedExpression else " ",
                            fontSize = 13.sp,
                            color = Color(0xFF64748B),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium
                        )

                        // Big Amount with Currency and Animated Cursor
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "NT$",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F766E),
                                modifier = Modifier.padding(end = 6.dp)
                            )
                            Text(
                                text = displayAmountString,
                                fontSize = 38.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF0F172A),
                                fontFamily = FontFamily.SansSerif
                            )
                            // Animated vertical cursor bar
                            Box(
                                modifier = Modifier
                                    .padding(start = 4.dp)
                                    .width(2.5.dp)
                                    .height(30.dp)
                                    .alpha(cursorAlpha)
                                    .background(Color(0xFF0F766E))
                            )
                        }
                    }

                    // 4. Quick Info Pills Row: [🍴 類別] [💳 帳戶] [📅 日期]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Category Pill (Mint green background)
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFD1FAE5),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = getCategoryIcon(category),
                                    contentDescription = category,
                                    tint = Color(0xFF065F46),
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = category,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF065F46),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Account Pill (White with subtle border)
                        Box(modifier = Modifier.weight(1.3f)) {
                            Surface(
                                shape = CircleShape,
                                color = Color.White,
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showAccountMenu = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CreditCard,
                                        contentDescription = "帳戶",
                                        tint = Color(0xFF475569),
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = accountName,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF334155),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = showAccountMenu,
                                onDismissRequest = { showAccountMenu = false }
                            ) {
                                defaultAccounts.forEach { acc ->
                                    DropdownMenuItem(
                                        text = { Text(acc, fontSize = 13.sp) },
                                        onClick = {
                                            accountName = acc
                                            showAccountMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        // Date Pill (White with subtle border)
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { showDatePicker = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = "日期",
                                    tint = Color(0xFF475569),
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = dateDisplay,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF334155),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    // 5. Category Selection Grid (消費類別)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (selectedTypeTab == "INCOME") "收入類別" else "消費類別",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF334155)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { showCustomCategoryDialog = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "自訂類別",
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "自訂類別",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    // 4x2 Category Grid
                    val currentCategories = if (selectedTypeTab == "INCOME") defaultIncomeCategories else defaultExpenseCategories
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val row1 = currentCategories.take(4)
                        val row2 = currentCategories.drop(4).take(4)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            row1.forEach { cat ->
                                CategoryGridItem(
                                    name = cat,
                                    isSelected = category == cat,
                                    onClick = {
                                        if (cat == "更多") {
                                            showCustomCategoryDialog = true
                                        } else {
                                            category = cat
                                        }
                                    }
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            row2.forEach { cat ->
                                CategoryGridItem(
                                    name = cat,
                                    isSelected = category == cat,
                                    onClick = {
                                        if (cat == "更多") {
                                            showCustomCategoryDialog = true
                                        } else {
                                            category = cat
                                        }
                                    }
                                )
                            }
                        }
                    }

                    // 6. Note Card (No subcategory recommendations)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF1F5F2),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "備註",
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(modifier = Modifier.weight(1f)) {
                                if (note.isEmpty()) {
                                    Text(
                                        text = "輸入備註...",
                                        fontSize = 13.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                                BasicTextField(
                                    value = note,
                                    onValueChange = { note = it },
                                    textStyle = TextStyle(
                                        fontSize = 13.sp,
                                        color = Color(0xFF0F172A),
                                        fontWeight = FontWeight.Medium
                                    ),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            // 不計入預算 Checkbox
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clickable { excludeFromBudget = !excludeFromBudget }
                                    .padding(start = 8.dp)
                            ) {
                                Text(
                                    text = "不計入預算",
                                    fontSize = 11.sp,
                                    color = Color(0xFF475569)
                                )
                                Checkbox(
                                    checked = excludeFromBudget,
                                    onCheckedChange = { excludeFromBudget = it },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = Color(0xFF184E38)
                                    ),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }

                    // 7. Built-in Keypad (4x4 Calculator Grid)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val keypadRows = listOf(
                            listOf("7", "8", "9", "÷"),
                            listOf("4", "5", "6", "×"),
                            listOf("1", "2", "3", "-"),
                            listOf(".", "0", "⌫", "+")
                        )

                        keypadRows.forEach { rowKeys ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                rowKeys.forEach { key ->
                                    val isOp = key in listOf("÷", "×", "-", "+")
                                    val isBack = key == "⌫"

                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = when {
                                            isOp -> Color(0xFFE2EFE7)
                                            else -> Color(0xFFF1F5F2)
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .clickable {
                                                when (key) {
                                                    "⌫" -> {
                                                        if (expression.isNotEmpty()) {
                                                            expression = expression.dropLast(1)
                                                        }
                                                    }
                                                    in listOf("÷", "×", "-", "+") -> {
                                                        if (expression.isNotEmpty()) {
                                                            val last = expression.last()
                                                            if (last in listOf('+', '-', '×', '÷')) {
                                                                expression = expression.dropLast(1) + key
                                                            } else {
                                                                expression += key
                                                            }
                                                        }
                                                    }
                                                    "." -> {
                                                        val lastToken = expression.split('+', '-', '×', '÷').lastOrNull() ?: ""
                                                        if (!lastToken.contains('.')) {
                                                            expression = if (expression.isEmpty()) "0." else "$expression."
                                                        }
                                                    }
                                                    else -> {
                                                        // Digit
                                                        expression += key
                                                    }
                                                }
                                            }
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isBack) {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                                                    contentDescription = "刪除",
                                                    tint = Color(0xFF1E293B),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            } else {
                                                Text(
                                                    text = key,
                                                    fontSize = if (isOp) 22.sp else 20.sp,
                                                    fontWeight = if (isOp) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isOp) Color(0xFF0F766E) else Color(0xFF0F172A)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 8. Bottom Dual Action Bar: [⊕ 再記一筆] [✓ 完成儲存]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: ⊕ 再記一筆
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFF1F5F2),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { doSave(true) }
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddCircleOutline,
                                    contentDescription = "再記一筆",
                                    tint = Color(0xFF1E293B),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "再記一筆",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
                                )
                            }
                        }

                        // Right: ✓ 完成儲存
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF184E38),
                            modifier = Modifier
                                .weight(1.3f)
                                .clickable { doSave(false) }
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "完成儲存",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "完成儲存",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { newDateUtcMillis ->
                            val oldCal = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
                            val hour = oldCal.get(Calendar.HOUR_OF_DAY)
                            val minute = oldCal.get(Calendar.MINUTE)

                            val newCal = Calendar.getInstance().apply {
                                timeInMillis = newDateUtcMillis
                                set(Calendar.HOUR_OF_DAY, hour)
                                set(Calendar.MINUTE, minute)
                            }
                            selectedDateMillis = newCal.timeInMillis
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("確定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("取消")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Custom Category / Subcategory Dialog
    if (showCustomCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showCustomCategoryDialog = false },
            title = {
                Text("新增「$category」次細項", fontWeight = FontWeight.Bold)
            },
            text = {
                OutlinedTextField(
                    value = customCategoryInput,
                    onValueChange = { customCategoryInput = it },
                    label = { Text("細項名稱 (如: 外送, 咖啡, 甜點...)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val trimmed = customCategoryInput.trim()
                        if (trimmed.isNotBlank()) {
                            onAddCustomSubCategory(category, trimmed)
                            customSubVersion++
                            customCategoryInput = ""
                            showCustomCategoryDialog = false
                        }
                    }
                ) {
                    Text("新增")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomCategoryDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun CategoryGridItem(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(64.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(if (isSelected) Color(0xFF184E38) else Color(0xFFF1F5F2)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = getCategoryIcon(name),
                contentDescription = name,
                tint = if (isSelected) Color.White else Color(0xFF1E293B),
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = name,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color(0xFF184E38) else Color(0xFF475569)
        )
    }
}
