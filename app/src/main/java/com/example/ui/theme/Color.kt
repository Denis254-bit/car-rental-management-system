package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// --- Clean Minimalism Design Theme Palette (Primary Slate Violet) ---
val MinimalBackground = Color(0xFFFDF8FD)
val MinimalSurface = Color(0xFFFFFFFF)
val MinimalSurfaceVariant = Color(0xFFF7F2FA)

val PrimaryViolet = Color(0xFF6750A4)
val SecondaryViolet = Color(0xFF625B71)

val AccentPurpleBg = Color(0xFFD0BCFF)
val AccentPurpleText = Color(0xFF21005D)
val LightPurplePill = Color(0xFFE8DEF8)
val LightPurplePillText = Color(0xFF1D192B)

val TextSlate900 = Color(0xFF1D1B20)
val TextSlate500 = Color(0xFF49454F)

// System status styling
val StatusGreen = Color(0xFF2E7D32)
val StatusGreenBg = Color(0xFFE8F5E9)
val StatusRed = Color(0xFFC62828)
val StatusRedBg = Color(0xFFFFEBEE)

// --- Classic Dark Theme Palette mappings ---
val DarkBackground = Color(0xFF1C1B1F)
val DarkSurface = Color(0xFF252427)
val DarkSurfaceVariant = Color(0xFF313033)
val DarkTextPrimary = Color(0xFFE6E1E5)
val DarkTextSecondary = Color(0xFFCAC4D0)

// --- Retro Compatibility Layer mapped dynamically to Active Theme ---
val CyberCyan: Color
    @Composable
    get() = MaterialTheme.colorScheme.primary

val CarbonBackground: Color
    @Composable
    get() = MaterialTheme.colorScheme.background

val CarbonSurface: Color
    @Composable
    get() = MaterialTheme.colorScheme.surface

val CarbonSurfaceVariant: Color
    @Composable
    get() = MaterialTheme.colorScheme.surfaceVariant

val ElectricBlue: Color
    @Composable
    get() = MaterialTheme.colorScheme.secondary

val TextPrimary: Color
    @Composable
    get() = MaterialTheme.colorScheme.onSurface

val TextSecondary: Color
    @Composable
    get() = MaterialTheme.colorScheme.onSurfaceVariant

val HighContrastWhite: Color
    @Composable
    get() = MaterialTheme.colorScheme.onSurface
