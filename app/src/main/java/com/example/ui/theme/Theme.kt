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
    primary = DarkPrimary,
    onPrimary = Color(0xFF003062),
    primaryContainer = Color(0xFF004689),
    onPrimaryContainer = Color(0xFFD7E3F7),
    secondary = DarkSecondary,
    onSecondary = Color(0xFF002D6E),
    tertiary = MinimalBluePrimary,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onBackground = Color(0xFFE2E2E9),
    onSurface = Color(0xFFE2E2E9),
    onSurfaceVariant = Color(0xFFC4C6D0),
    outline = Color(0xFF3B4048)
)

private val LightColorScheme = lightColorScheme(
    primary = MinimalBluePrimary,
    onPrimary = MinimalBlueOnPrimary,
    primaryContainer = MinimalBlueContainer,
    onPrimaryContainer = MinimalBlueOnContainer,
    secondary = MinimalSecondary,
    onSecondary = Color.White,
    secondaryContainer = MinimalSecondaryContainer,
    onSecondaryContainer = MinimalOnSecondaryContainer,
    tertiary = MinimalBluePrimary,
    background = MinimalBackground,
    surface = MinimalSurface,
    surfaceVariant = MinimalSurfaceVariant,
    onBackground = MinimalOnSurface,
    onSurface = MinimalOnSurface,
    onSurfaceVariant = MinimalOnSurfaceVariant,
    outline = MinimalOutline,
    outlineVariant = MinimalOutlineVariant,
    error = MinimalAlert,
    errorContainer = MinimalAlertContainer,
    onError = Color.White,
    onErrorContainer = Color(0xFF410002)
)

@Composable
fun ArtisanTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Preserve warm artisan look
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    ArtisanTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}

