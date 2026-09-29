package com.noboj.weighwise.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = PrimaryTeal,
    onPrimary = LightSurface,
    secondary = HighlightAmber,
    onSecondary = LightInk,
    error = NegativeRed,
    onError = LightSurface,
    background = LightBackground,
    onBackground = LightInk,
    surface = LightSurface,
    onSurface = LightInk,
    onSurfaceVariant = LightSecondaryText,
    surfaceVariant = LightSurface,
    outline = LightSecondaryText
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryTeal,
    onPrimary = DarkSurface,
    secondary = HighlightAmber,
    onSecondary = DarkInk,
    error = NegativeRed,
    onError = DarkInk,
    background = DarkBackground,
    onBackground = DarkInk,
    surface = DarkSurface,
    onSurface = DarkInk,
    onSurfaceVariant = DarkSecondaryText,
    surfaceVariant = DarkElevated,
    outline = DarkSecondaryText
)

@Composable
fun WeighWiseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
