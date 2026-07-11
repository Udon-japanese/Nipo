package com.example.nipo.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Teardrop-shaped map pin (rounded square with one sharp corner, rotated 45°),
 * mirroring the mockup's `border-radius:50% 50% 50% 0; transform:rotate(45deg)`.
 * Wrapped in a larger bounding box so the rotated diagonal isn't clipped when
 * Maps snapshots this composable to a marker bitmap (rotation is purely visual
 * and can extend past the un-rotated layout bounds).
 */
@Composable
fun TeardropMapPin(
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 34.dp,
) {
    val boundingSize = size * 1.5f
    Box(
        modifier = modifier.size(boundingSize),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .rotate(-45f)
                .clip(RoundedCornerShape(topStartPercent = 50, topEndPercent = 50, bottomEndPercent = 50, bottomStartPercent = 0))
                .background(color),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(size * 0.3f)
                    .rotate(45f)
                    .clip(CircleShape)
                    .background(Color.White)
            )
        }
    }
}
