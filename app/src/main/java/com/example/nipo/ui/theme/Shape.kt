package com.example.nipo.ui.theme

import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val CardCornerShape = RoundedCornerShape(16.dp)
val PillShape = RoundedCornerShape(50)
val ChipShape = RoundedCornerShape(12.dp)
val PhotoSlotShape = RoundedCornerShape(6.dp)

/**
 * Rectangle with its bottom-right corner cut off diagonally (folded-page notch),
 * shared by the Tips letter card and other "torn corner" cards.
 */
@Composable
fun rememberNotchedCardShape(notch: Dp): Shape {
    val density = LocalDensity.current
    return remember(density, notch) {
        val notchPx = with(density) { notch.toPx() }
        GenericShape { size, _ ->
            moveTo(0f, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width, size.height - notchPx)
            lineTo(size.width - notchPx, size.height)
            lineTo(0f, size.height)
            close()
        }
    }
}
