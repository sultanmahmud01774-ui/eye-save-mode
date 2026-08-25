package com.mdsultanmahamud.eyesavemode

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.mdsultanmahamud.eyesavemode.data.SettingsRepository
import com.mdsultanmahamud.eyesavemode.model.EyeGuardStage
import com.mdsultanmahamud.eyesavemode.model.FilterPreset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context matches app identity`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("EYE SAVE MODE", appName)
    }

    @Test
    fun `verify built in filter presets`() {
        val presets = FilterPreset.BUILT_IN_PRESETS
        assertTrue(presets.isNotEmpty())
        val warmNight = presets.firstOrNull { it.id == "warm_night" }
        assertNotNull(warmNight)
        assertEquals("Warm Night", warmNight?.name)
    }

    @Test
    fun `verify eye guard stage transitions`() {
        assertEquals(EyeGuardStage.DAY, EyeGuardStage.getCurrentStage(12))
        assertEquals(EyeGuardStage.EVENING, EyeGuardStage.getCurrentStage(19))
        assertEquals(EyeGuardStage.NIGHT, EyeGuardStage.getCurrentStage(22))
        assertEquals(EyeGuardStage.DEEP_NIGHT, EyeGuardStage.getCurrentStage(2))
    }

    @Test
    fun `verify settings repository json export and import`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = SettingsRepository(context)
        repo.updateSettings { it.copy(dimmingPercent = 65, activePresetId = "candlelight") }

        val json = repo.exportSettingsJson()
        assertTrue(json.contains("\"dimmingPercent\": 65"))
        assertTrue(json.contains("\"activePresetId\": \"candlelight\""))

        // Modify and import back
        repo.updateSettings { it.copy(dimmingPercent = 10) }
        val success = repo.importSettingsJson(json)
        assertTrue(success)
        assertEquals(65, repo.settings.value.dimmingPercent)
        assertEquals("candlelight", repo.settings.value.activePresetId)
    }
}
