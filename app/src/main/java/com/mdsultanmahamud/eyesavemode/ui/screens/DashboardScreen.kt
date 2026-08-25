package com.mdsultanmahamud.eyesavemode.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mdsultanmahamud.eyesavemode.model.EyeGuardStage
import com.mdsultanmahamud.eyesavemode.model.FilterPreset
import com.mdsultanmahamud.eyesavemode.ui.components.DimmerSliderCard
import com.mdsultanmahamud.eyesavemode.ui.components.FilterPresetSelector
import com.mdsultanmahamud.eyesavemode.ui.components.MainPowerCard
import com.mdsultanmahamud.eyesavemode.ui.components.PermissionBanner
import com.mdsultanmahamud.eyesavemode.ui.components.PremiumDialog
import com.mdsultanmahamud.eyesavemode.ui.theme.EyeAmberPrimary
import com.mdsultanmahamud.eyesavemode.ui.theme.EyeEmeraldTertiary
import com.mdsultanmahamud.eyesavemode.ui.theme.EyeGoldSecondary
import com.mdsultanmahamud.eyesavemode.viewmodel.EyeSaveViewModel

@Composable
fun DashboardScreen(
    viewModel: EyeSaveViewModel,
    onNavigateToColorStudio: () -> Unit,
    onNavigateToSchedules: () -> Unit,
    onNavigateToTools: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAbout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val customPresets by viewModel.customPresets.collectAsStateWithLifecycle()
    val hasOverlayPermission by viewModel.hasOverlayPermission.collectAsStateWithLifecycle()
    val hasNotificationPermission by viewModel.hasNotificationPermission.collectAsStateWithLifecycle()
    val currentStage by viewModel.currentEyeGuardStage.collectAsStateWithLifecycle()
    val currentLux by viewModel.currentLightLux.collectAsStateWithLifecycle()

    var showPremiumDialog by remember { mutableStateOf(false) }

    val activePreset = FilterPreset.findPreset(settings.activePresetId, customPresets)
    val activeColor = if (settings.activePresetId == "custom") {
        Color(settings.customR, settings.customG, settings.customB)
    } else {
        Color(activePreset.red, activePreset.green, activePreset.blue)
    }
    val activePresetName = if (settings.activePresetId == "custom") "Custom RGB" else activePreset.name

    if (showPremiumDialog) {
        PremiumDialog(
            isUnlocked = settings.isPremiumUnlocked,
            onDismiss = { showPremiumDialog = false },
            onToggleTestUnlock = { viewModel.setPremiumUnlocked(it) }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Header Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "EYE SAVE MODE",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        if (settings.isPremiumUnlocked) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = EyeAmberPrimary.copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, EyeAmberPrimary)
                            ) {
                                Text(
                                    text = "PRO",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = EyeAmberPrimary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = "Advanced Screen Comfort & Filter Suite",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row {
                    IconButton(
                        onClick = { showPremiumDialog = true },
                        modifier = Modifier.testTag("premium_header_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Premium",
                            tint = EyeAmberPrimary
                        )
                    }
                    IconButton(
                        onClick = onNavigateToAbout,
                        modifier = Modifier.testTag("about_header_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "About",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Permission Diagnostic Banner
        item {
            PermissionBanner(
                hasOverlayPermission = hasOverlayPermission,
                hasNotificationPermission = hasNotificationPermission,
                onPermissionRequested = { viewModel.refreshPermissions() }
            )
        }

        // Main Power Card
        item {
            MainPowerCard(
                isEnabled = settings.isEnabled,
                dimmingPercent = settings.dimmingPercent,
                filterName = activePresetName,
                filterColor = activeColor,
                smartEyeGuardActive = settings.smartEyeGuardEnabled,
                onTogglePower = { viewModel.togglePower(it) }
            )
        }

        // Dimming Slider Card
        item {
            DimmerSliderCard(
                dimmingPercent = settings.dimmingPercent,
                onDimmingChange = { viewModel.setDimmingPercent(it) }
            )
        }

        // Color Filter Selector
        item {
            FilterPresetSelector(
                activePresetId = settings.activePresetId,
                customPresets = customPresets,
                onSelectPreset = { viewModel.setActivePreset(it) },
                onOpenColorStudio = onNavigateToColorStudio
            )
        }

        // Smart Eye Guard Circadian Widget
        item {
            SmartEyeGuardCard(
                stage = currentStage,
                isEnabled = settings.smartEyeGuardEnabled,
                lux = currentLux,
                onToggle = { viewModel.toggleSmartEyeGuard(it) }
            )
        }

        // Quick Navigation Grid
        item {
            Text(
                text = "FEATURE SUITE",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickNavCard(
                    title = "SULTAN TOOLS",
                    subtitle = "All 9 Tools",
                    icon = Icons.Default.AutoAwesome,
                    color = EyeAmberPrimary,
                    onClick = onNavigateToTools,
                    modifier = Modifier.weight(1f),
                    testTag = "nav_sultan_tools"
                )
                QuickNavCard(
                    title = "Smart Schedule",
                    subtitle = "Day/Night Timer",
                    icon = Icons.Default.Schedule,
                    color = EyeEmeraldTertiary,
                    onClick = onNavigateToSchedules,
                    modifier = Modifier.weight(1f),
                    testTag = "nav_schedules"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickNavCard(
                    title = "RGB Color Studio",
                    subtitle = "Custom Hex/Matrix",
                    icon = Icons.Default.Palette,
                    color = EyeGoldSecondary,
                    onClick = onNavigateToColorStudio,
                    modifier = Modifier.weight(1f),
                    testTag = "nav_color_studio"
                )
                QuickNavCard(
                    title = "Settings",
                    subtitle = "Boot & Preferences",
                    icon = Icons.Default.Settings,
                    color = MaterialTheme.colorScheme.primary,
                    onClick = onNavigateToSettings,
                    modifier = Modifier.weight(1f),
                    testTag = "nav_settings"
                )
            }
        }
    }
}

@Composable
private fun SmartEyeGuardCard(
    stage: EyeGuardStage,
    isEnabled: Boolean,
    lux: Float,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("smart_eye_guard_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(EyeAmberPrimary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Smart Eye Guard",
                            tint = EyeAmberPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "SULTAN SMART EYE GUARD",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Time-based circadian comfort engine",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Switch(
                    checked = isEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = EyeAmberPrimary
                    ),
                    modifier = Modifier.testTag("smart_guard_switch")
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "CURRENT PHASE: ${stage.title}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = EyeAmberPrimary
                        )
                        Text(
                            text = stage.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = EyeAmberPrimary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "${stage.dimmingPercent}% Dim",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = EyeAmberPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickNavCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
