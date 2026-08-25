package com.mdsultanmahamud.eyesavemode.util

import android.app.Activity
import android.content.Context
import android.provider.Settings
import android.view.WindowManager

object SystemBrightnessHelper {

    fun getDeviceBrightness(context: Context): Int {
        return try {
            val brightness = Settings.System.getInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                128
            )
            ((brightness / 255f) * 100).toInt()
        } catch (e: Exception) {
            50
        }
    }

    fun setWindowBrightness(activity: Activity, brightnessPercent: Int) {
        try {
            val lp = activity.window.attributes
            lp.screenBrightness = if (brightnessPercent < 0) {
                WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
            } else {
                (brightnessPercent / 100f).coerceIn(0.01f, 1.0f)
            }
            activity.window.attributes = lp
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
