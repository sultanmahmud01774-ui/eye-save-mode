package com.mdsultanmahamud.eyesavemode.data

import android.content.Context
import android.content.SharedPreferences
import com.mdsultanmahamud.eyesavemode.model.EyeSaveSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

class SettingsRepository(private val context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("eye_save_preferences", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<EyeSaveSettings> = _settings.asStateFlow()

    private fun loadSettings(): EyeSaveSettings {
        return EyeSaveSettings(
            isEnabled = prefs.getBoolean(KEY_IS_ENABLED, false),
            dimmingPercent = prefs.getInt(KEY_DIMMING_PERCENT, 45),
            activePresetId = prefs.getString(KEY_ACTIVE_PRESET_ID, "warm_night") ?: "warm_night",
            customR = prefs.getInt(KEY_CUSTOM_R, 255),
            customG = prefs.getInt(KEY_CUSTOM_G, 147),
            customB = prefs.getInt(KEY_CUSTOM_B, 41),
            filterIntensity = prefs.getInt(KEY_FILTER_INTENSITY, 50),
            smartEyeGuardEnabled = prefs.getBoolean(KEY_SMART_GUARD_ENABLED, false),
            pauseWhenScreenOff = prefs.getBoolean(KEY_PAUSE_SCREEN_OFF, true),
            shakeActionEnabled = prefs.getBoolean(KEY_SHAKE_ACTION, false),
            shakeSensitivity = prefs.getFloat(KEY_SHAKE_SENSITIVITY, 12f),
            relaxReminderEnabled = prefs.getBoolean(KEY_RELAX_REMINDER, false),
            relaxReminderIntervalMinutes = prefs.getInt(KEY_RELAX_INTERVAL, 20),
            startOnBoot = prefs.getBoolean(KEY_START_ON_BOOT, true),
            themeMode = prefs.getString(KEY_THEME_MODE, "dark") ?: "dark",
            isPremiumUnlocked = prefs.getBoolean(KEY_IS_PREMIUM, false),
            halfScreenFixApplied = prefs.getBoolean(KEY_HALF_SCREEN_FIX, false),
            lastActiveTimestamp = prefs.getLong(KEY_LAST_ACTIVE, System.currentTimeMillis())
        )
    }

    fun updateSettings(update: (EyeSaveSettings) -> EyeSaveSettings) {
        val newSettings = update(_settings.value)
        prefs.edit().apply {
            putBoolean(KEY_IS_ENABLED, newSettings.isEnabled)
            putInt(KEY_DIMMING_PERCENT, newSettings.dimmingPercent)
            putString(KEY_ACTIVE_PRESET_ID, newSettings.activePresetId)
            putInt(KEY_CUSTOM_R, newSettings.customR)
            putInt(KEY_CUSTOM_G, newSettings.customG)
            putInt(KEY_CUSTOM_B, newSettings.customB)
            putInt(KEY_FILTER_INTENSITY, newSettings.filterIntensity)
            putBoolean(KEY_SMART_GUARD_ENABLED, newSettings.smartEyeGuardEnabled)
            putBoolean(KEY_PAUSE_SCREEN_OFF, newSettings.pauseWhenScreenOff)
            putBoolean(KEY_SHAKE_ACTION, newSettings.shakeActionEnabled)
            putFloat(KEY_SHAKE_SENSITIVITY, newSettings.shakeSensitivity)
            putBoolean(KEY_RELAX_REMINDER, newSettings.relaxReminderEnabled)
            putInt(KEY_RELAX_INTERVAL, newSettings.relaxReminderIntervalMinutes)
            putBoolean(KEY_START_ON_BOOT, newSettings.startOnBoot)
            putString(KEY_THEME_MODE, newSettings.themeMode)
            putBoolean(KEY_IS_PREMIUM, newSettings.isPremiumUnlocked)
            putBoolean(KEY_HALF_SCREEN_FIX, newSettings.halfScreenFixApplied)
            putLong(KEY_LAST_ACTIVE, System.currentTimeMillis())
            apply()
        }
        _settings.value = newSettings
    }

    fun exportSettingsJson(): String {
        val s = _settings.value
        val json = JSONObject().apply {
            put("dimmingPercent", s.dimmingPercent)
            put("activePresetId", s.activePresetId)
            put("customR", s.customR)
            put("customG", s.customG)
            put("customB", s.customB)
            put("filterIntensity", s.filterIntensity)
            put("smartEyeGuardEnabled", s.smartEyeGuardEnabled)
            put("pauseWhenScreenOff", s.pauseWhenScreenOff)
            put("relaxReminderEnabled", s.relaxReminderEnabled)
            put("relaxReminderIntervalMinutes", s.relaxReminderIntervalMinutes)
            put("startOnBoot", s.startOnBoot)
            put("themeMode", s.themeMode)
            put("exportedAt", System.currentTimeMillis())
        }
        return json.toString(2)
    }

    fun importSettingsJson(jsonString: String): Boolean {
        return try {
            val json = JSONObject(jsonString)
            updateSettings { current ->
                val safeTheme = when (val t = json.optString("themeMode", current.themeMode)) {
                    "light", "amoled", "dark" -> t
                    else -> "dark"
                }
                current.copy(
                    dimmingPercent = json.optInt("dimmingPercent", current.dimmingPercent).coerceIn(0, 90),
                    activePresetId = json.optString("activePresetId", current.activePresetId),
                    customR = json.optInt("customR", current.customR).coerceIn(0, 255),
                    customG = json.optInt("customG", current.customG).coerceIn(0, 255),
                    customB = json.optInt("customB", current.customB).coerceIn(0, 255),
                    filterIntensity = json.optInt("filterIntensity", current.filterIntensity).coerceIn(0, 100),
                    smartEyeGuardEnabled = json.optBoolean("smartEyeGuardEnabled", current.smartEyeGuardEnabled),
                    pauseWhenScreenOff = json.optBoolean("pauseWhenScreenOff", current.pauseWhenScreenOff),
                    relaxReminderEnabled = json.optBoolean("relaxReminderEnabled", current.relaxReminderEnabled),
                    relaxReminderIntervalMinutes = json.optInt("relaxReminderIntervalMinutes", current.relaxReminderIntervalMinutes).coerceIn(5, 120),
                    startOnBoot = json.optBoolean("startOnBoot", current.startOnBoot),
                    themeMode = safeTheme
                )
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun resetToDefaults() {
        prefs.edit().clear().apply()
        _settings.value = EyeSaveSettings()
    }

    companion object {
        private const val KEY_IS_ENABLED = "is_enabled"
        private const val KEY_DIMMING_PERCENT = "dimming_percent"
        private const val KEY_ACTIVE_PRESET_ID = "active_preset_id"
        private const val KEY_CUSTOM_R = "custom_r"
        private const val KEY_CUSTOM_G = "custom_g"
        private const val KEY_CUSTOM_B = "custom_b"
        private const val KEY_FILTER_INTENSITY = "filter_intensity"
        private const val KEY_SMART_GUARD_ENABLED = "smart_guard_enabled"
        private const val KEY_PAUSE_SCREEN_OFF = "pause_screen_off"
        private const val KEY_SHAKE_ACTION = "shake_screenshot"
        private const val KEY_SHAKE_SENSITIVITY = "shake_sensitivity"
        private const val KEY_RELAX_REMINDER = "relax_reminder"
        private const val KEY_RELAX_INTERVAL = "relax_interval"
        private const val KEY_START_ON_BOOT = "start_on_boot"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_IS_PREMIUM = "is_premium"
        private const val KEY_HALF_SCREEN_FIX = "half_screen_fix"
        private const val KEY_LAST_ACTIVE = "last_active"
    }
}
