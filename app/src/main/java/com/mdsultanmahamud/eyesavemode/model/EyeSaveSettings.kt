package com.mdsultanmahamud.eyesavemode.model

data class EyeSaveSettings(
    val isEnabled: Boolean = false,
    val dimmingPercent: Int = 45,
    val activePresetId: String = "warm_night",
    val customR: Int = 255,
    val customG: Int = 147,
    val customB: Int = 41,
    val filterIntensity: Int = 50,
    val smartEyeGuardEnabled: Boolean = false,
    val pauseWhenScreenOff: Boolean = true,
    val shakeActionEnabled: Boolean = false,
    val shakeSensitivity: Float = 12f,
    val relaxReminderEnabled: Boolean = false,
    val relaxReminderIntervalMinutes: Int = 20,
    val startOnBoot: Boolean = true,
    val themeMode: String = "dark",
    val isPremiumUnlocked: Boolean = false,
    val halfScreenFixApplied: Boolean = false,
    val lastActiveTimestamp: Long = System.currentTimeMillis()
)
