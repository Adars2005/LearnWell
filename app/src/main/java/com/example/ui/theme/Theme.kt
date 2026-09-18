package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = DuoGreen,
    onPrimary = Color.White,
    primaryContainer = DuoGreenLight,
    onPrimaryContainer = DuoGreenCorrectText,
    secondary = DuoBlue,
    onSecondary = Color.White,
    secondaryContainer = DuoBlueLight,
    onSecondaryContainer = DuoBlueDark,
    tertiary = DuoYellow,
    onTertiary = Color.White,
    error = DuoRed,
    onError = Color.White,
    errorContainer = DuoRedErrorBg,
    onErrorContainer = DuoRedErrorText,
    background = Color.White,
    onBackground = DuoGrayTextDark,
    surface = DuoCardBg,
    onSurface = DuoGrayTextDark,
    surfaceVariant = DuoGrayLight,
    onSurfaceVariant = DuoGrayText,
    outline = DuoGrayBorder,
    outlineVariant = DuoGrayBorderDark
)

private val DarkColorScheme = darkColorScheme(
    primary = DuoGreen,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1B3D0B),
    onPrimaryContainer = DuoGreenCorrectBg,
    secondary = DuoBlue,
    onSecondary = Color.White,
    tertiary = DuoYellow,
    background = Color(0xFF131A1C),
    onBackground = Color(0xFFF1F1F1),
    surface = Color(0xFF182226),
    onSurface = Color(0xFFF1F1F1),
    outline = Color(0xFF2E383D)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our handcrafted joyful Duolingo colors
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
