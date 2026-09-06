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
import com.example.debit.ui.utils.AppStrings
import com.example.debit.ui.utils.GpsLocationUtils
import com.example.debit.ui.utils.getCategoryIcon
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val defaultExpenseCategories = listOf("餐飲", "日常", "娛樂", "購物", "交通", "醫療", "居住", "其他")
val foodSubCategories = listOf("早餐", "午餐", "晚餐", "宵夜", "點心飲料")
val dailySubCategories = listOf("洗衣", "烘衣", "日用品")
val defaultLocations = listOf("7-11", "全家", "麥當勞", "星巴克", "全聯", "加油站")

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    initialTransaction: Transaction? = null,
    language: AppLanguage = AppLanguage.ZH,
    locationPredictionEnabled: Boolean = true,
    recentLocations: List<String> = emptyList(),
    onGetLocationEstimate: (String) -> Pair<Double, Int> = { Pair(0.0, 0) },
    onDismissRequest: () -> Unit,
    onConfirm: (amount: Double, category: String, note: String, date: Long, locationName: String) -> Unit
) {
    val context = LocalContext.current
    val isZh = language == AppLanguage.ZH

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
    var selectedMealTime by remember {
        mutableStateOf<String?>(
            foodSubCategories.find { initialTransaction?.note?.contains(it) == true }
        )
    }
    var selectedDailySub by remember {
        mutableStateOf<String?>(
            dailySubCategories.find { initialTransaction?.note?.contains(it) == true }
        )
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

                // Category Selection Chips
                Text(AppStrings.get("select_category", language), style = MaterialTheme.typography.labelMedium)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    defaultExpenseCategories.forEach { cat ->
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

                // Sub-category Selection for Dining ("餐飲")
                if (category == "餐飲") {
                    Text(AppStrings.get("meal_time", language), style = MaterialTheme.typography.labelMedium)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        foodSubCategories.forEach { meal ->
                            val mealLabel = AppStrings.getSubCategoryName(meal, language)
                            FilterChip(
                                selected = selectedMealTime == meal,
                                onClick = {
                                    selectedMealTime = if (selectedMealTime == meal) null else meal
                                },
                                label = { Text(mealLabel) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            )
                        }
                    }
                }

                // Sub-category Selection for Daily ("日常")
                if (category == "日常") {
                    Text(AppStrings.get("daily_sub", language), style = MaterialTheme.typography.labelMedium)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        dailySubCategories.forEach { sub ->
                            val chipLabel = when (sub) {
                                "洗衣" -> AppStrings.get("laundry_chip", language)
                                "烘衣" -> AppStrings.get("dryer_chip", language)
                                else -> AppStrings.getSubCategoryName(sub, language)
                            }
                            FilterChip(
                                selected = selectedDailySub == sub,
                                onClick = {
                                    if (selectedDailySub == sub) {
                                        selectedDailySub = null
                                    } else {
                                        selectedDailySub = sub
                                        if (sub == "洗衣" || sub == "烘衣") {
                                            amountText = "20"
                                            amountError = false
                                        }
                                    }
                                },
                                label = { Text(chipLabel) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onTertiaryContainer
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
                        if (category == "餐飲" && selectedMealTime != null) {
                            if (!finalNote.contains(selectedMealTime!!)) {
                                finalNote = if (finalNote.isBlank()) selectedMealTime!! else "${selectedMealTime!!} • $finalNote"
                            }
                        } else if (category == "日常" && selectedDailySub != null) {
                            if (!finalNote.contains(selectedDailySub!!)) {
                                finalNote = if (finalNote.isBlank()) selectedDailySub!! else "${selectedDailySub!!} • $finalNote"
                            }
                        }
                        onConfirm(amount, category, finalNote, selectedDateMillis, locationName.trim())
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
}
