package com.mdsultanmahamud.eyesavemode.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_presets")
data class FilterPreset(
    @PrimaryKey val id: String,
    val name: String,
    val red: Int,
    val green: Int,
    val blue: Int,
    val defaultIntensity: Int = 50,
    val isBuiltIn: Boolean = false,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        val BUILT_IN_PRESETS = listOf(
            FilterPreset(
                id = "warm_night",
                name = "Warm Night",
                red = 255,
                green = 147,
                blue = 41,
                defaultIntensity = 55,
                isBuiltIn = true,
                description = "2700K Warm candle glow for pleasant evening reading"
            ),
            FilterPreset(
                id = "night_black",
                name = "Night Black",
                red = 0,
                green = 0,
                blue = 0,
                defaultIntensity = 60,
                isBuiltIn = true,
                description = "Pure black overlay for ultimate OLED dimming"
            ),
            FilterPreset(
                id = "yellow_candle",
                name = "Candlelight Yellow",
                red = 255,
                green = 200,
                blue = 50,
                defaultIntensity = 50,
                isBuiltIn = true,
                description = "Soft golden yellow to block harsh blue spectrum"
            ),
            FilterPreset(
                id = "soothing_green",
                name = "Soothing Green",
                red = 34,
                green = 139,
                blue = 34,
                defaultIntensity = 45,
                isBuiltIn = true,
                description = "Relaxing nature green for eye fatigue recovery"
            ),
            FilterPreset(
                id = "deep_red",
                name = "Deep Red",
                red = 235,
                green = 55,
                blue = 55,
                defaultIntensity = 65,
                isBuiltIn = true,
                description = "Astronomical red preserving natural night vision"
            ),
            FilterPreset(
                id = "reddish_brown",
                name = "Reddish Brown",
                red = 160,
                green = 82,
                blue = 45,
                defaultIntensity = 55,
                isBuiltIn = true,
                description = "Rich amber-brown tone for book reading in bed"
            ),
            FilterPreset(
                id = "sunset_orange",
                name = "Sunset Orange",
                red = 255,
                green = 115,
                blue = 0,
                defaultIntensity = 55,
                isBuiltIn = true,
                description = "Melatonin-safe orange twilight tone"
            ),
            FilterPreset(
                id = "forest_emerald",
                name = "Forest Emerald",
                red = 46,
                green = 125,
                blue = 50,
                defaultIntensity = 40,
                isBuiltIn = true,
                description = "High-contrast soothing deep emerald tone"
            )
        )

        fun findPreset(id: String, customPresets: List<FilterPreset> = emptyList()): FilterPreset {
            return BUILT_IN_PRESETS.firstOrNull { it.id == id }
                ?: customPresets.firstOrNull { it.id == id }
                ?: BUILT_IN_PRESETS.first()
        }
    }
}
