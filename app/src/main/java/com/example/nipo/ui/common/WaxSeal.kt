package com.example.nipo.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.nipo.ui.theme.TipsSealBase
import com.example.nipo.ui.theme.TipsSealShadow

@Composable
fun WaxSeal(modifier: Modifier = Modifier, size: Dp = 48.dp) {
    Box(
        modifier = modifier
            .size(size)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(TipsSealBase, TipsSealShadow),
                    radius = size.value,
                ),
                shape = CircleShape,
            ),
    )
}
