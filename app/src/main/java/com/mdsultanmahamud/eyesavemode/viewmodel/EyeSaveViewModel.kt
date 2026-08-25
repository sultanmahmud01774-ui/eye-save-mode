package com.mdsultanmahamud.eyesavemode.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mdsultanmahamud.eyesavemode.EyeSaveApplication
import com.mdsultanmahamud.eyesavemode.model.EyeGuardStage
import com.mdsultanmahamud.eyesavemode.model.EyeSaveSettings
import com.mdsultanmahamud.eyesavemode.model.FilterPreset
import com.mdsultanmahamud.eyesavemode.model.ScheduleItem
import com.mdsultanmahamud.eyesavemode.receiver.ScheduleReceiver
import com.mdsultanmahamud.eyesavemode.service.RelaxReminderManager
import com.mdsultanmahamud.eyesavemode.service.ScreenFilterService
import com.mdsultanmahamud.eyesavemode.service.ShakeDetector
import com.mdsultanmahamud.eyesavemode.service.SmartEyeGuardEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID

class EyeSaveViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as EyeSaveApplication
    private val settingsRepository = app.settingsRepository
    private val customPresetDao = app.database.customPresetDao()
    private val scheduleDao = app.database.scheduleDao()

    val settings: StateFlow<EyeSaveSettings> = settingsRepository.settings

    val customPresets: StateFlow<List<FilterPreset>> = customPresetDao.getAllPresets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val schedules: StateFlow<List<ScheduleItem>> = scheduleDao.getAllSchedules()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _hasOverlayPermission = MutableStateFlow(Settings.canDrawOverlays(application))
    val hasOverlayPermission: StateFlow<Boolean> = _hasOverlayPermission.asStateFlow()

    private val _hasNotificationPermission = MutableStateFlow(
        NotificationManagerCompat.from(application).areNotificationsEnabled()
    )
    val hasNotificationPermission: StateFlow<Boolean> = _hasNotificationPermission.asStateFlow()

    private val _currentLightLux = MutableStateFlow(120f)
    val currentLightLux: StateFlow<Float> = _currentLightLux.asStateFlow()

    private val _currentEyeGuardStage = MutableStateFlow(
        EyeGuardStage.getCurrentStage(Calendar.getInstance().get(Calendar.HOUR_OF_DAY))
    )
    val currentEyeGuardStage: StateFlow<EyeGuardStage> = _currentEyeGuardStage.asStateFlow()

    private var shakeDetector: ShakeDetector? = null
    private var smartEyeGuardEngine: SmartEyeGuardEngine? = null

    init {
        initDefaultSchedulesIfEmpty()
        initEngines()
        syncServiceState()
    }

    fun refreshPermissions() {
        _hasOverlayPermission.value = Settings.canDrawOverlays(getApplication())
        _hasNotificationPermission.value = NotificationManagerCompat.from(getApplication()).areNotificationsEnabled()
    }

    private fun initDefaultSchedulesIfEmpty() {
        viewModelScope.launch {
            // Bug fix: this used to check getEnabledSchedules(), but the seeded
            // defaults are inserted with isEnabled = false, so that check was
            // always empty and re-inserted 3 duplicate schedules on every app
            // launch. Checking the total row count fixes the "seed once" logic.
            val existingCount = scheduleDao.getScheduleCount()
            if (existingCount == 0) {
                // Prepopulate standard presets
                scheduleDao.insertSchedule(
                    ScheduleItem(
                        title = "Sunset Mode",
                        startHour = 18,
                        startMinute = 30,
                        endHour = 22,
                        endMinute = 0,
                        dimmingPercent = 35,
                        filterPresetId = "sunset_orange",
                        filterR = 255,
                        filterG = 115,
                        filterB = 0,
                        filterIntensity = 50,
                        isEnabled = false,
                        isSmartPreset = true
                    )
                )
                scheduleDao.insertSchedule(
                    ScheduleItem(
                        title = "Night Comfort",
                        startHour = 22,
                        startMinute = 0,
                        endHour = 6,
                        endMinute = 30,
                        dimmingPercent = 65,
                        filterPresetId = "warm_night",
                        filterR = 255,
                        filterG = 147,
                        filterB = 41,
                        filterIntensity = 60,
                        isEnabled = false,
                        isSmartPreset = true
                    )
                )
                scheduleDao.insertSchedule(
                    ScheduleItem(
                        title = "Bedtime Sleep Guard",
                        startHour = 23,
                        startMinute = 0,
                        endHour = 7,
                        endMinute = 0,
                        dimmingPercent = 80,
                        filterPresetId = "deep_red",
                        filterR = 235,
                        filterG = 55,
                        filterB = 55,
                        filterIntensity = 75,
                        isEnabled = false,
                        isSmartPreset = true
                    )
                )
            }
        }
    }

    private fun initEngines() {
        val context = getApplication<Application>()

        // Shake Detector
        shakeDetector = ShakeDetector(context) {
            if (settings.value.shakeActionEnabled) {
                // Shake detected: quick toggle overlay mode
                togglePower(!settings.value.isEnabled)
                Toast.makeText(context, "👁️ Shake Detected: Toggled Eye Save Mode", Toast.LENGTH_SHORT).show()
            }
        }.apply {
            sensitivityThreshold = settings.value.shakeSensitivity
            if (settings.value.shakeActionEnabled) {
                start()
            }
        }

        // Smart Eye Guard Engine
        smartEyeGuardEngine = SmartEyeGuardEngine(context) { stage, lux ->
            val stageChanged = stage != _currentEyeGuardStage.value
            _currentEyeGuardStage.value = stage
            _currentLightLux.value = lux

            // Only push a settings update / overlay restart when the stage
            // actually changed, not on every periodic lux refresh.
            if (stageChanged && settings.value.smartEyeGuardEnabled && settings.value.isEnabled) {
                applySmartEyeGuardStage(stage)
            }
        }
        if (settings.value.smartEyeGuardEnabled) {
            smartEyeGuardEngine?.start()
        }
    }

    private fun applySmartEyeGuardStage(stage: EyeGuardStage) {
        settingsRepository.updateSettings { current ->
            current.copy(
                dimmingPercent = stage.dimmingPercent,
                customR = stage.r,
                customG = stage.g,
                customB = stage.b,
                filterIntensity = stage.intensity,
                activePresetId = "custom"
            )
        }
        syncServiceState()
    }

    fun togglePower(enabled: Boolean) {
        settingsRepository.updateSettings { it.copy(isEnabled = enabled) }
        syncServiceState()
    }

    fun setDimmingPercent(percent: Int) {
        val clamped = percent.coerceIn(0, 90)
        settingsRepository.updateSettings { it.copy(dimmingPercent = clamped) }
        syncServiceState()
    }

    fun setActivePreset(presetId: String) {
        val preset = FilterPreset.findPreset(presetId, customPresets.value)
        settingsRepository.updateSettings { current ->
            current.copy(
                activePresetId = presetId,
                customR = preset.red,
                customG = preset.green,
                customB = preset.blue,
                filterIntensity = preset.defaultIntensity
            )
        }
        syncServiceState()
    }

    fun setCustomRgb(r: Int, g: Int, b: Int, intensity: Int) {
        settingsRepository.updateSettings { current ->
            current.copy(
                activePresetId = "custom",
                customR = r.coerceIn(0, 255),
                customG = g.coerceIn(0, 255),
                customB = b.coerceIn(0, 255),
                filterIntensity = intensity.coerceIn(0, 100)
            )
        }
        syncServiceState()
    }

    fun saveCustomPreset(name: String, r: Int, g: Int, b: Int, intensity: Int) {
        viewModelScope.launch {
            val preset = FilterPreset(
                id = "custom_" + UUID.randomUUID().toString().take(8),
                name = name.ifBlank { "Custom Preset" },
                red = r,
                green = g,
                blue = b,
                defaultIntensity = intensity,
                isBuiltIn = false,
                description = "User created custom RGB profile"
            )
            customPresetDao.insertPreset(preset)
            setActivePreset(preset.id)
        }
    }

    fun deleteCustomPreset(presetId: String) {
        viewModelScope.launch {
            customPresetDao.deletePresetById(presetId)
            if (settings.value.activePresetId == presetId) {
                setActivePreset("warm_night")
            }
        }
    }

    fun renameCustomPreset(preset: FilterPreset, newName: String) {
        viewModelScope.launch {
            val updated = preset.copy(name = newName.ifBlank { preset.name })
            customPresetDao.updatePreset(updated)
        }
    }

    fun toggleSmartEyeGuard(enabled: Boolean) {
        settingsRepository.updateSettings { it.copy(smartEyeGuardEnabled = enabled) }
        if (enabled) {
            smartEyeGuardEngine?.start()
            val stage = smartEyeGuardEngine?.evaluateCurrentProfile(forceNotify = true) ?: _currentEyeGuardStage.value
            applySmartEyeGuardStage(stage)
        } else {
            smartEyeGuardEngine?.stop()
            syncServiceState()
        }
    }

    fun addOrUpdateSchedule(schedule: ScheduleItem) {
        viewModelScope.launch {
            val context = getApplication<Application>()
            val id = if (schedule.id == 0L) {
                scheduleDao.insertSchedule(schedule)
            } else {
                scheduleDao.updateSchedule(schedule)
                schedule.id
            }

            val saved = schedule.copy(id = id)
            if (saved.isEnabled) {
                ScheduleReceiver.scheduleAlarm(context, saved)
            } else {
                ScheduleReceiver.cancelScheduleAlarm(context, saved.id)
            }
        }
    }

    fun toggleSchedule(schedule: ScheduleItem, enabled: Boolean) {
        viewModelScope.launch {
            val context = getApplication<Application>()
            val updated = schedule.copy(isEnabled = enabled)
            scheduleDao.updateSchedule(updated)

            if (enabled) {
                ScheduleReceiver.scheduleAlarm(context, updated)
            } else {
                ScheduleReceiver.cancelScheduleAlarm(context, updated.id)
            }
        }
    }

    fun deleteSchedule(schedule: ScheduleItem) {
        viewModelScope.launch {
            val context = getApplication<Application>()
            ScheduleReceiver.cancelScheduleAlarm(context, schedule.id)
            scheduleDao.deleteSchedule(schedule)
        }
    }

    fun toggleRelaxReminder(enabled: Boolean, intervalMinutes: Int = settings.value.relaxReminderIntervalMinutes) {
        val context = getApplication<Application>()
        settingsRepository.updateSettings {
            it.copy(
                relaxReminderEnabled = enabled,
                relaxReminderIntervalMinutes = intervalMinutes
            )
        }
        if (enabled) {
            RelaxReminderManager.scheduleReminder(context, intervalMinutes)
            Toast.makeText(context, "👁️ Relax reminder set for every $intervalMinutes mins", Toast.LENGTH_SHORT).show()
        } else {
            RelaxReminderManager.cancelReminder(context)
            Toast.makeText(context, "Relax reminder disabled", Toast.LENGTH_SHORT).show()
        }
    }

    fun toggleShakeAction(enabled: Boolean, sensitivity: Float = settings.value.shakeSensitivity) {
        settingsRepository.updateSettings {
            it.copy(
                shakeActionEnabled = enabled,
                shakeSensitivity = sensitivity
            )
        }
        shakeDetector?.sensitivityThreshold = sensitivity
        if (enabled) {
            shakeDetector?.start()
        } else {
            shakeDetector?.stop()
        }
    }

    fun setThemeMode(themeMode: String) {
        settingsRepository.updateSettings { it.copy(themeMode = themeMode) }
    }

    fun setStartOnBoot(enabled: Boolean) {
        settingsRepository.updateSettings { it.copy(startOnBoot = enabled) }
    }

    fun setPauseWhenScreenOff(enabled: Boolean) {
        settingsRepository.updateSettings { it.copy(pauseWhenScreenOff = enabled) }
        syncServiceState()
    }

    fun fixHalfScreenIssue() {
        val context = getApplication<Application>()
        settingsRepository.updateSettings { it.copy(halfScreenFixApplied = true) }
        if (settings.value.isEnabled) {
            ScreenFilterService.stop(context)
            syncServiceState()
        }
        Toast.makeText(context, "Screen bounds & cutout layout reset successfully!", Toast.LENGTH_SHORT).show()
    }

    fun safeResetAll() {
        val context = getApplication<Application>()
        ScreenFilterService.stop(context)
        RelaxReminderManager.cancelReminder(context)
        shakeDetector?.stop()
        smartEyeGuardEngine?.stop()
        settingsRepository.resetToDefaults()
        syncServiceState()
        Toast.makeText(context, "All Eye Save Mode settings safely restored to factory defaults", Toast.LENGTH_LONG).show()
    }

    fun setPremiumUnlocked(unlocked: Boolean) {
        settingsRepository.updateSettings { it.copy(isPremiumUnlocked = unlocked) }
    }

    fun exportSettingsJson(): String {
        return settingsRepository.exportSettingsJson()
    }

    fun importSettingsJson(json: String): Boolean {
        val success = settingsRepository.importSettingsJson(json)
        if (success) {
            syncServiceState()
        }
        return success
    }

    fun syncServiceState() {
        val context = getApplication<Application>()
        val current = settings.value
        refreshPermissions()

        if (current.isEnabled && _hasOverlayPermission.value) {
            val preset = FilterPreset.findPreset(current.activePresetId, customPresets.value)
            val r = if (current.activePresetId == "custom") current.customR else preset.red
            val g = if (current.activePresetId == "custom") current.customG else preset.green
            val b = if (current.activePresetId == "custom") current.customB else preset.blue
            val intensity = if (current.activePresetId == "custom") current.filterIntensity else preset.defaultIntensity

            ScreenFilterService.startOrUpdate(
                context = context,
                dimmingPercent = current.dimmingPercent,
                r = r,
                g = g,
                b = b,
                intensity = intensity,
                presetName = if (current.activePresetId == "custom") "Custom RGB" else preset.name,
                pauseWhenScreenOff = current.pauseWhenScreenOff
            )
        } else if (!current.isEnabled) {
            ScreenFilterService.stop(context)
        }
    }

    override fun onCleared() {
        super.onCleared()
        shakeDetector?.stop()
        smartEyeGuardEngine?.stop()
    }
}
