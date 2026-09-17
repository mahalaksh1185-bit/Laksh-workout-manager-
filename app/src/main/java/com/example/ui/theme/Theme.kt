package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val FitnessDarkColorScheme = darkColorScheme(
    primary = NeonLime,
    onPrimary = Color(0xFF0A0E13),
    primaryContainer = Color(0xFF243600),
    onPrimaryContainer = NeonLime,
    secondary = ElectricCyan,
    onSecondary = Color(0xFF002F35),
    secondaryContainer = Color(0xFF004D56),
    onSecondaryContainer = ElectricCyan,
    tertiary = FlameOrange,
    onTertiary = Color.White,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = DarkCardBorder,
    error = ErrorRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Fitness apps look best locked to a high-contrast dark aesthetic
    MaterialTheme(
        colorScheme = FitnessDarkColorScheme,
        typography = Typography,
        content = content
    )
}
