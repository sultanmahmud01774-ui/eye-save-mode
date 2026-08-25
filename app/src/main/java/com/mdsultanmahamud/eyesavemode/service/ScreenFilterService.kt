package com.mdsultanmahamud.eyesavemode.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.mdsultanmahamud.eyesavemode.EyeSaveApplication
import com.mdsultanmahamud.eyesavemode.MainActivity
import com.mdsultanmahamud.eyesavemode.R
import com.mdsultanmahamud.eyesavemode.receiver.NotificationActionReceiver

class ScreenFilterService : Service() {

    private var windowManager: WindowManager? = null
    private var overlayView: OverlayDrawView? = null
    private var isOverlayAttached = false

    private var currentDimmingPercent = 45
    private var currentFilterR = 255
    private var currentFilterG = 147
    private var currentFilterB = 41
    private var currentFilterIntensity = 50
    private var currentPresetName = "Warm Night"
    private var pauseWhenScreenOff = true
    private var isScreenOn = true

    private val screenStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_OFF -> {
                    isScreenOn = false
                    Log.d(TAG, "Screen turned OFF - pausing overlay if configured")
                    if (pauseWhenScreenOff) {
                        removeOverlayView()
                    }
                }
                Intent.ACTION_SCREEN_ON -> {
                    isScreenOn = true
                    Log.d(TAG, "Screen turned ON - restoring overlay if active")
                    if (pauseWhenScreenOff) {
                        applyOverlayView()
                    }
                }
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "ScreenFilterService onCreate")
        windowManager = getSystemService(Context.WINDOW_SERVICE) as? WindowManager

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
        }
        try {
            registerReceiver(screenStateReceiver, filter)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register screenStateReceiver", e)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Log.d(TAG, "Configuration changed (orientation / layout change) - refreshing overlay metrics")
        if (isOverlayAttached && isScreenOn) {
            applyOverlayView()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) return START_STICKY

        when (intent.action) {
            ACTION_STOP -> {
                Log.d(TAG, "ACTION_STOP received - stopping foreground service")
                removeOverlayView()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                } else {
                    @Suppress("DEPRECATION")
                    stopForeground(true)
                }
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_START, ACTION_UPDATE -> {
                currentDimmingPercent = intent.getIntExtra(EXTRA_DIMMING, currentDimmingPercent).coerceIn(0, 90)
                currentFilterR = intent.getIntExtra(EXTRA_R, currentFilterR).coerceIn(0, 255)
                currentFilterG = intent.getIntExtra(EXTRA_G, currentFilterG).coerceIn(0, 255)
                currentFilterB = intent.getIntExtra(EXTRA_B, currentFilterB).coerceIn(0, 255)
                currentFilterIntensity = intent.getIntExtra(EXTRA_INTENSITY, currentFilterIntensity).coerceIn(0, 100)
                currentPresetName = intent.getStringExtra(EXTRA_PRESET_NAME) ?: currentPresetName
                pauseWhenScreenOff = intent.getBooleanExtra(EXTRA_PAUSE_SCREEN_OFF, pauseWhenScreenOff)

                startForegroundSafe()
                applyOverlayView()
            }
            ACTION_INC_DIM -> {
                currentDimmingPercent = (currentDimmingPercent + 10).coerceAtMost(90)
                updateOverlayColors()
                startForegroundSafe()
            }
            ACTION_DEC_DIM -> {
                currentDimmingPercent = (currentDimmingPercent - 10).coerceAtLeast(0)
                updateOverlayColors()
                startForegroundSafe()
            }
        }

        return START_STICKY
    }

    private fun startForegroundSafe() {
        val notification = buildNotification()
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val fgsType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                } else {
                    0
                }
                ServiceCompat.startForeground(
                    this,
                    EyeSaveApplication.NOTIFICATION_SERVICE_ID,
                    notification,
                    fgsType
                )
            } else {
                startForeground(EyeSaveApplication.NOTIFICATION_SERVICE_ID, notification)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to startForeground", e)
        }
    }

    private fun applyOverlayView() {
        if (!Settings.canDrawOverlays(this)) {
            Log.w(TAG, "Cannot apply overlay: SYSTEM_ALERT_WINDOW permission not granted")
            return
        }

        try {
            if (overlayView == null) {
                overlayView = OverlayDrawView(this)
            }

            updateOverlayColors()

            val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY
            }

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                type,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                        WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    layoutInDisplayCutoutMode =
                        WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
                }
            }

            if (!isOverlayAttached && overlayView != null) {
                windowManager?.addView(overlayView, params)
                isOverlayAttached = true
                Log.d(TAG, "Overlay window view attached successfully")
            } else if (isOverlayAttached && overlayView != null) {
                windowManager?.updateViewLayout(overlayView, params)
                Log.d(TAG, "Overlay window view layout updated")
            }
        } catch (e: WindowManager.BadTokenException) {
            Log.e(TAG, "BadTokenException adding overlay view", e)
        } catch (e: IllegalStateException) {
            Log.e(TAG, "IllegalStateException adding overlay view", e)
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException adding overlay view", e)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error applying overlay view", e)
        }
    }

    private fun updateOverlayColors() {
        overlayView?.setOverlayProperties(
            dimmingPercent = currentDimmingPercent,
            r = currentFilterR,
            g = currentFilterG,
            b = currentFilterB,
            intensity = currentFilterIntensity
        )
    }

    private fun removeOverlayView() {
        try {
            if (isOverlayAttached && overlayView != null) {
                windowManager?.removeView(overlayView)
                isOverlayAttached = false
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun buildNotification(): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            this, 0, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Stop Action
        val stopIntent = Intent(this, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_TOGGLE_POWER
        }
        val stopPendingIntent = PendingIntent.getBroadcast(
            this, 1, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Dim + Action
        val incIntent = Intent(this, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_INCREASE_DIM
        }
        val incPendingIntent = PendingIntent.getBroadcast(
            this, 2, incIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Dim - Action
        val decIntent = Intent(this, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_DECREASE_DIM
        }
        val decPendingIntent = PendingIntent.getBroadcast(
            this, 3, decIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, EyeSaveApplication.CHANNEL_SERVICE_ID)
            .setContentTitle("EYE SAVE MODE: Active")
            .setContentText("Filter: $currentPresetName | Dimming: $currentDimmingPercent%")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(openPendingIntent)
            .setOngoing(true)
            .addAction(R.drawable.ic_launcher_foreground, "Turn OFF", stopPendingIntent)
            .addAction(R.drawable.ic_launcher_foreground, "Dim +10%", incPendingIntent)
            .addAction(R.drawable.ic_launcher_foreground, "Dim -10%", decPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        removeOverlayView()
        try {
            unregisterReceiver(screenStateReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private class OverlayDrawView(context: Context) : View(context) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private var dimmingPercent: Int = 45
        private var filterR: Int = 255
        private var filterG: Int = 147
        private var filterB: Int = 41
        private var filterIntensity: Int = 50

        fun setOverlayProperties(dimmingPercent: Int, r: Int, g: Int, b: Int, intensity: Int) {
            this.dimmingPercent = dimmingPercent.coerceIn(0, 90) // Safe clamp to avoid total screen blackout
            this.filterR = r.coerceIn(0, 255)
            this.filterG = g.coerceIn(0, 255)
            this.filterB = b.coerceIn(0, 255)
            this.filterIntensity = intensity.coerceIn(0, 100)
            invalidate()
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)

            // Draw color filter layer
            if (filterIntensity > 0) {
                val colorAlpha = (filterIntensity / 100f * 180).toInt().coerceIn(0, 240)
                val colorTint = Color.argb(colorAlpha, filterR, filterG, filterB)
                paint.color = colorTint
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
            }

            // Draw dimming layer (black overlay)
            if (dimmingPercent > 0) {
                val dimAlpha = (dimmingPercent / 100f * 230).toInt().coerceIn(0, 235)
                val dimColor = Color.argb(dimAlpha, 0, 0, 0)
                paint.color = dimColor
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
            }
        }
    }

    companion object {
        private const val TAG = "ScreenFilterService"

        const val ACTION_START = "com.mdsultanmahamud.eyesavemode.action.START_OVERLAY"
        const val ACTION_UPDATE = "com.mdsultanmahamud.eyesavemode.action.UPDATE_OVERLAY"
        const val ACTION_STOP = "com.mdsultanmahamud.eyesavemode.action.STOP_OVERLAY"
        const val ACTION_INC_DIM = "com.mdsultanmahamud.eyesavemode.action.INC_DIM"
        const val ACTION_DEC_DIM = "com.mdsultanmahamud.eyesavemode.action.DEC_DIM"

        const val EXTRA_DIMMING = "extra_dimming"
        const val EXTRA_R = "extra_r"
        const val EXTRA_G = "extra_g"
        const val EXTRA_B = "extra_b"
        const val EXTRA_INTENSITY = "extra_intensity"
        const val EXTRA_PRESET_NAME = "extra_preset_name"
        const val EXTRA_PAUSE_SCREEN_OFF = "extra_pause_screen_off"

        fun startOrUpdate(
            context: Context,
            dimmingPercent: Int,
            r: Int,
            g: Int,
            b: Int,
            intensity: Int,
            presetName: String,
            pauseWhenScreenOff: Boolean
        ) {
            val intent = Intent(context, ScreenFilterService::class.java).apply {
                action = ACTION_UPDATE
                putExtra(EXTRA_DIMMING, dimmingPercent)
                putExtra(EXTRA_R, r)
                putExtra(EXTRA_G, g)
                putExtra(EXTRA_B, b)
                putExtra(EXTRA_INTENSITY, intensity)
                putExtra(EXTRA_PRESET_NAME, presetName)
                putExtra(EXTRA_PAUSE_SCREEN_OFF, pauseWhenScreenOff)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, ScreenFilterService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}
