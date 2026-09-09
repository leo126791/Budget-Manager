package com.example.debit.ui.dialogs

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.debit.data.AppLanguage
import com.example.debit.data.Transaction
import com.example.debit.data.TransactionType
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import com.example.debit.ui.utils.AppStrings
import com.example.debit.ui.utils.GpsLocationUtils
import com.example.debit.ui.utils.getCategoryIcon
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val defaultExpenseCategories = listOf("餐飲", "日常", "娛樂", "購物", "交通", "醫療", "居住", "其他")
val defaultIncomeCategories = listOf("薪水", "兼職", "獎金", "投資", "零用錢", "其他收入")
val defaultAccounts = listOf("現金", "信用卡", "銀行帳戶", "電子支付")

val defaultSubCategoriesMap = mapOf(
    "餐飲" to listOf("早餐", "午餐", "晚餐", "宵夜", "點心飲料"),
    "日常" to listOf("電費", "洗衣", "烘衣", "日用品"),
    "娛樂" to listOf("電影", "遊戲", "KTV", "景點門票"),
    "購物" to listOf("服飾", "鞋包", "3C數碼", "美妝保養"),
    "交通" to listOf("捷運/公車", "加油", "計程車", "停車費"),
    "醫療" to listOf("掛號費", "藥品", "保健品", "健檢"),
    "居住" to listOf("房租", "水費", "瓦斯費", "管理費", "寬頻網路"),
    "其他" to listOf("雜項", "禮物", "捐款")
)

