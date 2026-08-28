package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = WaffleOrange,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF3D2205),
    onPrimaryContainer = GoldenAmber,
    
    secondary = GoldenAmber,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF382C05),
    onSecondaryContainer = GoldenAmber,
    
    tertiary = VegGreen,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF0F3D37),
    onTertiaryContainer = Color(0xFF88D4CC),
    
    background = DarkBackground,
    onBackground = TextPrimary,
    
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    
    error = ErrorRed,
    onError = Color.White,
    errorContainer = Color(0xFF4A1015),
    onErrorContainer = Color(0xFFFFB4AB),
    
    outline = CardBorder,
    outlineVariant = DarkSurfaceElevated
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // TJW Cafe is designed with a premium dark cafe aesthetic
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
