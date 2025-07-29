package com.owensteel.starlingroundup.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    background = AppDarkBackground,
    onBackground = Color.White,
    surface = AppDarkBackground,
    onSurface = Color.White,

    primary = StarlingBrandPurple,
    onPrimary = OnStarlingBrandPurple,
    secondary = StarlingBrandCyan,
    onSecondary = OnStarlingBrandCyan,
    tertiary = StarlingBrandLightPurple
)

private val LightColorScheme = lightColorScheme(
    background = AppLightBackground,
    onBackground = Color.Black,
    surface = AppLightBackground,
    onSurface = Color.Black,

    primary = StarlingBrandPurple,
    onPrimary = OnStarlingBrandPurple,
    secondary = StarlingBrandCyan,
    onSecondary = OnStarlingBrandCyan,
    tertiary = StarlingBrandLightPurple
)

@Composable
fun StarlingRoundupTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Dynamic color schemes disabled as were interfering
    // with the set colour scheme
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}