package com.example.debit.data

import android.content.Context
import androidx.compose.ui.graphics.Color
import java.util.UUID

enum class AppThemeColor(val displayNameZh: String, val displayNameEn: String, val primaryColor: Color, val containerColor: Color) {
    EMERALD("翡翠綠", "Emerald", Color(0xFF1B4A31), Color(0xFFECFDF5)),
    INDIGO("經典靛藍", "Indigo", Color(0xFF4F46E5), Color(0xFFEEF2FF)),
    TEAL("海洋湛藍", "Teal", Color(0xFF0284C7), Color(0xFFE0F2FE)),
    ROSE("浪漫玫瑰", "Rose", Color(0xFFE11D48), Color(0xFFFFE4E6)),
    AMBER("活力琥珀", "Amber", Color(0xFFD97706), Color(0xFFFEF3C7)),
    PURPLE("高雅薰衣", "Purple", Color(0xFF7C3AED), Color(0xFFF3E8FF)),
    CYAN("清新薄荷", "Mint", Color(0xFF0D9488), Color(0xFFCCFBF1)),
    CORAL("珊瑚暖橘", "Coral", Color(0xFFEA580C), Color(0xFFFFEDD5)),
    SLATE("沈穩墨灰", "Slate", Color(0xFF475569), Color(0xFFF1F5F9)),
    CUSTOM("自訂色彩", "Custom", Color(0xFF6366F1), Color(0xFFEEF2FF))
}

val AppThemeColor.primaryColorInt: Int
    get() = when (this) {
        AppThemeColor.INDIGO -> android.graphics.Color.parseColor("#4F46E5")
        AppThemeColor.EMERALD -> android.graphics.Color.parseColor("#1B4A31")
        AppThemeColor.TEAL -> android.graphics.Color.parseColor("#0284C7")
        AppThemeColor.ROSE -> android.graphics.Color.parseColor("#E11D48")
        AppThemeColor.AMBER -> android.graphics.Color.parseColor("#D97706")
        AppThemeColor.PURPLE -> android.graphics.Color.parseColor("#7C3AED")
        AppThemeColor.CYAN -> android.graphics.Color.parseColor("#0D9488")
        AppThemeColor.CORAL -> android.graphics.Color.parseColor("#EA580C")
        AppThemeColor.SLATE -> android.graphics.Color.parseColor("#475569")
        AppThemeColor.CUSTOM -> android.graphics.Color.parseColor("#6366F1")
    }

val AppThemeColor.darkPrimaryColorInt: Int
    get() = when (this) {
        AppThemeColor.INDIGO -> android.graphics.Color.parseColor("#4338CA")
        AppThemeColor.EMERALD -> android.graphics.Color.parseColor("#047857")
        AppThemeColor.TEAL -> android.graphics.Color.parseColor("#0369A1")
        AppThemeColor.ROSE -> android.graphics.Color.parseColor("#BE123C")
        AppThemeColor.AMBER -> android.graphics.Color.parseColor("#B45309")
        AppThemeColor.PURPLE -> android.graphics.Color.parseColor("#6D28D9")
        AppThemeColor.CYAN -> android.graphics.Color.parseColor("#0F766E")
        AppThemeColor.CORAL -> android.graphics.Color.parseColor("#C2410C")
        AppThemeColor.SLATE -> android.graphics.Color.parseColor("#334155")
        AppThemeColor.CUSTOM -> android.graphics.Color.parseColor("#4F46E5")
    }

