package com.mdsultanmahamud.eyesavemode.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import com.mdsultanmahamud.eyesavemode.data.AppDatabase
import com.mdsultanmahamud.eyesavemode.data.SettingsRepository
import com.mdsultanmahamud.eyesavemode.model.FilterPreset
import com.mdsultanmahamud.eyesavemode.model.ScheduleItem
import com.mdsultanmahamud.eyesavemode.service.RelaxReminderManager
import com.mdsultanmahamud.eyesavemode.service.ScreenFilterService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class ScheduleReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_SCHEDULE_TRIGGER_START -> {
                val scheduleId = intent.getLongExtra(EXTRA_SCHEDULE_ID, -1L)
                if (scheduleId != -1L) {
                    handleScheduleStart(context, scheduleId)
                }
            }
            ACTION_SCHEDULE_TRIGGER_END -> {
                val scheduleId = intent.getLongExtra(EXTRA_SCHEDULE_ID, -1L)
                if (scheduleId != -1L) {
                    handleScheduleEnd(context, scheduleId)
                }
            }
            ACTION_RELAX_REMINDER -> {
                val intervalMinutes = intent.getIntExtra(EXTRA_INTERVAL_MINUTES, 20)
                RelaxReminderManager.showReminderNotification(context)
                // Reschedule next interval
                val settings = SettingsRepository(context).settings.value
                if (settings.relaxReminderEnabled) {
                    RelaxReminderManager.scheduleReminder(context, intervalMinutes)
                }
            }
        }
    }

    private fun handleScheduleStart(context: Context, scheduleId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getDatabase(context)
            val schedule = db.scheduleDao().getScheduleById(scheduleId) ?: return@launch
            if (!schedule.isEnabled) return@launch

            // Bug fix: daysOfWeek was defined on the model but never checked
            // anywhere, so schedules always fired every day. Skip applying
            // the filter (but still reschedule below) if today isn't selected.
            val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
            if (!schedule.isActiveOnDay(today)) {
                scheduleAlarm(context, schedule)
                return@launch
            }

            val repo = SettingsRepository(context)
            repo.updateSettings { current ->
                current.copy(
                    isEnabled = true,
                    dimmingPercent = schedule.dimmingPercent,
                    activePresetId = schedule.filterPresetId,
                    customR = schedule.filterR,
                    customG = schedule.filterG,
                    customB = schedule.filterB,
                    filterIntensity = schedule.filterIntensity
                )
            }

            if (Settings.canDrawOverlays(context)) {
                val preset = FilterPreset.findPreset(schedule.filterPresetId)
                ScreenFilterService.startOrUpdate(
                    context = context,
                    dimmingPercent = schedule.dimmingPercent,
                    r = schedule.filterR,
                    g = schedule.filterG,
                    b = schedule.filterB,
                    intensity = schedule.filterIntensity,
                    presetName = schedule.title.ifEmpty { preset.name },
                    pauseWhenScreenOff = repo.settings.value.pauseWhenScreenOff
                )
            }

            // Reschedule for next day
            scheduleAlarm(context, schedule)
        }
    }

    private fun handleScheduleEnd(context: Context, scheduleId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getDatabase(context)
            val schedule = db.scheduleDao().getScheduleById(scheduleId) ?: return@launch
            if (!schedule.isEnabled) return@launch

            val repo = SettingsRepository(context)
            repo.updateSettings { current ->
                current.copy(isEnabled = false)
            }

            ScreenFilterService.stop(context)

            // Reschedule for next day
            scheduleAlarm(context, schedule)
        }
    }

    companion object {
        const val ACTION_SCHEDULE_TRIGGER_START = "com.mdsultanmahamud.eyesavemode.action.SCHEDULE_TRIGGER_START"
        const val ACTION_SCHEDULE_TRIGGER_END = "com.mdsultanmahamud.eyesavemode.action.SCHEDULE_TRIGGER_END"
        const val ACTION_RELAX_REMINDER = "com.mdsultanmahamud.eyesavemode.action.RELAX_REMINDER"

        const val EXTRA_SCHEDULE_ID = "extra_schedule_id"
        const val EXTRA_INTERVAL_MINUTES = "extra_interval_minutes"
        const val REQUEST_CODE_RELAX_REMINDER = 9001

        fun scheduleAlarm(context: Context, schedule: ScheduleItem) {
            if (!schedule.isEnabled) return
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val now = Calendar.getInstance()
            val activeDays = schedule.activeDaySet()
            val startCalendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, schedule.startHour.coerceIn(0, 23))
                set(Calendar.MINUTE, schedule.startMinute.coerceIn(0, 59))
                set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                var guard = 0
                while ((before(now) || get(Calendar.DAY_OF_WEEK) !in activeDays) && guard < 8) { add(Calendar.DAY_OF_YEAR, 1); guard++ }
                if (get(Calendar.DAY_OF_WEEK) !in activeDays) add(Calendar.DAY_OF_YEAR, 1)
            }
            val endCalendar = (startCalendar.clone() as Calendar).apply {
                set(Calendar.HOUR_OF_DAY, schedule.endHour.coerceIn(0, 23))
                set(Calendar.MINUTE, schedule.endMinute.coerceIn(0, 59))
                set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                if (!after(startCalendar)) add(Calendar.DAY_OF_YEAR, 1)
            }
            val startIntent = Intent(context, ScheduleReceiver::class.java).apply { action = ACTION_SCHEDULE_TRIGGER_START; putExtra(EXTRA_SCHEDULE_ID, schedule.id) }
            val startPending = PendingIntent.getBroadcast(context, (schedule.id * 10).toInt(), startIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            val endIntent = Intent(context, ScheduleReceiver::class.java).apply { action = ACTION_SCHEDULE_TRIGGER_END; putExtra(EXTRA_SCHEDULE_ID, schedule.id) }
            val endPending = PendingIntent.getBroadcast(context, (schedule.id * 10 + 1).toInt(), endIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            try {
                val canExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
                if (canExact) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, startCalendar.timeInMillis, startPending)
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, endCalendar.timeInMillis, endPending)
                    } else {
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, startCalendar.timeInMillis, startPending)
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, endCalendar.timeInMillis, endPending)
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, startCalendar.timeInMillis, startPending)
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, endCalendar.timeInMillis, endPending)
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, startCalendar.timeInMillis, startPending)
                    alarmManager.set(AlarmManager.RTC_WAKEUP, endCalendar.timeInMillis, endPending)
                }
            } catch (_: SecurityException) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, startCalendar.timeInMillis, startPending)
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, endCalendar.timeInMillis, endPending)
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, startCalendar.timeInMillis, startPending)
                    alarmManager.set(AlarmManager.RTC_WAKEUP, endCalendar.timeInMillis, endPending)
                }
            }
        }

        fun cancelScheduleAlarm(context: Context, scheduleId: Long) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val startIntent = Intent(context, ScheduleReceiver::class.java).apply {
                action = ACTION_SCHEDULE_TRIGGER_START
            }
            val startPending = PendingIntent.getBroadcast(
                context,
                (scheduleId * 10).toInt(),
                startIntent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (startPending != null) {
                alarmManager.cancel(startPending)
                startPending.cancel()
            }

            val endIntent = Intent(context, ScheduleReceiver::class.java).apply {
                action = ACTION_SCHEDULE_TRIGGER_END
            }
            val endPending = PendingIntent.getBroadcast(
                context,
                (scheduleId * 10 + 1).toInt(),
                endIntent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (endPending != null) {
                alarmManager.cancel(endPending)
                endPending.cancel()
            }
        }
    }
}
