package com.example.ui.theme

import androidx.compose.ui.graphics.Color

val CyberBg = Color(0xFF090D16)
val CyberSurface = Color(0xFF131B2E)
val CyberSurfaceVariant = Color(0xFF1E293B)
val CyberCyan = Color(0xFF00F0FF)
val CyberMagenta = Color(0xFFFF007A)
val CyberYellow = Color(0xFFFFE600)
val CyberGreen = Color(0xFF00FF88)
val CyberPurple = Color(0xFF9D4EDD)

val TextPrimary = Color(0xFFF1F5F9)
val TextSecondary = Color(0xFF94A3B8)
val TextMuted = Color(0xFF64748B)

val DarkCyberColorScheme = androidx.compose.material3.darkColorScheme(
    primary = CyberCyan,
    onPrimary = Color(0xFF001F24),
    primaryContainer = Color(0xFF004F58),
    onPrimaryContainer = Color(0xFF97F0FF),
    secondary = CyberMagenta,
    onSecondary = Color(0xFF3E001B),
    secondaryContainer = Color(0xFF7B0039),
    onSecondaryContainer = Color(0xFFFFD8E4),
    tertiary = CyberYellow,
    background = CyberBg,
    onBackground = TextPrimary,
    surface = CyberSurface,
    onSurface = TextPrimary,
    surfaceVariant = CyberSurfaceVariant,
    onSurfaceVariant = TextSecondary
)
