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

enum class AppLanguage(val code: String, val displayName: String) {
    ZH("zh", "繁體中文"),
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
        return prefs.getBoolean("beta_testing", false)
    }

    fun setBetaTestingEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("beta_testing", enabled).apply()
    }

    fun isLocationPredictionEnabled(): Boolean {
        return prefs.getBoolean("location_prediction", false)
    }

    fun setLocationPredictionEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("location_prediction", enabled).apply()
    }

    fun isAutoBackupEnabled(): Boolean {
        return prefs.getBoolean("auto_backup", true)
    }

    fun setAutoBackupEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("auto_backup", enabled).apply()
    }

    fun getLastAutoBackupTime(): Long {
        return prefs.getLong("last_auto_backup_time", 0L)
    }

    fun setLastAutoBackupTime(time: Long) {
        prefs.edit().putLong("last_auto_backup_time", time).apply()
    }
}
