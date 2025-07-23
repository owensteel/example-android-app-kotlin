package com.owensteel.starlingroundup.ui.theme

import androidx.compose.ui.graphics.Color
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

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
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}