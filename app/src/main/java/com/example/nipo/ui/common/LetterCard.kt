package com.example.nipo.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.example.nipo.ui.theme.TipsCard
import com.example.nipo.ui.theme.rememberNotchedCardShape

private val NotchSize = 22.dp

/**
 * Letter card used for the Tips detail screen: a borderless rounded-corner card
 * with a bottom-right corner literally cut off (folded-page notch), rather than
 * an overlaid triangle.
 */
@Composable
fun LetterCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val shape = rememberNotchedCardShape(NotchSize)
    Surface(
        modifier = modifier
            .rotate(0.5f)
            .shadow(elevation = 10.dp, shape = shape, clip = false),
        color = TipsCard,
        shape = shape,
    ) {
        Column(modifier = Modifier.padding(top = 28.dp, start = 22.dp, end = 22.dp, bottom = 22.dp)) {
            content()
        }
    }
}
