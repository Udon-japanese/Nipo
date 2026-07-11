package com.example.nipo.ui.theme

import androidx.compose.ui.graphics.Color
import com.example.nipo.data.PostTag

data class TagStyle(
    val bg: Color,
    val fg: Color,
    val border: Color
)

val tagStyles: Map<PostTag, TagStyle> = mapOf(
    PostTag.TRASH_CAN to TagStyle(Color(0xFFE3D3A8), Color(0xFF5C4A30), TipsBorder),
    PostTag.CHARGING to TagStyle(Color(0xFFE3D3A8), Color(0xFF5C4A30), TipsBorder),
    PostTag.WATER_FOUNTAIN to TagStyle(Color(0xFFE3D3A8), Color(0xFF5C4A30), TipsBorder),
    PostTag.SMOKING_AREA to TagStyle(Color(0xFFE3D3A8), Color(0xFF5C4A30), TipsBorder),
    PostTag.RESTROOM_FREE to TagStyle(Color(0xFFDCEAD8), Color(0xFF33562F), Color(0xFFAFCBA6)),
    PostTag.QUIET_SPOT to TagStyle(Color(0xFFDCEAD8), Color(0xFF33562F), Color(0xFFAFCBA6)),
    PostTag.SEATING to TagStyle(Color(0xFFDCEAD8), Color(0xFF33562F), Color(0xFFAFCBA6)),
    PostTag.STEP_CAUTION to TagStyle(Color(0xFFF3DCD0), Color(0xFF8A4A28), Color(0xFFE0B79B)),
    PostTag.WEAK_WIFI to TagStyle(Color(0xFFF3DCD0), Color(0xFF8A4A28), Color(0xFFE0B79B)),
    PostTag.CROWDED_HOURS to TagStyle(Color(0xFFF3DCD0), Color(0xFF8A4A28), Color(0xFFE0B79B)),
)

val PostTag.style: TagStyle
    get() = tagStyles.getValue(this)
