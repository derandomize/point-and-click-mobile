package com.podzemnayapochta.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val UndergroundDarkColors =
    darkColorScheme(
        primary = LanternAmber,
        onPrimary = UndergroundShadow,
        secondary = LanternTeal,
        onSecondary = UndergroundShadow,
        tertiary = LanternHoney,
        background = UndergroundShadow,
        onBackground = ParchmentLight,
        surface = UndergroundViolet,
        onSurface = ParchmentLight,
    )

private val UndergroundLightColors =
    lightColorScheme(
        primary = UndergroundBurgundy,
        onPrimary = ParchmentLight,
        secondary = LanternTeal,
        tertiary = LanternAmber,
        background = ParchmentLight,
        onBackground = UndergroundShadow,
        surface = ParchmentDim,
        onSurface = UndergroundShadow,
    )

@Composable
fun PodzemnayaPochtaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) UndergroundDarkColors else UndergroundLightColors
    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}
