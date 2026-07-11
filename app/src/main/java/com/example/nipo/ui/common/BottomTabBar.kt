package com.example.nipo.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.nipo.ui.theme.NeutralMutedText
import com.example.nipo.ui.theme.NeutralText

data class BottomTabItem(
    val label: String,
    val icon: ImageVector,
    val selected: Boolean,
    val onClick: () -> Unit,
)

@Composable
fun BottomTabBar(items: List<BottomTabItem>, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = 6.dp),
    ) {
        items.forEach { item ->
            BottomTabEntry(item, Modifier)
        }
    }
}

@Composable
private fun RowScope.BottomTabEntry(item: BottomTabItem, modifier: Modifier) {
    Column(
        modifier = modifier
            .weight(1f)
            .clickable(onClick = item.onClick)
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val tint = if (item.selected) NeutralText else NeutralMutedText
        Icon(item.icon, contentDescription = item.label, tint = tint)
        Text(item.label, style = MaterialTheme.typography.labelSmall, color = tint)
    }
}
