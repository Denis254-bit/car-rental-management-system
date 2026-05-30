package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = AccentPurpleBg,
    onPrimary = AccentPurpleText,
    primaryContainer = DarkSurfaceVariant,
    onPrimaryContainer = AccentPurpleBg,
    secondary = SecondaryViolet,
    onSecondary = Color.White,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    error = StatusRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryViolet,
    onPrimary = Color.White,
    primaryContainer = LightPurplePill,
    onPrimaryContainer = LightPurplePillText,
    secondary = SecondaryViolet,
    onSecondary = Color.White,
    background = MinimalBackground,
    onBackground = TextSlate900,
    surface = MinimalSurface,
    onSurface = TextSlate900,
    surfaceVariant = MinimalSurfaceVariant,
    onSurfaceVariant = TextSlate500,
    error = StatusRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
