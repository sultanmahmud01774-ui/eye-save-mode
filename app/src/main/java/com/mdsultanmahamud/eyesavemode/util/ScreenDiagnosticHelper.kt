package com.mdsultanmahamud.eyesavemode.util

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.core.app.NotificationManagerCompat

data class DiagnosticStatus(
    val overlayPermissionGranted: Boolean,
    val notificationPermissionGranted: Boolean,
    val batteryOptimizationIgnored: Boolean,
    val screenResolution: String,
    val screenDensityDpi: Int,
    val orientation: String,
    val isCutoutSupported: Boolean,
    val isAmoledRecommended: Boolean,
    val isPowerSaveModeActive: Boolean,
    val androidVersion: String
)

object ScreenDiagnosticHelper {

    fun runDiagnostics(context: Context): DiagnosticStatus {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        val dm = DisplayMetrics()
        @Suppress("DEPRECATION")
        wm?.defaultDisplay?.getRealMetrics(dm)

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val isPowerSave = powerManager?.isPowerSaveMode == true
        val isBatteryOptimizationIgnored = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            powerManager?.isIgnoringBatteryOptimizations(context.packageName) == true
        } else {
            true
        }

        val orientation = if (context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            "Landscape"
        } else {
            "Portrait"
        }

        val isCutout = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P

        return DiagnosticStatus(
            overlayPermissionGranted = Settings.canDrawOverlays(context),
            notificationPermissionGranted = NotificationManagerCompat.from(context).areNotificationsEnabled(),
            batteryOptimizationIgnored = isBatteryOptimizationIgnored,
            screenResolution = "${dm.widthPixels} x ${dm.heightPixels}",
            screenDensityDpi = dm.densityDpi,
            orientation = orientation,
            isCutoutSupported = isCutout,
            isAmoledRecommended = true,
            isPowerSaveModeActive = isPowerSave,
            androidVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
        )
    }
}
