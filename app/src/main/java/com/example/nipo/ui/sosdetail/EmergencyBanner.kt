package com.example.nipo.ui.sosdetail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.nipo.ui.theme.SosWarnBg
import com.example.nipo.ui.theme.SosWarnText

@Composable
fun EmergencyBanner(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = SosWarnBg,
        border = BorderStroke(1.dp, SosWarnText.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(10.dp),
    ) {
        Row(modifier = Modifier.padding(14.dp)) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = SosWarnText)
            Text(
                "命に関わる緊急の場合は、まず119/110へ連絡してください。このアプリはあくまで補助的な連絡手段です。",
                style = MaterialTheme.typography.bodySmall,
                color = SosWarnText,
                modifier = Modifier.padding(start = 10.dp),
            )
        }
    }
}
