package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF00D4AA),       // Teal accent
    secondary = Color(0xFF0074D9),     // Goldman Sachs Blue
    tertiary = Color(0xFFFF851B),      // Warning/Auth label Amber
    background = Color(0xFF050910),    // Rich deep slate black
    surface = Color(0xFF0C1322),       // Card background navy
    onPrimary = Color(0xFF050910),     // Deep text on light green
    onSecondary = Color(0xFFFFFFFF),
    onBackground = Color(0xFFE2EDFB),  // High contrast body text
    onSurface = Color(0xFFC0D2E5)      // Secondary text
)

private val LightColorScheme = darkColorScheme(
    primary = Color(0xFF00D4AA),
    secondary = Color(0xFF0074D9),
    tertiary = Color(0xFFFF851B),
    background = Color(0xFF050910),
    surface = Color(0xFF0C1322),
    onPrimary = Color(0xFF050910),
    onSecondary = Color(0xFFFFFFFF),
    onBackground = Color(0xFFE2EDFB),
    onSurface = Color(0xFFC0D2E5)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force developer dark theme for stellar readability
    dynamicColor: Boolean = false, // Use our custom-crafted color scheme
    content: @Composable () -> Unit,
) {
    val colorScheme = DarkColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
