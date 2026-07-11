package com.example.nipo.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Dp

/**
 * Minimal manual wrapping row, avoiding `androidx.compose.foundation.layout.FlowRow`
 * (its experimental API signature has caused a runtime `NoSuchMethodError` when the
 * compiled and packaged compose-foundation versions disagree).
 */
@Composable
fun SimpleFlowRow(
    modifier: Modifier = Modifier,
    horizontalGap: Dp,
    verticalGap: Dp,
    content: @Composable () -> Unit,
) {
    Layout(content = content, modifier = modifier) { measurables, constraints ->
        val hGapPx = horizontalGap.roundToPx()
        val vGapPx = verticalGap.roundToPx()
        val maxWidth = constraints.maxWidth

        val placeables = measurables.map { it.measure(constraints.copy(minWidth = 0, minHeight = 0)) }

        var x = 0
        var y = 0
        var rowHeight = 0
        val positions = ArrayList<Pair<Int, Int>>(placeables.size)

        placeables.forEach { placeable ->
            if (x != 0 && x + placeable.width > maxWidth) {
                x = 0
                y += rowHeight + vGapPx
                rowHeight = 0
            }
            positions.add(x to y)
            x += placeable.width + hGapPx
            rowHeight = maxOf(rowHeight, placeable.height)
        }

        val totalHeight = y + rowHeight

        layout(maxWidth, totalHeight) {
            placeables.forEachIndexed { index, placeable ->
                val (px, py) = positions[index]
                placeable.placeRelative(px, py)
            }
        }
    }
}
