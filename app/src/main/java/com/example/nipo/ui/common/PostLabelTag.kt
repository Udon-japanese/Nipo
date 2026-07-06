package com.example.nipo.ui.common

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun PostLabelTag(label: String) {
    val isTroubled = label == "TROUBLED"
    val containerColor = if (isTroubled) Color(0xFFFFE3E3) else Color(0xFFE3F1FF)
    val contentColor = if (isTroubled) Color(0xFFC62828) else Color(0xFF1565C0)
    val text = if (isTroubled) "困ってる" else "未来への手紙"

    Surface(
        color = containerColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(50)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}