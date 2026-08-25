package com.mdsultanmahamud.eyesavemode.model

enum class EyeGuardStage(
    val title: String,
    val timeRange: String,
    val description: String,
    val dimmingPercent: Int,
    val r: Int,
    val g: Int,
    val b: Int,
    val intensity: Int
) {
    DAY(
        title = "DAY MODE",
        timeRange = "08:00 AM – 06:00 PM",
        description = "Minimal subtle filter to ease digital eye fatigue while maintaining true daytime color accuracy.",
        dimmingPercent = 10,
        r = 255,
        g = 220,
        b = 150,
        intensity = 20
    ),
    EVENING(
        title = "EVENING TRANSITION",
        timeRange = "06:00 PM – 09:00 PM",
        description = "Warm amber tone gradually reducing blue light to support natural melatonin synthesis.",
        dimmingPercent = 40,
        r = 255,
        g = 160,
        b = 50,
        intensity = 45
    ),
    NIGHT(
        title = "NIGHT COMFORT",
        timeRange = "09:00 PM – 12:00 AM",
        description = "Deep warm candlelight filter with enhanced screen dimming for bedtime reading.",
        dimmingPercent = 65,
        r = 255,
        g = 130,
        b = 20,
        intensity = 65
    ),
    DEEP_NIGHT(
        title = "DEEP NIGHT (BEDTIME)",
        timeRange = "12:00 AM – 08:00 AM",
        description = "Ultra-dim dark ruby-amber shield blocking 99% of blue wavelengths to safeguard circadian sleep rhythm.",
        dimmingPercent = 80,
        r = 240,
        g = 80,
        b = 10,
        intensity = 80
    );

    companion object {
        fun getCurrentStage(hour: Int): EyeGuardStage {
            return when (hour) {
                in 8..17 -> DAY
                in 18..20 -> EVENING
                in 21..23 -> NIGHT
                else -> DEEP_NIGHT
            }
        }
    }
}
