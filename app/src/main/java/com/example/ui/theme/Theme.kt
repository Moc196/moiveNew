package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val CinemaColorScheme = darkColorScheme(
    primary = SleekPrimary,
    secondary = SleekTextSecondary,
    tertiary = SleekPrimary,
    background = SleekBackground,
    surface = SleekSurface,
    surfaceVariant = SleekSurfaceVariant,
    onPrimary = SleekOnPrimary,
    onSecondary = SleekBackground,
    onTertiary = SleekBackground,
    onBackground = SleekTextPrimary,
    onSurface = SleekTextPrimary,
    onSurfaceVariant = SleekTextSecondary,
    outline = SleekOutline
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force dark mode for immersive cinema feel
    dynamicColor: Boolean = false, // Disable dynamic colors to preserve theme identity
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = CinemaColorScheme,
        typography = Typography,
        content = content
    )
}

