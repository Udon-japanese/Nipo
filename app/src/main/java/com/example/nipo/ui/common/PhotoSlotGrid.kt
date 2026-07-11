package com.example.nipo.ui.common

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.nipo.ui.theme.LocalNipoModeColors
import com.example.nipo.ui.theme.PhotoSlotShape

@Composable
fun PhotoSlotGrid(
    photoUris: List<Uri>,
    maxSlots: Int,
    onAddPhoto: () -> Unit,
    onRemovePhoto: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalNipoModeColors.current
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        repeat(maxSlots) { index ->
            val uri = photoUris.getOrNull(index)
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(PhotoSlotShape)
                    .background(colors.cardBackground)
                    .border(BorderStroke(1.dp, colors.border), PhotoSlotShape)
                    .clickable {
                        if (uri == null) onAddPhoto() else onRemovePhoto(index)
                    },
                contentAlignment = Alignment.Center,
            ) {
                if (uri != null) {
                    AsyncImage(model = uri, contentDescription = null, modifier = Modifier.size(76.dp))
                } else {
                    Text("＋", color = colors.mutedText)
                }
            }
        }
    }
}
