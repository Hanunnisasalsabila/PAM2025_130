package com.clipvault.clipvault.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// === GAMING AESTHETIC THEME ===

// Dark Mode Color Scheme (Primary)
private val DarkColorScheme = darkColorScheme(
    primary = BrightBlue,           // Biru terang dari logo
    onPrimary = White,
    secondary = ElectricCyan,       // Cyan accent
    onSecondary = White,
    tertiary = PurpleAccent,        // Purple touch
    onTertiary = White,
    background = DeepPurple,        // Navy purple background
    onBackground = White,
    surface = DarkNavy,             // Dark navy surface
    onSurface = OffWhite,
    error = ErrorRed,
    onError = White
)

// Light Mode Color Scheme
private val LightColorScheme = lightColorScheme(
    primary = RoyalBlue,            // Deep blue
    onPrimary = White,
    secondary = BrightBlue,         // Bright blue accent
    onSecondary = White,
    tertiary = CyanGlow,            // Cyan highlight
    onTertiary = BlackText,
    background = OffWhite,          // Light background
    onBackground = BlackText,
    surface = White,                // White surface
    onSurface = BlackText,
    surfaceVariant = LightGray,
    onSurfaceVariant = DarkGray,
    error = ErrorRed,
    onError = White
)

@Composable
fun ClipVaultTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Matikan dynamic color biar konsisten
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Status bar matching primary color
            window.statusBarColor = if (darkTheme) DeepPurple.toArgb() else RoyalBlue.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // From Type.kt
        content = content
    )
}