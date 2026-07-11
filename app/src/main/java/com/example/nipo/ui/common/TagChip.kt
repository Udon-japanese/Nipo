package com.example.nipo.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.nipo.data.PostTag
import com.example.nipo.ui.theme.ChipShape
import com.example.nipo.ui.theme.style

// Unselected chips always use this neutral paper tone so the change to the
// tag's own (sometimes close-in-value) color on selection stays visible
// regardless of which screen/background the chip sits on.
private val UnselectedChipBg = Color(0xFFFFFDF5)
private val UnselectedChipFg = Color(0xFF6B6255)

@Composable
fun TagChip(
    tag: PostTag,
    selected: Boolean = true,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val style = tag.style
    Surface(
        modifier = modifier.let { if (onClick != null) it.clickable(onClick = onClick) else it },
        color = if (selected) style.bg else UnselectedChipBg,
        contentColor = if (selected) style.fg else UnselectedChipFg,
        border = BorderStroke(if (selected) 1.5.dp else 1.dp, if (selected) style.fg else style.border),
        shape = ChipShape,
    ) {
        Text(
            text = tag.displayName,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp)
        )
    }
}
