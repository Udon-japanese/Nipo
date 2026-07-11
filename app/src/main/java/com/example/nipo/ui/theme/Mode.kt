package com.example.nipo.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

sealed interface NipoMode {
    data object Neutral : NipoMode
    data object Tips : NipoMode
    data object Sos : NipoMode
}

data class NipoModeColors(
    val background: Color,
    val cardBackground: Color,
    val border: Color,
    val accent: Color,
    val accentLight: Color,
    val onAccent: Color,
    val mutedText: Color,
)

val neutralModeColors = NipoModeColors(
    background = NeutralBg,
    cardBackground = Color.White,
    border = NeutralBorder,
    accent = NeutralAccent,
    accentLight = NeutralAccent,
    onAccent = NeutralOnAccent,
    mutedText = NeutralMutedText,
)

val tipsModeColors = NipoModeColors(
    background = TipsBg,
    cardBackground = TipsCard,
    border = TipsBorder,
    accent = TipsAccent,
    accentLight = TipsAccentLight,
    onAccent = TipsCard,
    mutedText = TipsMutedText,
)

val sosModeColors = NipoModeColors(
    background = SosBg,
    cardBackground = Color.White,
    border = SosBorder,
    accent = SosGradientEnd,
    accentLight = SosGradientStart,
    onAccent = Color.White,
    mutedText = SosGradientEnd,
)

val LocalNipoModeColors = staticCompositionLocalOf { neutralModeColors }

@Composable
fun NipoModeProvider(mode: NipoMode, content: @Composable () -> Unit) {
    val colors = when (mode) {
        NipoMode.Neutral -> neutralModeColors
        NipoMode.Tips -> tipsModeColors
        NipoMode.Sos -> sosModeColors
    }
    CompositionLocalProvider(LocalNipoModeColors provides colors, content = content)
}
