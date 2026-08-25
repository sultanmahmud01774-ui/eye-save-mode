package com.mdsultanmahamud.eyesavemode.service

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.mdsultanmahamud.eyesavemode.model.EyeGuardStage
import java.util.Calendar

class SmartEyeGuardEngine(
    private val context: Context,
    private val onStageChanged: (EyeGuardStage, Float) -> Unit
) : SensorEventListener {

    private var sensorManager: SensorManager? = null
    private var lightSensor: Sensor? = null
    private var currentLux: Float = 100f
    private var isListening = false
    private var lastReportedStage: EyeGuardStage? = null
    private var lastLuxReportTime: Long = 0L

    fun start() {
        if (isListening) return
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        lightSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT)

        lightSensor?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
            isListening = true
        }

        evaluateCurrentProfile(forceNotify = true)
    }

    fun stop() {
        if (!isListening) return
        sensorManager?.unregisterListener(this)
        isListening = false
    }

    fun evaluateCurrentProfile(forceNotify: Boolean = false): EyeGuardStage {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val stage = EyeGuardStage.getCurrentStage(hour)
        // Bug fix: this used to fire onStageChanged() on every single sensor
        // tick (light sensor updates can arrive several times per second),
        // which caused the ViewModel to rewrite settings + restart the
        // overlay service constantly, draining battery and flickering the
        // notification. Now we only notify when the stage actually changes,
        // or periodically so the lux reading in the UI still stays fresh.
        val now = System.currentTimeMillis()
        val stageChanged = stage != lastReportedStage
        val luxStale = now - lastLuxReportTime >= LUX_REPORT_INTERVAL_MS
        if (forceNotify || stageChanged || luxStale) {
            lastReportedStage = stage
            lastLuxReportTime = now
            onStageChanged(stage, currentLux)
        }
        return stage
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_LIGHT) return
        currentLux = event.values[0]
        evaluateCurrentProfile()
    }

    private companion object {
        // Minimum gap between lux-only updates when the stage hasn't changed.
        const val LUX_REPORT_INTERVAL_MS = 5_000L
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
