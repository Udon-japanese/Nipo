package com.example.nipo.ui.common

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.nipo.ui.theme.PillShape

@Composable
fun NipoSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    checkedColor: Color? = null,
) {
    val trackColor = if (checked) (checkedColor ?: MaterialTheme.colorScheme.primary) else Color(0xFFD8D0BD)
    val thumbOffset by animateDpAsState(if (checked) 21.dp else 3.dp, label = "switchThumb")

    Box(
        modifier = modifier
            .size(width = 42.dp, height = 24.dp)
            .clip(PillShape)
            .background(trackColor)
            .clickable { onCheckedChange(!checked) },
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = thumbOffset)
                .size(18.dp)
                .clip(PillShape)
                .background(Color.White)
        )
    }
}
