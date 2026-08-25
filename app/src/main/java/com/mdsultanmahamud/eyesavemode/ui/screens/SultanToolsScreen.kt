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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BrightnessLow
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitScreen
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mdsultanmahamud.eyesavemode.model.EyeGuardStage
import com.mdsultanmahamud.eyesavemode.ui.components.openAppNotificationSettings
import com.mdsultanmahamud.eyesavemode.ui.components.openBatteryOptimizationSettings
import com.mdsultanmahamud.eyesavemode.ui.components.openOverlayPermissionSettings
import com.mdsultanmahamud.eyesavemode.ui.theme.EyeAmberPrimary
import com.mdsultanmahamud.eyesavemode.ui.theme.EyeEmeraldTertiary
import com.mdsultanmahamud.eyesavemode.ui.theme.EyeErrorRed
import com.mdsultanmahamud.eyesavemode.ui.theme.EyeGoldSecondary
import com.mdsultanmahamud.eyesavemode.ui.theme.EyeSuccessGreen
import com.mdsultanmahamud.eyesavemode.ui.theme.EyeWarningYellow
import com.mdsultanmahamud.eyesavemode.util.ScreenDiagnosticHelper
import com.mdsultanmahamud.eyesavemode.viewmodel.EyeSaveViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SultanToolsScreen(
    viewModel: EyeSaveViewModel,
    onNavigateToColorStudio: () -> Unit,
    onNavigateToSchedules: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val hasOverlayPermission by viewModel.hasOverlayPermission.collectAsStateWithLifecycle()
    val hasNotificationPermission by viewModel.hasNotificationPermission.collectAsStateWithLifecycle()
    val currentStage by viewModel.currentEyeGuardStage.collectAsStateWithLifecycle()
    val currentLux by viewModel.currentLightLux.collectAsStateWithLifecycle()

    var showSafeResetDialog by remember { mutableStateOf(false) }

    if (showSafeResetDialog) {
        AlertDialog(
            onDismissRequest = { showSafeResetDialog = false },
            title = { Text("Perform SULTAN Safe Reset?") },
            text = {
                Text(
                    text = "This will safely stop the overlay service, clear corrupted cache, reset dimming and color matrices, and restore factory defaults.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.safeResetAll()
                        showSafeResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EyeErrorRed),
                    modifier = Modifier.testTag("confirm_safe_reset_btn")
                ) {
                    Text("Confirm Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSafeResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = EyeAmberPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SULTAN TOOLS",
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = EyeAmberPrimary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("back_btn")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .testTag("sultan_tools_screen"),
            contentPadding = PaddingValues(top = 8.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "SULTAN SIGNATURE COMFORT SUITE",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
            }

            // TOOL 1: SULTAN SMART EYE GUARD
            item {
                ToolCard(
                    toolNumber = "TOOL 1",
                    title = "SULTAN SMART EYE GUARD",
                    subtitle = "Circadian time-of-day automatic eye guard engine",
                    icon = Icons.Default.Shield,
                    accentColor = EyeAmberPrimary
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Automated Transitions",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Transitions between Day, Evening, Night, Deep Night",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = settings.smartEyeGuardEnabled,
                            onCheckedChange = { viewModel.toggleSmartEyeGuard(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = EyeAmberPrimary
                            ),
                            modifier = Modifier.testTag("smart_guard_tool_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Stages Overview Grid
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        EyeGuardStage.entries.forEach { stage ->
                            val isCurrent = currentStage == stage
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isCurrent) EyeAmberPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                                border = if (isCurrent) androidx.compose.foundation.BorderStroke(1.dp, EyeAmberPrimary) else null,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = stage.title,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isCurrent) EyeAmberPrimary else MaterialTheme.colorScheme.onSurface
                                            )
                                            if (isCurrent) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "● ACTIVE NOW",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = EyeAmberPrimary
                                                )
                                            }
                                        }
                                        Text(
                                            text = stage.timeRange,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = "${stage.dimmingPercent}% Dim",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = EyeAmberPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // TOOL 2: SULTAN SCREEN DIMMER
            item {
                ToolCard(
                    toolNumber = "TOOL 2",
                    title = "SULTAN SCREEN DIMMER",
                    subtitle = "Ultra-fine hardware & software dimming engine (0-90%)",
                    icon = Icons.Default.BrightnessLow,
                    accentColor = EyeGoldSecondary
                ) {
                    Text(
                        text = "Current Level: ${settings.dimmingPercent}%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Slider(
                        value = settings.dimmingPercent.toFloat(),
                        onValueChange = { viewModel.setDimmingPercent(it.toInt()) },
                        valueRange = 0f..90f,
                        colors = SliderDefaults.colors(
                            thumbColor = EyeGoldSecondary,
                            activeTrackColor = EyeGoldSecondary
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("sultan_dimmer_slider")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.setDimmingPercent(35) },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Reading 35%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Button(
                            onClick = { viewModel.setDimmingPercent(70) },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cinema 70%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Button(
                            onClick = { viewModel.setDimmingPercent(85) },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("OLED 85%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }

            // TOOL 3: SULTAN COLOR STUDIO SHORTCUT
            item {
                ToolCard(
                    toolNumber = "TOOL 3",
                    title = "SULTAN COLOR STUDIO",
                    subtitle = "Master RGB Filter Matrix & Hex Generator",
                    icon = Icons.Default.Palette,
                    accentColor = EyeAmberPrimary
                ) {
                    Text(
                        text = "Customize independent red, green, blue color channels and filter opacity.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onNavigateToColorStudio,
                        colors = ButtonDefaults.buttonColors(containerColor = EyeAmberPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("launch_color_studio_tool")
                    ) {
                        Text("Open SULTAN Color Studio", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // TOOL 4: SULTAN SMART SCHEDULE SHORTCUT
            item {
                ToolCard(
                    toolNumber = "TOOL 4",
                    title = "SULTAN SMART SCHEDULE",
                    subtitle = "Automated Day/Night timer & wake-up routines",
                    icon = Icons.Default.Schedule,
                    accentColor = EyeEmeraldTertiary
                ) {
                    Text(
                        text = "Manage recurring schedules with custom start/end times and preset filters.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onNavigateToSchedules,
                        colors = ButtonDefaults.buttonColors(containerColor = EyeEmeraldTertiary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("launch_schedule_tool")
                    ) {
                        Text("Manage Smart Schedules", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // TOOL 5: SULTAN PERMISSION FIXER
            item {
                val diagnostics = remember(hasOverlayPermission, hasNotificationPermission) {
                    ScreenDiagnosticHelper.runDiagnostics(context)
                }

                ToolCard(
                    toolNumber = "TOOL 5",
                    title = "SULTAN PERMISSION FIXER",
                    subtitle = "System diagnostic & permission integrity helper",
                    icon = Icons.Default.Security,
                    accentColor = if (hasOverlayPermission && hasNotificationPermission) EyeSuccessGreen else EyeWarningYellow
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        PermissionStatusRow(
                            name = "Display Over Other Apps (Overlay)",
                            isGranted = hasOverlayPermission,
                            onFix = { openOverlayPermissionSettings(context) }
                        )
                        PermissionStatusRow(
                            name = "Notification Control Access",
                            isGranted = hasNotificationPermission,
                            onFix = { openAppNotificationSettings(context) }
                        )
                        PermissionStatusRow(
                            name = "Battery Optimization Exemption",
                            isGranted = diagnostics.batteryOptimizationIgnored,
                            onFix = { openBatteryOptimizationSettings(context) }
                        )
                    }
                }
            }

            // TOOL 6: SULTAN SAFE RESET
            item {
                ToolCard(
                    toolNumber = "TOOL 6",
                    title = "SULTAN SAFE RESET",
                    subtitle = "Safely restore filters, dimming, and service state",
                    icon = Icons.Default.Restore,
                    accentColor = EyeErrorRed
                ) {
                    Text(
                        text = "Safely cleans up any stalled overlays and resets all settings to factory comfort standards.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { showSafeResetDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = EyeErrorRed),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("launch_safe_reset_tool")
                    ) {
                        Text("Run SULTAN Safe Reset", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // TOOL 7: FIX HALF SCREEN ISSUE
            item {
                ToolCard(
                    toolNumber = "TOOL 7",
                    title = "FIX HALF SCREEN ISSUE",
                    subtitle = "Recalibrate screen aspect ratio & camera notch cutout",
                    icon = Icons.Default.FitScreen,
                    accentColor = EyeEmeraldTertiary
                ) {
                    Text(
                        text = "If your overlay does not cover the full display or status bar, tap below to re-bind full-screen window metrics.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { viewModel.fixHalfScreenIssue() },
                        colors = ButtonDefaults.buttonColors(containerColor = EyeEmeraldTertiary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("fix_half_screen_btn")
                    ) {
                        Text("Fix & Rebind Full Screen Overlay", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // TOOL 8: SHAKE TO ACTION
            item {
                ToolCard(
                    toolNumber = "TOOL 8",
                    title = "SHAKE TO TOGGLE",
                    subtitle = "Accelerometer motion gesture shortcut",
                    icon = Icons.Default.Vibration,
                    accentColor = EyeAmberPrimary
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Shake Motion Detection",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Shake device to quickly toggle Eye Save Mode ON/OFF",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = settings.shakeActionEnabled,
                            onCheckedChange = { viewModel.toggleShakeAction(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = EyeAmberPrimary
                            ),
                            modifier = Modifier.testTag("shake_switch")
                        )
                    }

                    if (settings.shakeActionEnabled) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Shake Sensitivity",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            val sensitivityLabel = when {
                                settings.shakeSensitivity <= 11f -> "High"
                                settings.shakeSensitivity <= 15f -> "Medium"
                                else -> "Low"
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = EyeAmberPrimary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = sensitivityLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = EyeAmberPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Slider(
                            value = settings.shakeSensitivity,
                            onValueChange = { viewModel.toggleShakeAction(true, it) },
                            valueRange = 9f..18f,
                            colors = SliderDefaults.colors(
                                thumbColor = EyeAmberPrimary,
                                activeTrackColor = EyeAmberPrimary,
                                inactiveTrackColor = MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("shake_sensitivity_slider")
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Sensitive (light shake)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Firm (hard shake)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // TOOL 9: RELAX EYES REMINDER
            item {
                ToolCard(
                    toolNumber = "TOOL 9",
                    title = "RELAX EYES REMINDER",
                    subtitle = "20-20-20 rule timer for ocular strain relief",
                    icon = Icons.Default.Alarm,
                    accentColor = EyeGoldSecondary
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Eye Rest Interval",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Remind me every ${settings.relaxReminderIntervalMinutes} minutes",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = settings.relaxReminderEnabled,
                            onCheckedChange = { viewModel.toggleRelaxReminder(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = EyeGoldSecondary
                            ),
                            modifier = Modifier.testTag("relax_reminder_switch")
                        )
                    }

                    if (settings.relaxReminderEnabled) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(15, 20, 30, 45, 60).forEach { mins ->
                                val isSelected = settings.relaxReminderIntervalMinutes == mins
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) EyeGoldSecondary else MaterialTheme.colorScheme.surface,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { viewModel.toggleRelaxReminder(true, mins) }
                                ) {
                                    Text(
                                        text = "${mins}m",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(vertical = 6.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ToolCard(
    toolNumber: String,
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = accentColor.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = toolNumber,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = accentColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            content()
        }
    }
}

@Composable
private fun PermissionStatusRow(
    name: String,
    isGranted: Boolean,
    onFix: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (isGranted) EyeSuccessGreen else EyeWarningYellow,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (!isGranted) {
                Button(
                    onClick = onFix,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EyeWarningYellow),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Fix",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = EyeSuccessGreen.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "READY",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = EyeSuccessGreen,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
