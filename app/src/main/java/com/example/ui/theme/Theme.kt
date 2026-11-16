package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = BrandAccent,
    secondary = SecondaryNeonPurple,
    tertiary = PrimaryNeonBlue,
    background = OLEDBackground,
    surface = PremiumSurface,
    onPrimary = OLEDBackground,
    onSecondary = TextPrimaryDark,
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force dark theme for premium aesthetic
    dynamicColor: Boolean = false, // Disable wallpaper tinting to keep pure cyber aesthetics
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