val defaultLocations = listOf("7-11", "全家", "麥當勞", "星巴克", "全聯", "加油站")

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    initialTransaction: Transaction? = null,
    language: AppLanguage = AppLanguage.ZH,
    isLivingExpensePoolEnabled: Boolean = false,
    livingExpensePoolAmount: Double = 0.0,
    isIncomeTrackingEnabled: Boolean = true,
    isMultiAccountEnabled: Boolean = true,
    locationPredictionEnabled: Boolean = false,
    recentLocations: List<String> = emptyList(),
    onGetCustomSubCategories: (String) -> List<String> = { emptyList() },
    onAddCustomSubCategory: (String, String) -> Unit = { _, _ -> },
    onGetLocationEstimate: (String) -> Pair<Double, Int> = { Pair(0.0, 0) },
    onDismissRequest: () -> Unit,
    onConfirm: (amount: Double, category: String, note: String, date: Long, locationName: String, deductFromPool: Boolean, accountName: String, type: TransactionType) -> Unit
) {
    val context = LocalContext.current
    val isZh = language == AppLanguage.ZH

    var transactionType by remember {
        mutableStateOf(initialTransaction?.type ?: TransactionType.EXPENSE)
    }
    var accountName by remember {
        mutableStateOf(initialTransaction?.accountName ?: "現金")
    }
    var deductFromPool by remember { mutableStateOf(initialTransaction?.deductFromPool ?: false) }

    var amountText by remember {
        mutableStateOf(
            initialTransaction?.amount?.let {
                if (it % 1.0 == 0.0) it.toInt().toString() else it.toString()
            } ?: ""
        )
    }
    var category by remember {
        mutableStateOf(initialTransaction?.category ?: defaultExpenseCategories.first())
    }
    var selectedSubCategory by remember {
        mutableStateOf<String?>(
            initialTransaction?.note?.split(" • ")?.firstOrNull()
        )
    }
    var showAddSubDialog by remember { mutableStateOf(false) }
    var customSubInput by remember { mutableStateOf("") }
    var customSubVersion by remember { mutableIntStateOf(0) }

    val defaultSubs = defaultSubCategoriesMap[category] ?: emptyList()
    val currentSubs = remember(category, customSubVersion) {
        val custom = onGetCustomSubCategories(category)
        (defaultSubs + custom).distinct()
    }
    var locationName by remember {
        mutableStateOf(initialTransaction?.locationName ?: "")
    }
    var note by remember { mutableStateOf(initialTransaction?.note ?: "") }
    var selectedDateMillis by remember {
        mutableLongStateOf(initialTransaction?.date ?: System.currentTimeMillis())
    }
    var amountError by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val formattedDate = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date(selectedDateMillis))

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.any { it }) {
            GpsLocationUtils.getCurrentGpsLocationName(context) { gpsName ->
                if (gpsName.isNotBlank()) {
                    locationName = gpsName
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        if (locationPredictionEnabled && locationName.isBlank()) {
            if (GpsLocationUtils.hasLocationPermission(context)) {
                GpsLocationUtils.getCurrentGpsLocationName(context) { gpsName ->
                    if (gpsName.isNotBlank()) {
                        locationName = gpsName
                    }
                }
            }
        }
    }

    val estimate = remember(locationName, note) {
        val query = locationName.ifBlank { note }
        onGetLocationEstimate(query)
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = if (initialTransaction == null) AppStrings.get("add_expense", language) else AppStrings.get("edit_record", language),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Location Input & GPS Auto-Fetch
                if (locationPredictionEnabled) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = locationName,
                            onValueChange = { locationName = it },
                            label = { Text(if (isZh) "GPS 消費地點 / 店名" else "GPS Location / Merchant") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Place,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        if (GpsLocationUtils.hasLocationPermission(context)) {
                                            GpsLocationUtils.getCurrentGpsLocationName(context) { gpsName ->
                                                if (gpsName.isNotBlank()) locationName = gpsName
                                            }
                                        } else {
                                            permissionLauncher.launch(
                                                arrayOf(
                                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                                )
                                            )
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MyLocation,
                                        contentDescription = "GPS 定位",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Location Quick Chips
                        val displayLocs = (recentLocations + defaultLocations).distinct().take(5)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            displayLocs.forEach { loc ->
                                FilterChip(
                                    selected = locationName == loc,
                                    onClick = {
                                        locationName = if (locationName == loc) "" else loc
                                    },
                                    label = { Text(loc, fontSize = 11.sp) }
                                )
                            }
                        }

                        // Prediction Badge Chip based on GPS Location
                        if (estimate.second > 0) {
                            val avgFormatted = String.format(Locale.getDefault(), "%,.0f", estimate.first)
                            val estText = if (isZh)
                                "🔮 預估消費: $$avgFormatted (點擊自動填入，共 ${estimate.second} 次歷史紀錄)"
                            else
                                "🔮 Est. Spend: $$avgFormatted (Tap to fill, ${estimate.second} visits)"

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        amountText = if (estimate.first % 1.0 == 0.0) estimate.first.toInt().toString() else estimate.first.toString()
                                        amountError = false
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = estText,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Amount Text Field
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        amountError = false
                    },
                    label = { Text(AppStrings.get("expense_amount", language)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = amountError,
                    supportingText = if (amountError) {
                        { Text(if (language == AppLanguage.ZH) "請輸入有效的金額" else "Please enter a valid amount", color = MaterialTheme.colorScheme.error) }
                    } else null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Date Selection Card
                Text(AppStrings.get("select_date", language), style = MaterialTheme.typography.labelMedium)
                OutlinedCard(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = AppStrings.get("select_date", language),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = formattedDate, style = MaterialTheme.typography.bodyLarge)
                        }
                        Text(
                            text = AppStrings.get("change_date", language),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Transaction Type Segmented Button
                if (isIncomeTrackingEnabled) {
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        SegmentedButton(
                            selected = transactionType == TransactionType.EXPENSE,
                            onClick = {
                                transactionType = TransactionType.EXPENSE
                                category = defaultExpenseCategories.first()
                            },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                        ) {
                            Text(if (isZh) "💸 支出" else "Expense")
                        }
                        SegmentedButton(
                            selected = transactionType == TransactionType.INCOME,
                            onClick = {
                                transactionType = TransactionType.INCOME
                                category = defaultIncomeCategories.first()
                            },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                        ) {
                            Text(if (isZh) "💵 收入" else "Income")
                        }
                    }
                }

                // Multi Account Selection
                if (isMultiAccountEnabled) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(if (isZh) "支付 / 存入帳戶" else "Account", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            defaultAccounts.forEach { acc ->
                                val iconStr = when (acc) {
                                    "現金" -> "💵 "
                                    "信用卡" -> "💳 "
                                    "銀行帳戶" -> "🏦 "
                                    else -> "📱 "
                                }
                                FilterChip(
                                    selected = accountName == acc,
                                    onClick = { accountName = acc },
                                    label = { Text(iconStr + acc) }
                                )
                            }
                        }
                    }
                }

                // Category Selection Chips
                val currentCategories = if (transactionType == TransactionType.INCOME) defaultIncomeCategories else defaultExpenseCategories
                Text(AppStrings.get("select_category", language), style = MaterialTheme.typography.labelMedium)

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    currentCategories.forEach { cat ->
                        val catLabel = AppStrings.getCategoryName(cat, language)
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(catLabel) },
                            leadingIcon = {
                                Icon(
                                    imageVector = getCategoryIcon(cat),
                                    contentDescription = catLabel,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }
                }

                // Sub-category Selection for ALL Categories
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = if (isZh) "選擇「$category」細項" else "Subcategory for $category",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        currentSubs.forEach { sub ->
                            val chipLabel = when (sub) {
                                "洗衣" -> AppStrings.get("laundry_chip", language)
                                "烘衣" -> AppStrings.get("dryer_chip", language)
                                else -> AppStrings.getSubCategoryName(sub, language)
                            }
                            FilterChip(
                                selected = selectedSubCategory == sub,
                                onClick = {
                                    if (selectedSubCategory == sub) {
                                        selectedSubCategory = null
                                    } else {
                                        selectedSubCategory = sub
                                        if (sub == "洗衣" || sub == "烘衣") {
                                            amountText = "20"
                                            amountError = false
                                        }
                                    }
                                },
                                label = { Text(chipLabel) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            )
                        }

                        // ➕ Add Custom Subcategory Chip
                        FilterChip(
                            selected = false,
                            onClick = { showAddSubDialog = true },
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "新增細項",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(if (isZh) "新增" else "Add")
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }
                }

                // Budget Source Selection for Living Expense Pool
                if (isLivingExpensePoolEnabled) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = if (isZh) "扣款預算來源" else "Deduct From Budget",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val poolFormatted = if (livingExpensePoolAmount < 0)
                                "-$${String.format(Locale.getDefault(), "%,.0f", -livingExpensePoolAmount)}"
                            else
                                "$${String.format(Locale.getDefault(), "%,.0f", livingExpensePoolAmount)}"
                            FilterChip(
                                selected = !deductFromPool,
                                onClick = { deductFromPool = false },
                                label = {
                                    Text(if (isZh) "🏢 本月預算" else "Monthly Budget", fontSize = 12.sp)
                                },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = deductFromPool,
                                onClick = { deductFromPool = true },
                                label = {
                                    Text(if (isZh) "💰 生活費池 ($poolFormatted)" else "Pool ($poolFormatted)", fontSize = 12.sp)
                                },
                                modifier = Modifier.weight(1.1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = if (livingExpensePoolAmount < 0) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = if (livingExpensePoolAmount < 0) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }
                }

                // Note Text Field
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(AppStrings.get("note_optional", language)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    if (amount == null || amount <= 0) {
                        amountError = true
                    } else {
                        var finalNote = note.trim()
                        if (selectedSubCategory != null) {
                            if (!finalNote.contains(selectedSubCategory!!)) {
                                finalNote = if (finalNote.isBlank()) selectedSubCategory!! else "${selectedSubCategory!!} • $finalNote"
                            }
                        }
                        onConfirm(amount, category, finalNote, selectedDateMillis, locationName.trim(), deductFromPool, accountName, transactionType)
                        onDismissRequest()
                    }
                }
            ) {
                Text(if (initialTransaction == null) AppStrings.get("save", language) else AppStrings.get("update_record", language))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(AppStrings.get("cancel", language))
            }
        }
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { selectedDateMillis = it }
                        showDatePicker = false
                    }
                ) {
                    Text(AppStrings.get("confirm", language))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(AppStrings.get("cancel", language))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showAddSubDialog) {
        AlertDialog(
            onDismissRequest = { showAddSubDialog = false },
            title = {
                Text(if (isZh) "新增「$category」次細項" else "Add $category Subcategory")
            },
            text = {
                OutlinedTextField(
                    value = customSubInput,
                    onValueChange = { customSubInput = it },
                    label = { Text(if (isZh) "細項名稱 (如: 外送, 健身房...)" else "Subcategory Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val trimmed = customSubInput.trim()
                        if (trimmed.isNotBlank()) {
                            onAddCustomSubCategory(category, trimmed)
                            selectedSubCategory = trimmed
                            customSubVersion++
                            customSubInput = ""
                            showAddSubDialog = false
                        }
                    }
                ) {
                    Text(AppStrings.get("confirm", language))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddSubDialog = false }) {
                    Text(AppStrings.get("cancel", language))
                }
            }
        )
    }
}
