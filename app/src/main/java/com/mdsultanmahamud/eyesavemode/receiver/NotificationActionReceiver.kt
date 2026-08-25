package com.mdsultanmahamud.eyesavemode.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.mdsultanmahamud.eyesavemode.data.SettingsRepository
import com.mdsultanmahamud.eyesavemode.model.FilterPreset
import com.mdsultanmahamud.eyesavemode.service.ScreenFilterService

class NotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val repo = SettingsRepository(context)
        val current = repo.settings.value

        when (intent.action) {
            ACTION_TOGGLE_POWER -> {
                val newEnabled = !current.isEnabled
                repo.updateSettings { it.copy(isEnabled = newEnabled) }

                if (newEnabled && Settings.canDrawOverlays(context)) {
                    val preset = FilterPreset.findPreset(current.activePresetId)
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
                } else {
                    ScreenFilterService.stop(context)
                }
            }
            ACTION_INCREASE_DIM -> {
                val newDim = (current.dimmingPercent + 10).coerceAtMost(90)
                repo.updateSettings { it.copy(dimmingPercent = newDim) }
                if (current.isEnabled && Settings.canDrawOverlays(context)) {
                    val preset = FilterPreset.findPreset(current.activePresetId)
                    ScreenFilterService.startOrUpdate(
                        context = context,
                        dimmingPercent = newDim,
                        r = if (current.activePresetId == "custom") current.customR else preset.red,
                        g = if (current.activePresetId == "custom") current.customG else preset.green,
                        b = if (current.activePresetId == "custom") current.customB else preset.blue,
                        intensity = if (current.activePresetId == "custom") current.filterIntensity else preset.defaultIntensity,
                        presetName = if (current.activePresetId == "custom") "Custom RGB" else preset.name,
                        pauseWhenScreenOff = current.pauseWhenScreenOff
                    )
                }
            }
            ACTION_DECREASE_DIM -> {
                val newDim = (current.dimmingPercent - 10).coerceAtLeast(0)
                repo.updateSettings { it.copy(dimmingPercent = newDim) }
                if (current.isEnabled && Settings.canDrawOverlays(context)) {
                    val preset = FilterPreset.findPreset(current.activePresetId)
                    ScreenFilterService.startOrUpdate(
                        context = context,
                        dimmingPercent = newDim,
                        r = if (current.activePresetId == "custom") current.customR else preset.red,
                        g = if (current.activePresetId == "custom") current.customG else preset.green,
                        b = if (current.activePresetId == "custom") current.customB else preset.blue,
                        intensity = if (current.activePresetId == "custom") current.filterIntensity else preset.defaultIntensity,
                        presetName = if (current.activePresetId == "custom") "Custom RGB" else preset.name,
                        pauseWhenScreenOff = current.pauseWhenScreenOff
                    )
                }
            }
        }
    }

    companion object {
        const val ACTION_TOGGLE_POWER = "com.mdsultanmahamud.eyesavemode.action.NOTIF_TOGGLE_POWER"
        const val ACTION_INCREASE_DIM = "com.mdsultanmahamud.eyesavemode.action.NOTIF_INC_DIM"
        const val ACTION_DECREASE_DIM = "com.mdsultanmahamud.eyesavemode.action.NOTIF_DEC_DIM"
    }
}
