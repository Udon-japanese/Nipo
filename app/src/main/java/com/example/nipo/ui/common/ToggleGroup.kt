package com.example.nipo.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.nipo.data.PostTag

/**
 * Single or multi-select chip group over [PostTag]s, generalizing the app's
 * former private `LabelSelectButton` toggle-pair into a reusable N-option row.
 */
@Composable
fun TagChipGroup(
    options: List<PostTag>,
    selected: Set<PostTag>,
    onToggle: (PostTag) -> Unit,
    modifier: Modifier = Modifier,
) {
    SimpleFlowRow(modifier = modifier, horizontalGap = 8.dp, verticalGap = 8.dp) {
        options.forEach { tag ->
            TagChip(
                tag = tag,
                selected = tag in selected,
                onClick = { onToggle(tag) },
            )
        }
    }
}
