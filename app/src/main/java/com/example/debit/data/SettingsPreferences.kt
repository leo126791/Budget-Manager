package com.example.debit.data

import android.content.Context
import androidx.compose.ui.graphics.Color

enum class AppThemeColor(val displayNameZh: String, val displayNameEn: String, val primaryColor: Color, val containerColor: Color) {
    INDIGO("經典靛藍", "Indigo", Color(0xFF4F46E5), Color(0xFFEEF2FF)),
    EMERALD("翡翠綠", "Emerald Green", Color(0xFF059669), Color(0xFFECFDF5)),
    TEAL("電光藍", "Electric Blue", Color(0xFF0284C7), Color(0xFFE0F2FE)),
    ROSE("玫瑰紅", "Rose Pink", Color(0xFFE11D48), Color(0xFFFFE4E6)),
    SLATE("深夜灰", "Dark Slate", Color(0xFF475569), Color(0xFFF1F5F9))
}

val AppThemeColor.primaryColorInt: Int
    get() = when (this) {
        AppThemeColor.INDIGO -> android.graphics.Color.parseColor("#4F46E5")
        AppThemeColor.EMERALD -> android.graphics.Color.parseColor("#059669")
        AppThemeColor.TEAL -> android.graphics.Color.parseColor("#0284C7")
        AppThemeColor.ROSE -> android.graphics.Color.parseColor("#E11D48")
        AppThemeColor.SLATE -> android.graphics.Color.parseColor("#475569")
    }

val AppThemeColor.darkPrimaryColorInt: Int
    get() = when (this) {
        AppThemeColor.INDIGO -> android.graphics.Color.parseColor("#4338CA")
        AppThemeColor.EMERALD -> android.graphics.Color.parseColor("#047857")
        AppThemeColor.TEAL -> android.graphics.Color.parseColor("#0369A1")
        AppThemeColor.ROSE -> android.graphics.Color.parseColor("#BE123C")
        AppThemeColor.SLATE -> android.graphics.Color.parseColor("#334155")
    }

val AppThemeColor.containerColorInt: Int
    get() = when (this) {
        AppThemeColor.INDIGO -> android.graphics.Color.parseColor("#EEF2FF")
        AppThemeColor.EMERALD -> android.graphics.Color.parseColor("#ECFDF5")
        AppThemeColor.TEAL -> android.graphics.Color.parseColor("#E0F2FE")
        AppThemeColor.ROSE -> android.graphics.Color.parseColor("#FFE4E6")
        AppThemeColor.SLATE -> android.graphics.Color.parseColor("#F1F5F9")
    }

enum class AppLanguage(val code: String, val displayName: String) {
    ZH("zh-TW", "繁體中文"),
    EN("en", "English")
}

class SettingsPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("debit_settings", Context.MODE_PRIVATE)

    fun getThemeColor(): AppThemeColor {
        val name = prefs.getString("theme_color", AppThemeColor.INDIGO.name)
        return try { AppThemeColor.valueOf(name!!) } catch (e: Exception) { AppThemeColor.INDIGO }
    }

    fun setThemeColor(color: AppThemeColor) {
        prefs.edit().putString("theme_color", color.name).apply()
    }

    fun getLanguage(): AppLanguage {
        val name = prefs.getString("language", AppLanguage.ZH.name)
        return try { AppLanguage.valueOf(name!!) } catch (e: Exception) { AppLanguage.ZH }
    }

    fun setLanguage(language: AppLanguage) {
        prefs.edit().putString("language", language.name).apply()
    }

    fun isBetaTestingEnabled(): Boolean {
        return prefs.getBoolean("beta_testing", true)
    }

    fun setBetaTestingEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("beta_testing", enabled).apply()
    }

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
