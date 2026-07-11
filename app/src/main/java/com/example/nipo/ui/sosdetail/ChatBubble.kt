package com.example.nipo.ui.sosdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.example.nipo.data.SosMessage
import com.example.nipo.ui.theme.SosBg
import com.example.nipo.ui.theme.SosGradientEnd
import com.example.nipo.ui.theme.SosGradientStart

@Composable
fun ChatBubble(message: SosMessage, isOwnMessage: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isOwnMessage) Alignment.End else Alignment.Start,
    ) {
        Text(
            text = if (isOwnMessage) "あなた" else "相手",
            fontFamily = FontFamily.Monospace,
            fontSize = MaterialTheme.typography.labelSmall.fontSize,
            color = SosGradientStart,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
        )
        Text(
            text = message.text,
            color = if (isOwnMessage) Color.White else Color(0xFF2B2620),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .background(
                    color = if (isOwnMessage) SosGradientEnd else SosBg,
                    shape = RoundedCornerShape(12.dp),
                )
                .padding(horizontal = 14.dp, vertical = 10.dp),
        )
    }
}
