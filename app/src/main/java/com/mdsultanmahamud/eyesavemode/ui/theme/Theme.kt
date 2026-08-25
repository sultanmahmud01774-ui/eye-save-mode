package com.mdsultanmahamud.eyesavemode.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val EyeDarkColorScheme = darkColorScheme(
    primary = EyeAmberPrimary,
    onPrimary = Color(0xFF1A0E00),
    primaryContainer = EyeDarkSurfaceVariant,
    onPrimaryContainer = EyeAmberLight,
    secondary = EyeGoldSecondary,
    onSecondary = Color(0xFF1F1600),
    secondaryContainer = Color(0xFF2C220E),
    onSecondaryContainer = EyeGoldSecondary,
    tertiary = EyeEmeraldTertiary,
    onTertiary = Color(0xFF00201C),
    background = EyeDarkBackground,
    onBackground = Color(0xFFF1F5F9),
    surface = EyeDarkSurface,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = EyeDarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0xFF334155),
    error = EyeErrorRed,
    onError = Color.White
)

private val EyeAmoledColorScheme = darkColorScheme(
    primary = EyeAmberPrimary,
    onPrimary = Color.Black,
    primaryContainer = EyeAmoledSurface,
    onPrimaryContainer = EyeAmberLight,
    secondary = EyeGoldSecondary,
    onSecondary = Color.Black,
    tertiary = EyeEmeraldTertiary,
    onTertiary = Color.Black,
    background = EyeAmoledBlack,
    onBackground = Color(0xFFF8FAFC),
    surface = EyeAmoledSurface,
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF1A1A1A),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF262626),
    error = EyeErrorRed,
    onError = Color.White
)

private val EyeLightColorScheme = lightColorScheme(
    primary = EyeLightPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFEF3C7),
    onPrimaryContainer = Color(0xFF78350F),
    secondary = EyeLightSecondary,
    onSecondary = Color.White,
    tertiary = EyeLightTertiary,
    onTertiary = Color.White,
    background = EyeLightBackground,
    onBackground = Color(0xFF0F172A),
    surface = EyeLightSurface,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = EyeLightSurfaceVariant,
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1),
    error = EyeErrorRed,
    onError = Color.White
)

@Composable
fun EyeSaveTheme(
    themeMode: String = "dark",
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val colorScheme = when (themeMode) {
        "amoled" -> EyeAmoledColorScheme
        "light" -> EyeLightColorScheme
        "system" -> if (isSystemDark) EyeDarkColorScheme else EyeLightColorScheme
        else -> EyeDarkColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
