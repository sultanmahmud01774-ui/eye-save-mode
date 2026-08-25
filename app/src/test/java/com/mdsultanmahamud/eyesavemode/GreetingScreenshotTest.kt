package com.mdsultanmahamud.eyesavemode

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.mdsultanmahamud.eyesavemode.ui.components.MainPowerCard
import com.mdsultanmahamud.eyesavemode.ui.theme.EyeSaveTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [34])
class GreetingScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun main_power_card_screenshot() {
        composeTestRule.setContent {
            EyeSaveTheme(themeMode = "dark") {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    MainPowerCard(
                        isEnabled = true,
                        dimmingPercent = 45,
                        filterName = "Warm Amber",
                        filterColor = Color(255, 147, 41),
                        smartEyeGuardActive = true,
                        onTogglePower = {}
                    )
                }
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/main_power_card.png")
    }
}
