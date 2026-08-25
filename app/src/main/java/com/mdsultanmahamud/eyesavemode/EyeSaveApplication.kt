package com.mdsultanmahamud.eyesavemode

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.mdsultanmahamud.eyesavemode.data.AppDatabase
import com.mdsultanmahamud.eyesavemode.data.SettingsRepository

class EyeSaveApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
    val settingsRepository: SettingsRepository by lazy { SettingsRepository(this) }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Foreground Service Channel
            val serviceChannel = NotificationChannel(
                CHANNEL_SERVICE_ID,
                "Eye Save Mode Status",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Displays persistent status and controls when Eye Save Mode is active"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(serviceChannel)

            // Relax Eyes Reminder Channel
            val reminderChannel = NotificationChannel(
                CHANNEL_REMINDER_ID,
                "Eye Rest Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Periodic reminders to relax and rest your eyes (20-20-20 rule)"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(reminderChannel)
        }
    }

    companion object {
        const val CHANNEL_SERVICE_ID = "eye_save_service_channel"
        const val CHANNEL_REMINDER_ID = "eye_save_reminder_channel"
        const val NOTIFICATION_SERVICE_ID = 1001
        const val NOTIFICATION_REMINDER_ID = 1002
    }
}
