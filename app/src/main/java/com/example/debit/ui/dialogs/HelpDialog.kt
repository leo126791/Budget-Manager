package com.example.debit.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.debit.data.AppLanguage

@Composable
fun HelpDialog(
    language: AppLanguage,
    onDismissRequest: () -> Unit,
    onReplayOnboarding: () -> Unit,
) {
    val isZh = language == AppLanguage.ZH
    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismissRequest,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier
                            .padding(6.dp)
                            .size(24.dp)
                    )
                }
                Text(
                    text = if (isZh) "功能與設定說明" else "Features & Settings Help",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                HelpItem(
                    icon = Icons.Default.AccountBalanceWallet,
                    title = if (isZh) "本月目標小金庫" else "Monthly Budget Pool",
                    description = if (isZh)
                        "設定每月預算上限，首頁即時顯示支出用量與剩餘水位，防止過度消費。"
                    else
                        "Set monthly spending limits with visual progress meters to stay on track."
                )

                HelpItem(
                    icon = Icons.Default.FlashOn,
                    title = if (isZh) "Google Pay / LINE Pay 自動記帳" else "Auto Capture",
                    description = if (isZh)
                        "開啟並授予通知存取權限後，當收到 Google Pay 或 LINE Pay 付款通知時，系統會自動捕捉金額與項目完成記帳。"
                    else
                        "Automatically captures transactions when Google Pay or LINE Pay payment notifications arrive."
                )

                HelpItem(
                    icon = Icons.Default.Alarm,
                    title = if (isZh) "定時記帳提醒" else "Daily Reminders",
                    description = if (isZh)
                        "可自由新增多個每日提醒時段（如晚間 20:00），定時發送通知提醒您記錄當天花費。"
                    else
                        "Configure multiple daily notification times so you never forget to log expenses."
                )

                HelpItem(
                    icon = Icons.Default.DarkMode,
                    title = if (isZh) "色彩外觀與系統語言" else "Theme & Language",
                    description = if (isZh)
                        "支援深色模式（Dark）與亮色模式（Light），並可自由切換繁體中文與英文介面。"
                    else
                        "Supports Dark/Light mode and easy switching between Traditional Chinese and English."
                )

                HelpItem(
                    icon = Icons.Default.CloudUpload,
                    title = if (isZh) "雲端同步與資料備份" else "Backup & Restore",
                    description = if (isZh)
                        "支援匯出 CSV 檔案檢視，以及安全備份資料至 Google 雲端硬碟，更換手機隨時可還原。"
                    else
                        "Export to CSV and securely backup/restore your transactions with Google Drive."
                )

                HelpItem(
                    icon = Icons.Default.Science,
                    title = if (isZh) "BETA 實驗性功能" else "Beta Features",
                    description = if (isZh)
                        "可開啟額外進階工具，包括收入記帳、關鍵字搜尋、週期訂閱管理與多帳戶金流支援。"
                    else
                        "Optional advanced tools including income tracking, search, subscriptions, and multi-account."
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismissRequest,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (isZh) "我知道了" else "Got It")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = {
                    onDismissRequest()
                    onReplayOnboarding()
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (isZh) "查看新手引導" else "Replay Tutorial")
            }
        }
    )
}

@Composable
private fun HelpItem(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier
                    .padding(6.dp)
                    .size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
