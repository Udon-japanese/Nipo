package com.example.nipo.ui.postdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.nipo.ui.theme.ChipShape
import com.example.nipo.ui.theme.TipsAccentLight

@Composable
fun OutdatedBanner(modifier: Modifier = Modifier) {
    Text(
        text = "情報が古い可能性あり",
        color = Color.White,
        style = MaterialTheme.typography.labelSmall,
        modifier = modifier
            .clip(ChipShape)
            .background(TipsAccentLight)
            .padding(horizontal = 10.dp, vertical = 5.dp),
    )
}
