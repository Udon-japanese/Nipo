package com.example.nipo.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = TipsAccentLight,
    onPrimary = NeutralOnAccent,
    secondary = SosGradientStart,
    tertiary = TipsAccent,
    background = NeutralText,
    surface = Color(0xFF2B2620),
    onBackground = NeutralBg,
    onSurface = NeutralBg,
)

private val LightColorScheme = lightColorScheme(
    primary = NeutralAccent,
    onPrimary = NeutralOnAccent,
    secondary = TipsAccent,
    tertiary = SosGradientEnd,
    background = NeutralBg,
    surface = Color.White,
    onBackground = NeutralText,
    onSurface = NeutralText,
    outline = NeutralBorder,
)

@Composable
fun NipoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
