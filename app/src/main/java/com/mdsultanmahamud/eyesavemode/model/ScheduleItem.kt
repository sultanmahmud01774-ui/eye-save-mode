package com.mdsultanmahamud.eyesavemode.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Locale

@Entity(tableName = "schedules")
data class ScheduleItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val dimmingPercent: Int = 60,
    val filterPresetId: String = "warm_night",
    val filterR: Int = 255,
    val filterG: Int = 147,
    val filterB: Int = 41,
    val filterIntensity: Int = 50,
    val daysOfWeek: String = "1,2,3,4,5,6,7", // 1=Sun, 2=Mon ... 7=Sat
    val isEnabled: Boolean = true,
    val isSmartPreset: Boolean = false
) {
    fun formatStartTime(): String {
        return formatTime(startHour, startMinute)
    }

    fun formatEndTime(): String {
        return formatTime(endHour, endMinute)
    }

    private fun formatTime(hour: Int, minute: Int): String {
        val amPm = if (hour < 12) "AM" else "PM"
        val displayHour = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        return String.format(Locale.getDefault(), "%02d:%02d %s", displayHour, minute, amPm)
    }

    /** Parses [daysOfWeek] ("1,2,3,4,5,6,7", 1=Sun...7=Sat, matching java.util.Calendar) into a set of ints. */
    fun activeDaySet(): Set<Int> {
        return daysOfWeek.split(",")
            .mapNotNull { it.trim().toIntOrNull() }
            .filter { it in 1..7 }
            .toSet()
            .ifEmpty { setOf(1, 2, 3, 4, 5, 6, 7) }
    }

    /** True if this schedule is configured to run on [calendarDayOfWeek] (java.util.Calendar.DAY_OF_WEEK, 1=Sun...7=Sat). */
    fun isActiveOnDay(calendarDayOfWeek: Int): Boolean {
        return calendarDayOfWeek in activeDaySet()
    }
}
