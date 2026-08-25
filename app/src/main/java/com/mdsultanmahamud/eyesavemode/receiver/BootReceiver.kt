package com.mdsultanmahamud.eyesavemode.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.mdsultanmahamud.eyesavemode.data.AppDatabase
import com.mdsultanmahamud.eyesavemode.data.SettingsRepository
import com.mdsultanmahamud.eyesavemode.model.FilterPreset
import com.mdsultanmahamud.eyesavemode.service.RelaxReminderManager
import com.mdsultanmahamud.eyesavemode.service.ScreenFilterService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            val repository = SettingsRepository(context)
            val settings = repository.settings.value

            if (settings.startOnBoot && settings.isEnabled && Settings.canDrawOverlays(context)) {
                val preset = FilterPreset.findPreset(settings.activePresetId)
                val r = if (settings.activePresetId == "custom") settings.customR else preset.red
                val g = if (settings.activePresetId == "custom") settings.customG else preset.green
                val b = if (settings.activePresetId == "custom") settings.customB else preset.blue
                val intensity = if (settings.activePresetId == "custom") settings.filterIntensity else preset.defaultIntensity

                ScreenFilterService.startOrUpdate(
                    context = context,
                    dimmingPercent = settings.dimmingPercent,
                    r = r,
                    g = g,
                    b = b,
                    intensity = intensity,
                    presetName = if (settings.activePresetId == "custom") "Custom RGB" else preset.name,
                    pauseWhenScreenOff = settings.pauseWhenScreenOff
                )
            }

            if (settings.relaxReminderEnabled) {
                RelaxReminderManager.scheduleReminder(context, settings.relaxReminderIntervalMinutes)
            }

            // Reschedule active schedules
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getDatabase(context)
                    val enabledSchedules = db.scheduleDao().getEnabledSchedules()
                    enabledSchedules.forEach { schedule ->
                        ScheduleReceiver.scheduleAlarm(context, schedule)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
