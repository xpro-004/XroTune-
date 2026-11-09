package com.xrotune.music.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

private val XroDarkScheme = darkColorScheme(
    primary = XroPurple,
    onPrimary = Color.White,
    primaryContainer = XroPurpleDark,
    onPrimaryContainer = Color.White,
    secondary = XroPink,
    onSecondary = Color.White,
    tertiary = XroCyan,
    onTertiary = Color.Black,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = DarkOutline,
    outlineVariant = DarkOutline,
    error = Color(0xFFFF5252),
    onError = Color.White,
)

/** Gradient used across headers and the mini player. */
val BrandGradient: Brush
    get() = Brush.linearGradient(listOf(XroPurple, XroPink))

@Composable
fun XroTuneTheme(
    @Suppress("UNUSED_PARAMETER") darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    // The app is dark-first; the light system setting still uses the dark brand scheme.
    MaterialTheme(
        colorScheme = XroDarkScheme,
        typography = XroTypography,
        content = content,
    )
}
