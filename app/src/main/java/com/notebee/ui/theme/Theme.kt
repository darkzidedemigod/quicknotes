package com.notebee.ui.theme

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

private val DarkColorScheme = darkColorScheme(
    primary = NoteBeePrimary, // Use NoteBeePrimary even in dark mode for consistency
    secondary = PinYellow,
    tertiary = Pink80,
    onPrimary = Color.Black
)

private val LightColorScheme = lightColorScheme(
    primary = NoteBeePrimary,
    secondary = PinYellow,
    tertiary = Pink40,
    onPrimary = Color.Black
)

@Composable
fun QuickNotesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Set dynamicColor to false to prevent system colors from overriding yours on Android 12+
    dynamicColor: Boolean = false,
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
            // Set the status bar color to match the primary theme color
            window.statusBarColor = colorScheme.primary.toArgb()
            // Since the primary color is light (Yellow), we want dark icons on the status bar
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
