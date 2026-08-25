package com.mdsultanmahamud.eyesavemode.service

import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import com.mdsultanmahamud.eyesavemode.data.SettingsRepository
import com.mdsultanmahamud.eyesavemode.model.FilterPreset

@RequiresApi(Build.VERSION_CODES.N)
class EyeSaveTileService : TileService() {

    private lateinit var settingsRepository: SettingsRepository

    override fun onCreate() {
        super.onCreate()
        settingsRepository = SettingsRepository(this)
    }

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()
        if (!Settings.canDrawOverlays(this)) {
            // Cannot enable overlay directly from tile without permission; prompt user by opening main app
            try {
                val intent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                if (intent != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        val pendingIntent = android.app.PendingIntent.getActivity(
                            this,
                            0,
                            intent,
                            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
                        )
                        startActivityAndCollapse(pendingIntent)
                    } else {
                        @Suppress("DEPRECATION")
                        startActivityAndCollapse(intent)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            updateTileState()
            return
        }

        val current = settingsRepository.settings.value
        val newEnabled = !current.isEnabled

        settingsRepository.updateSettings { it.copy(isEnabled = newEnabled) }

        if (newEnabled) {
            val preset = FilterPreset.findPreset(current.activePresetId)
            val r = if (current.activePresetId == "custom") current.customR else preset.red
            val g = if (current.activePresetId == "custom") current.customG else preset.green
            val b = if (current.activePresetId == "custom") current.customB else preset.blue
            val intensity = if (current.activePresetId == "custom") current.filterIntensity else preset.defaultIntensity

            ScreenFilterService.startOrUpdate(
                context = this,
                dimmingPercent = current.dimmingPercent,
                r = r,
                g = g,
                b = b,
                intensity = intensity,
                presetName = if (current.activePresetId == "custom") "Custom RGB" else preset.name,
                pauseWhenScreenOff = current.pauseWhenScreenOff
            )
        } else {
            ScreenFilterService.stop(this)
        }

        updateTileState()
    }

    private fun updateTileState() {
        val tile = qsTile ?: return
        val current = settingsRepository.settings.value
        val hasPermission = Settings.canDrawOverlays(this)

        if (current.isEnabled && hasPermission) {
            tile.state = Tile.STATE_ACTIVE
            tile.label = "Eye Save: ON"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.subtitle = "${current.dimmingPercent}% Dim"
            }
        } else {
            tile.state = Tile.STATE_INACTIVE
            tile.label = "Eye Save: OFF"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.subtitle = if (!hasPermission) "Grant Permission" else "Tap to Enable"
            }
        }
        tile.updateTile()
    }
}
