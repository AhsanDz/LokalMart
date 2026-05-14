package com.kelompok4.lokalmart.core.common.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = Green600,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = Green100,
    onPrimaryContainer = Green800,
    secondary = Green700,
    background = androidx.compose.ui.graphics.Color.White,
    onBackground = Neutral900,
    surface = Neutral50,
    onSurface = Neutral900,
    surfaceVariant = Neutral100,
    onSurfaceVariant = Neutral600,
    error = ErrorRed,
    outline = Neutral200
)

private val DarkColors = darkColorScheme(
    primary = Green500,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = Green800,
    onPrimaryContainer = Green100,
    secondary = Green500,
    background = Neutral900,
    onBackground = Neutral50,
    surface = Neutral800,
    onSurface = Neutral50,
    surfaceVariant = Neutral800,
    onSurfaceVariant = Neutral400,
    error = ErrorRed,
    outline = Neutral600
)

@Composable
fun LokalMartTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = LokalMartTypography,
        content = content
    )
}
