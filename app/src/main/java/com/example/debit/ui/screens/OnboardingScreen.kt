package com.example.debit.ui.screens

import android.Manifest
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.debit.data.AppLanguage
import com.example.debit.data.AppThemeColor
import com.example.debit.data.SettingsPreferences
import com.example.debit.receiver.DailyReminderReceiver
import com.example.debit.service.PaymentNotificationListenerService
import com.example.debit.ui.DebitViewModel
import com.example.debit.ui.theme.DebitTheme
import com.example.debit.ui.utils.bouncyClickable
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun OnboardingScreen(
    viewModel: DebitViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()

    var selectedLanguage by remember { mutableStateOf(uiState.appLanguage) }
    var isDarkMode by remember { mutableStateOf(uiState.isDarkMode) }
    val selectedTheme = AppThemeColor.EMERALD
    var budgetInput by remember { mutableStateOf("15000") }

    var dailyReminderState by remember { mutableStateOf(uiState.isDailyReminderEnabled) }
    var reminderHourState by remember { mutableIntStateOf(21) }
    var reminderMinuteState by remember { mutableIntStateOf(0) }

    var googlePayListenerState by remember { mutableStateOf(uiState.isGooglePayListenerEnabled) }
    var confirmDeleteState by remember { mutableStateOf(uiState.isConfirmDeleteEnabled) }

    val isZh = selectedLanguage == AppLanguage.ZH
    val pagerState = rememberPagerState(pageCount = { 4 })

    val postNotificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            DailyReminderReceiver.showReminderNotification(
                context,
                SettingsPreferences(context)
            )
            Toast.makeText(
                context,
                if (isZh) "已發送測試提醒通知！" else "Test reminder notification sent!",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(
                context,
                if (isZh) "請開啟通知權限以接收記帳提醒！" else "Notification permission required!",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    DebitTheme(darkTheme = isDarkMode, themeColor = selectedTheme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                // Top Navigation Bar (Back, Dots Indicator, Skip)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (pagerState.currentPage > 0) {
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Previous Page",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.size(48.dp))
                    }

                    // Modern Pill Indicator
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(4) { index ->
                            val isSelected = pagerState.currentPage == index
                            val width by animateDpAsState(
                                targetValue = if (isSelected) 24.dp else 8.dp,
                                animationSpec = tween(300),
                                label = "dotWidth"
                            )
                            val color by animateColorAsState(
                                targetValue = if (isSelected) selectedTheme.primaryColor
                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                                animationSpec = tween(300),
                                label = "dotColor"
                            )
                            Box(
                                modifier = Modifier
                                    .height(7.dp)
                                    .width(width)
                                    .clip(CircleShape)
                                    .background(color)
                                    .clickable {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(index)
                                        }
                                    }
                            )
                        }
                    }

                    if (pagerState.currentPage < 3) {
                        TextButton(
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(3)
                                }
                            }
                        ) {
                            Text(
                                text = if (isZh) "跳過" else "Skip",
                                color = selectedTheme.primaryColor,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.size(48.dp))
                    }
                }

                // Horizontal Pager for the 4 Onboarding Steps
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) { page ->
                    when (page) {
                        0 -> OnboardingWelcomePage(
                            selectedLanguage = selectedLanguage,
                            onLanguageSelected = { selectedLanguage = it },
                            isDarkMode = isDarkMode,
                            onDarkModeSelected = { isDarkMode = it },
                            selectedTheme = selectedTheme
                        )
                        1 -> OnboardingAutoTrackingPage(
                            selectedLanguage = selectedLanguage,
                            selectedTheme = selectedTheme,
                            enabled = googlePayListenerState,
                            onToggle = { enabled ->
                                googlePayListenerState = enabled
                                if (enabled && !PaymentNotificationListenerService.isPermissionGranted(context)) {
                                    val promptMsg = if (isZh)
                                        "請在系統設定中開啟「通知存取」權限，以自動捕捉交易！"
                                    else
                                        "Please enable Notification Access permission!"
                                    Toast.makeText(context, promptMsg, Toast.LENGTH_LONG).show()
                                    context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                                }
                            }
                        )
                        2 -> OnboardingBudgetAndReminderPage(
                            selectedLanguage = selectedLanguage,
                            selectedTheme = selectedTheme,
                            budget = budgetInput,
                            onBudgetChange = { budgetInput = it },
                            reminderEnabled = dailyReminderState,
                            onReminderToggle = { enabled ->
                                dailyReminderState = enabled
                                if (enabled && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) &&
                                    (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)) {
                                    postNotificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            },
                            reminderHour = reminderHourState,
                            reminderMinute = reminderMinuteState,
                            onTimeClick = {
                                TimePickerDialog(
                                    context,
                                    { _, hourOfDay, minute ->
                                        reminderHourState = hourOfDay
                                        reminderMinuteState = minute
                                    },
                                    reminderHourState,
                                    reminderMinuteState,
                                    true
                                ).show()
                            }
                        )
                        3 -> OnboardingSummaryPage(
                            selectedLanguage = selectedLanguage,
                            selectedTheme = selectedTheme,
                            isDarkMode = isDarkMode,
                            budget = budgetInput,
                            autoTrackingEnabled = googlePayListenerState,
                            dailyReminderEnabled = dailyReminderState,
                            reminderHour = reminderHourState,
                            reminderMinute = reminderMinuteState,
                            confirmDeleteEnabled = confirmDeleteState,
                            onConfirmDeleteToggle = { confirmDeleteState = it }
                        )
                    }
                }

                // Bottom Action Button Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    val parsedBudget = budgetInput.toDoubleOrNull() ?: 0.0

                    if (pagerState.currentPage < 3) {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .shadow(8.dp, shape = RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = selectedTheme.primaryColor
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = if (isZh) "下一步" else "Next Step",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    ),
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    } else {
                        Button(
                            onClick = {
                                viewModel.completeOnboarding(
                                    budgetLimit = parsedBudget,
                                    color = selectedTheme,
                                    language = selectedLanguage,
                                    googlePayListenerEnabled = googlePayListenerState,
                                    dailyReminderEnabled = dailyReminderState,
                                    reminderHour = reminderHourState,
                                    reminderMinute = reminderMinuteState,
                                    confirmDeleteEnabled = confirmDeleteState,
                                    darkMode = isDarkMode
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .shadow(12.dp, shape = RoundedCornerShape(18.dp)),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = selectedTheme.primaryColor
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RocketLaunch,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = if (isZh) "開始使用 Budget Manager" else "Get Started with Budget Manager",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp
                                    ),
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Page 0: Welcome & Personalization (Language & Dark / Light Appearance Selection)
 */
@Composable
private fun OnboardingWelcomePage(
    selectedLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
    isDarkMode: Boolean,
    onDarkModeSelected: (Boolean) -> Unit,
    selectedTheme: AppThemeColor
) {
    val isZh = selectedLanguage == AppLanguage.ZH

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Hero Visual
        Box(
            modifier = Modifier
                .size(92.dp)
                .shadow(12.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(
                            selectedTheme.primaryColor,
                            selectedTheme.primaryColor.copy(alpha = 0.75f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AccountBalanceWallet,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = if (isZh) "歡迎來到 Budget Manager" else "Welcome to Budget Manager",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            ),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = if (isZh) "極致簡約 · 3D 質感 · 智慧省心理財" else "Minimal, 3D Organic Design & Smart Finance",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(26.dp))

        // Step 1: Language Selection Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Translate,
                        contentDescription = null,
                        tint = selectedTheme.primaryColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isZh) "選擇語言 (Language)" else "Select Language",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AppLanguage.entries.forEach { lang ->
                        val isSelected = selectedLanguage == lang
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .bouncyClickable { onLanguageSelected(lang) }
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) selectedTheme.primaryColor
                                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(14.dp)
                                ),
                            color = if (isSelected) selectedTheme.containerColor
                            else MaterialTheme.colorScheme.surface
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp, horizontal = 12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = lang.displayName,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) selectedTheme.primaryColor
                                    else MaterialTheme.colorScheme.onSurface
                                )
                                if (isSelected) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = selectedTheme.primaryColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Step 2: Appearance Mode (Light / Dark) Selection Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                        contentDescription = null,
                        tint = selectedTheme.primaryColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isZh) "色彩外觀 (Appearance)" else "Appearance",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Light Mode Button
                    val isLightSelected = !isDarkMode
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .bouncyClickable { onDarkModeSelected(false) }
                            .border(
                                width = if (isLightSelected) 2.dp else 1.dp,
                                color = if (isLightSelected) selectedTheme.primaryColor
                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(14.dp)
                            ),
                        color = if (isLightSelected) selectedTheme.containerColor
                        else MaterialTheme.colorScheme.surface
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 14.dp, horizontal = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LightMode,
                                contentDescription = null,
                                tint = if (isLightSelected) selectedTheme.primaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (isZh) "亮色外觀" else "Light Mode",
                                fontWeight = if (isLightSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 14.sp,
                                color = if (isLightSelected) selectedTheme.primaryColor else MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isZh) "清新明亮" else "Clean & Bright",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Dark Mode Button
                    val isDarkSelected = isDarkMode
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .bouncyClickable { onDarkModeSelected(true) }
                            .border(
                                width = if (isDarkSelected) 2.dp else 1.dp,
                                color = if (isDarkSelected) selectedTheme.primaryColor
                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(14.dp)
                            ),
                        color = if (isDarkSelected) selectedTheme.containerColor
                        else MaterialTheme.colorScheme.surface
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 14.dp, horizontal = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DarkMode,
                                contentDescription = null,
                                tint = if (isDarkSelected) selectedTheme.primaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (isZh) "深色外觀" else "Dark Mode",
                                fontWeight = if (isDarkSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 14.sp,
                                color = if (isDarkSelected) selectedTheme.primaryColor else MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isZh) "深邃護眼" else "Easy on Eyes",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * Page 1: Smart Payment Auto-Tracking (Google Pay & LINE Pay)
 */
@Composable
private fun OnboardingAutoTrackingPage(
    selectedLanguage: AppLanguage,
    selectedTheme: AppThemeColor,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val isZh = selectedLanguage == AppLanguage.ZH
    val isPermissionGranted = PaymentNotificationListenerService.isPermissionGranted(context)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Hero Visual
        Box(
            modifier = Modifier
                .size(92.dp)
                .shadow(12.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF059669),
                            Color(0xFF10B981)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CreditCard,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = if (isZh) "Google Pay & LINE Pay 自動記帳" else "Smart Payment Auto-Tracking",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            ),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = if (isZh)
                "感應或條碼付款後，自動讀取通知並瞬間記錄開銷"
            else
                "Automatically capture expenses from Google Pay & LINE Pay notifications in real time.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Feature Highlights Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                FeatureRow(
                    icon = Icons.Default.CheckCircle,
                    tint = Color(0xFF3B82F6),
                    title = if (isZh) "Google Pay / Google 錢包" else "Google Pay & Wallet",
                    description = if (isZh) "實體刷卡感應與線上付款通知即時精準捕捉" else "Real-time capture for tap-to-pay and online orders."
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                FeatureRow(
                    icon = Icons.Default.CheckCircle,
                    tint = Color(0xFF10B981),
                    title = if (isZh) "LINE Pay 條碼與線上支付" else "LINE Pay Notifications",
                    description = if (isZh) "自動解析 LINE Pay 扣款資訊，無縫記錄" else "Effortless auto-logging for LINE Pay purchases."
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                FeatureRow(
                    icon = Icons.Default.Security,
                    tint = selectedTheme.primaryColor,
                    title = if (isZh) "純本機安全分析" else "100% On-Device Privacy",
                    description = if (isZh) "所有通知僅於本機解析，嚴格過濾行銷推播與郵件" else "Strictly processed locally, safely ignoring promotions and emails."
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Switch Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (enabled) selectedTheme.containerColor.copy(alpha = 0.35f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            ),
            border = BorderStroke(
                width = if (enabled) 1.5.dp else 1.dp,
                color = if (enabled) selectedTheme.primaryColor.copy(alpha = 0.5f)
                else MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
            )
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isZh) "啟用自動捕捉記帳" else "Enable Payment Auto-Tracking",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isZh) "日後可隨時在設定頁面開啟或關閉" else "You can toggle this anytime in Settings.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = enabled,
                        onCheckedChange = onToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = selectedTheme.primaryColor
                        )
                    )
                }

                if (enabled) {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isPermissionGranted)
                            Color(0xFF10B981).copy(alpha = 0.12f)
                        else
                            Color(0xFFF59E0B).copy(alpha = 0.15f),
                        border = BorderStroke(
                            1.dp,
                            if (isPermissionGranted) Color(0xFF10B981).copy(alpha = 0.3f)
                            else Color(0xFFF59E0B).copy(alpha = 0.4f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = if (isPermissionGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (isPermissionGranted) Color(0xFF10B981) else Color(0xFFD97706),
                                modifier = Modifier.size(20.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isPermissionGranted) {
                                        if (isZh) "通知存取權限：已啟用 (運作中)" else "Notification Access: Active"
                                    } else {
                                        if (isZh) "尚未開啟通知存取權限 (點此開啟)" else "Notification Access Required (Tap to open)"
                                    },
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isPermissionGranted) Color(0xFF047857) else Color(0xFFB45309)
                                )
                                if (!isPermissionGranted) {
                                    Text(
                                        text = if (isZh) "請在系統列表中允許 Budget Manager 存取通知" else "Please allow notification permission in system settings",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 11.sp,
                                        color = Color(0xFFB45309).copy(alpha = 0.85f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * Page 2: Budget Setup & Daily Reminders
 */
@Composable
private fun OnboardingBudgetAndReminderPage(
    selectedLanguage: AppLanguage,
    selectedTheme: AppThemeColor,
    budget: String,
    onBudgetChange: (String) -> Unit,
    reminderEnabled: Boolean,
    onReminderToggle: (Boolean) -> Unit,
    reminderHour: Int,
    reminderMinute: Int,
    onTimeClick: () -> Unit
) {
    val isZh = selectedLanguage == AppLanguage.ZH

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Hero Visual
        Box(
            modifier = Modifier
                .size(92.dp)
                .shadow(12.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF2563EB),
                            Color(0xFF3B82F6)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Savings,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = if (isZh) "預算規劃與貼心提醒" else "Budget & Daily Reminders",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            ),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = if (isZh)
                "設定每月開銷上限，定時回顧每日收支，培養自律理財習慣"
            else
                "Set monthly budget targets and stay on track with regular daily reminders.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Step 1: Monthly Budget Input Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Savings,
                        contentDescription = null,
                        tint = selectedTheme.primaryColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isZh) "每月支出目標金庫 ($)" else "Monthly Budget Limit ($)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = budget,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() }) {
                            onBudgetChange(input)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    prefix = { Text("$ ", fontWeight = FontWeight.Bold) },
                    suffix = { Text("TWD", style = MaterialTheme.typography.labelMedium) },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = selectedTheme.primaryColor,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Quick budget presets
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf("10000", "15000", "20000", "30000").forEach { amount ->
                        val isSelected = budget == amount
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .bouncyClickable { onBudgetChange(amount) },
                            color = if (isSelected) selectedTheme.containerColor
                            else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) selectedTheme.primaryColor
                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            )
                        ) {
                            Text(
                                text = "$$amount",
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = TextAlign.Center,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) selectedTheme.primaryColor
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Step 2: Daily Reminder Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = selectedTheme.primaryColor,
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Text(
                                text = if (isZh) "每日記帳提醒" else "Daily Expense Reminder",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = if (isZh) "固定時間提醒您記錄今天的小開銷" else "Get a friendly daily reminder to log expenses",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = reminderEnabled,
                        onCheckedChange = onReminderToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = selectedTheme.primaryColor
                        )
                    )
                }

                if (reminderEnabled) {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isZh)
                                "提醒時間：%02d:%02d".format(reminderHour, reminderMinute)
                            else
                                "Time: %02d:%02d".format(reminderHour, reminderMinute),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )

                        OutlinedButton(
                            onClick = onTimeClick,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, selectedTheme.primaryColor)
                        ) {
                            Text(
                                text = if (isZh) "變更時間" else "Change Time",
                                color = selectedTheme.primaryColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * Page 3: Summary & Ready to Go
 */
@Composable
private fun OnboardingSummaryPage(
    selectedLanguage: AppLanguage,
    selectedTheme: AppThemeColor,
    isDarkMode: Boolean,
    budget: String,
    autoTrackingEnabled: Boolean,
    dailyReminderEnabled: Boolean,
    reminderHour: Int,
    reminderMinute: Int,
    confirmDeleteEnabled: Boolean,
    onConfirmDeleteToggle: (Boolean) -> Unit
) {
    val isZh = selectedLanguage == AppLanguage.ZH
    val budgetValue = budget.toDoubleOrNull() ?: 0.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Hero Visual
        Box(
            modifier = Modifier
                .size(92.dp)
                .shadow(12.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(
                            selectedTheme.primaryColor,
                            selectedTheme.primaryColor.copy(alpha = 0.75f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.RocketLaunch,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = if (isZh) "一切準備就緒！" else "All Set & Ready!",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            ),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = if (isZh)
                "已為您量身打造專屬理財儀表板，確認後立即展開體驗"
            else
                "Your personalized financial dashboard is configured. Tap below to begin!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Configuration Overview Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (isZh) "初始設定總覽" else "Configuration Overview",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = selectedTheme.primaryColor
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                SummaryRow(
                    label = if (isZh) "顯示語言" else "Language",
                    value = selectedLanguage.displayName
                )

                SummaryRow(
                    label = if (isZh) "色彩外觀" else "Appearance",
                    value = if (isDarkMode) (if (isZh) "深色外觀" else "Dark Mode") else (if (isZh) "亮色外觀" else "Light Mode")
                )

                SummaryRow(
                    label = if (isZh) "月預算上限" else "Monthly Budget",
                    value = "$${String.format(Locale.getDefault(), "%,.0f", budgetValue)} TWD"
                )

                SummaryRow(
                    label = if (isZh) "自動記帳 (GPay / LinePay)" else "Auto-Tracking",
                    value = if (autoTrackingEnabled) {
                        if (isZh) "已啟用 (Active)" else "Enabled"
                    } else {
                        if (isZh) "未開啟" else "Disabled"
                    }
                )

                SummaryRow(
                    label = if (isZh) "每日記帳提醒" else "Daily Reminder",
                    value = if (dailyReminderEnabled) {
                        "%02d:%02d".format(reminderHour, reminderMinute)
                    } else {
                        if (isZh) "未開啟" else "Disabled"
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Confirm Delete Safety Switch Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isZh) "刪除前確認 (防誤觸)" else "Confirm Before Delete",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = if (isZh) "刪除記錄或訂閱時彈出確認對話框" else "Show confirmation dialog when deleting items",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = confirmDeleteEnabled,
                        onCheckedChange = onConfirmDeleteToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = selectedTheme.primaryColor
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun FeatureRow(
    icon: ImageVector,
    tint: Color,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(tint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    leadingDotColor: Color? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (leadingDotColor != null) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(leadingDotColor)
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
