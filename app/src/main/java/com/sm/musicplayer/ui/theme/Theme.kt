package com.sm.musicplayer.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun MusicPlayerTheme(
    moodTheme: MoodTheme = MoodTheme.PASSION_RED,
    isDarkMode: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = getMoodColors(moodTheme)
    
    val colorScheme = if (isDarkMode) {
        darkColorScheme(
            primary = colors.primary,
            secondary = colors.secondary,
            tertiary = colors.accent,
            background = colors.background,
            surface = colors.surface,
            onPrimary = colors.onPrimary,
            onSecondary = colors.onPrimary,
            onTertiary = colors.onPrimary,
            onBackground = colors.onBackground,
            onSurface = colors.onSurface
        )
    } else {
        lightColorScheme(
            primary = colors.primary,
            secondary = colors.secondary,
            tertiary = colors.accent,
            onPrimary = colors.onPrimary,
            onSecondary = colors.onPrimary,
            onTertiary = colors.onPrimary
        )
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colors.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDarkMode
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
