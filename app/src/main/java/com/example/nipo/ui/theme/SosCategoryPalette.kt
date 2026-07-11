package com.example.nipo.ui.theme

import androidx.compose.ui.graphics.Color
import com.example.nipo.data.SosCategory

data class SosCategoryStyle(
    val bg: Color,
    val fg: Color,
    val border: Color,
)

// 選択時の塗りつぶし色は、SosBg(カード背景)と被らないよう、はっきり濃い色にする
val sosCategoryStyles: Map<SosCategory, SosCategoryStyle> = mapOf(
    SosCategory.LOST to SosCategoryStyle(SosGradientEnd, Color.White, SosGradientEnd),
    SosCategory.ILLNESS to SosCategoryStyle(SosWarnText, Color.White, SosWarnText),
    SosCategory.LOST_ITEM to SosCategoryStyle(Color(0xFF4C8C6B), Color.White, Color(0xFF4C8C6B)),
    SosCategory.OTHER to SosCategoryStyle(Color(0xFF7A6FA6), Color.White, Color(0xFF7A6FA6)),
)

val SosCategory.style: SosCategoryStyle
    get() = sosCategoryStyles.getValue(this)
