package com.mdsultanmahamud.eyesavemode.util

import android.content.Context
import com.mdsultanmahamud.eyesavemode.data.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Clean architectural manager for premium feature entitlements.
 * Provides verifiable lock indicators and testing bypass mechanism without fake payment loops.
 */
class PremiumManager(private val repository: SettingsRepository) {

    private val _isPremium = MutableStateFlow(repository.settings.value.isPremiumUnlocked)
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    fun setPremiumUnlocked(unlocked: Boolean) {
        repository.updateSettings { it.copy(isPremiumUnlocked = unlocked) }
        _isPremium.value = unlocked
    }

    companion object {
        val PREMIUM_FEATURES = listOf(
            "SULTAN Smart Eye Guard automated circadian transitions",
            "Unlimited custom RGB color presets in Sultan Color Studio",
            "Multiple active scheduled profiles with custom day filters",
            "Ultra-fine OLED sub-zero dimming down to 90%",
            "Shake to quickly toggle Eye Save Mode",
            "Priority background engine optimization and ad-free experience"
        )
    }
}