val AppThemeColor.containerColorInt: Int
    get() = when (this) {
        AppThemeColor.INDIGO -> android.graphics.Color.parseColor("#EEF2FF")
        AppThemeColor.EMERALD -> android.graphics.Color.parseColor("#ECFDF5")
        AppThemeColor.TEAL -> android.graphics.Color.parseColor("#E0F2FE")
        AppThemeColor.ROSE -> android.graphics.Color.parseColor("#FFE4E6")
        AppThemeColor.AMBER -> android.graphics.Color.parseColor("#FEF3C7")
        AppThemeColor.PURPLE -> android.graphics.Color.parseColor("#F3E8FF")
        AppThemeColor.CYAN -> android.graphics.Color.parseColor("#CCFBF1")
        AppThemeColor.CORAL -> android.graphics.Color.parseColor("#FFEDD5")
        AppThemeColor.SLATE -> android.graphics.Color.parseColor("#F1F5F9")
        AppThemeColor.CUSTOM -> android.graphics.Color.parseColor("#EEF2FF")
    }

enum class AppLanguage(val code: String, val displayName: String) {
    ZH("zh-TW", "繁體中文"),
    EN("en", "English")
}

data class ReminderTime(
    val id: String = UUID.randomUUID().toString(),
    val hour: Int,
    val minute: Int,
    val enabled: Boolean = true
)

class SettingsPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("debit_settings", Context.MODE_PRIVATE)

    fun getThemeColor(): AppThemeColor {
        val name = prefs.getString("theme_color", AppThemeColor.EMERALD.name)
        return try { AppThemeColor.valueOf(name!!) } catch (e: Exception) { AppThemeColor.EMERALD }
    }

    fun setThemeColor(color: AppThemeColor) {
        prefs.edit().putString("theme_color", color.name).apply()
    }

    fun isDarkMode(): Boolean {
        return prefs.getBoolean("dark_mode", false)
    }

    fun setDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean("dark_mode", enabled).apply()
    }

    fun getLanguage(): AppLanguage {
        val name = prefs.getString("language", AppLanguage.ZH.name)
        return try { AppLanguage.valueOf(name!!) } catch (e: Exception) { AppLanguage.ZH }
    }

    fun setLanguage(language: AppLanguage) {
        prefs.edit().putString("language", language.name).apply()
    }

    fun isBetaTestingEnabled(): Boolean {
        return prefs.getBoolean("beta_testing", false)
    }

    fun setBetaTestingEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("beta_testing", enabled).apply()
    }

    fun isThemePaletteEnabled(): Boolean = prefs.getBoolean("theme_palette_beta", false)
    fun setThemePaletteEnabled(enabled: Boolean) = prefs.edit().putBoolean("theme_palette_beta", enabled).apply()

    fun getCustomColorHex(): String? = prefs.getString("custom_color_hex", null)
    fun setCustomColorHex(hex: String?) = prefs.edit().putString("custom_color_hex", hex).apply()

    fun isIncomeTrackingEnabled(): Boolean = prefs.getBoolean("income_tracking", true)
    fun setIncomeTrackingEnabled(enabled: Boolean) = prefs.edit().putBoolean("income_tracking", enabled).apply()

    fun isSearchEnabled(): Boolean = prefs.getBoolean("search_feature", true)
    fun setSearchEnabled(enabled: Boolean) = prefs.edit().putBoolean("search_feature", enabled).apply()

    fun isSubscriptionEnabled(): Boolean = prefs.getBoolean("subscription_feature", true)
    fun setSubscriptionEnabled(enabled: Boolean) = prefs.edit().putBoolean("subscription_feature", enabled).apply()

    fun isMultiAccountEnabled(): Boolean = prefs.getBoolean("multi_account", true)
    fun setMultiAccountEnabled(enabled: Boolean) = prefs.edit().putBoolean("multi_account", enabled).apply()

    fun isGooglePayListenerEnabled(): Boolean = prefs.getBoolean("google_pay_listener", false)
    fun setGooglePayListenerEnabled(enabled: Boolean) = prefs.edit().putBoolean("google_pay_listener", enabled).apply()

    fun isConfirmDeleteEnabled(): Boolean = prefs.getBoolean("confirm_delete", true)
    fun setConfirmDeleteEnabled(enabled: Boolean) = prefs.edit().putBoolean("confirm_delete", enabled).apply()

    fun isDailyReminderEnabled(): Boolean = prefs.getBoolean("daily_reminder_enabled", false)
    fun setDailyReminderEnabled(enabled: Boolean) = prefs.edit().putBoolean("daily_reminder_enabled", enabled).apply()

    fun getReminderHour(): Int = prefs.getInt("reminder_hour", 21)
    fun getReminderMinute(): Int = prefs.getInt("reminder_minute", 0)
    fun setReminderTime(hour: Int, minute: Int) {
        prefs.edit().putInt("reminder_hour", hour).putInt("reminder_minute", minute).apply()
        val single = listOf(ReminderTime(id = "reminder_1", hour = hour, minute = minute, enabled = isDailyReminderEnabled()))
        saveReminderTimes(single)
    }

    fun getReminderTimes(): List<ReminderTime> {
        val raw = prefs.getString("reminder_times_v2", null)
        if (raw.isNullOrEmpty()) {
            val oldHour = getReminderHour()
            val oldMin = getReminderMinute()
            val defaultList = listOf(ReminderTime(id = "reminder_1", hour = oldHour, minute = oldMin, enabled = isDailyReminderEnabled()))
            saveReminderTimes(defaultList)
            return defaultList
        }
        return try {
            raw.split("|").filter { it.isNotBlank() }.map { item ->
                val parts = item.split(",")
                ReminderTime(
                    id = parts[0],
                    hour = parts[1].toInt(),
                    minute = parts[2].toInt(),
                    enabled = parts.getOrNull(3) == "1"
                )
            }
        } catch (_: Exception) {
            listOf(ReminderTime("reminder_1", 21, 0, true))
        }
    }

    fun saveReminderTimes(list: List<ReminderTime>) {
        val serialized = list.joinToString("|") { "${it.id},${it.hour},${it.minute},${if (it.enabled) 1 else 0}" }
        val first = list.firstOrNull()
        if (first != null) {
            prefs.edit()
                .putInt("reminder_hour", first.hour)
                .putInt("reminder_minute", first.minute)
                .putString("reminder_times_v2", serialized)
                .apply()
        } else {
            prefs.edit().putString("reminder_times_v2", serialized).apply()
        }
    }

    fun isModern3DUiEnabled(): Boolean = prefs.getBoolean("modern_3d_ui", true)
    fun setModern3DUiEnabled(enabled: Boolean) = prefs.edit().putBoolean("modern_3d_ui", enabled).apply()

    fun isDragDateReorderEnabled(): Boolean = prefs.getBoolean("drag_date_reorder", true)
    fun setDragDateReorderEnabled(enabled: Boolean) = prefs.edit().putBoolean("drag_date_reorder", enabled).apply()

    fun isAutoBackupEnabled(): Boolean {
        return prefs.getBoolean("auto_backup", true)
    }

    fun setAutoBackupEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("auto_backup", enabled).apply()
    }

    fun isInitialized(): Boolean {
        return prefs.getBoolean("is_initialized", false)
    }

    fun setInitialized(initialized: Boolean) {
        prefs.edit().putBoolean("is_initialized", initialized).apply()
    }

    fun getLastAutoBackupTime(): Long {
        return prefs.getLong("last_auto_backup_time", 0L)
    }

    fun setLastAutoBackupTime(time: Long) {
        prefs.edit().putLong("last_auto_backup_time", time).apply()
    }

    fun getCustomSubCategories(category: String): List<String> {
        val set = prefs.getStringSet("custom_subs_$category", emptySet()) ?: emptySet()
        return set.toList()
    }

    fun addCustomSubCategory(category: String, subCategory: String) {
        val trimmed = subCategory.trim()
        if (trimmed.isBlank()) return
        val current = getCustomSubCategories(category).toMutableList()
        if (!current.contains(trimmed)) {
            current.add(trimmed)
            prefs.edit().putStringSet("custom_subs_$category", current.toSet()).apply()
        }
    }
}
