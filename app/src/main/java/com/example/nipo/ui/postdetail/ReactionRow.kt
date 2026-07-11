package com.example.nipo.ui.postdetail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.nipo.ui.theme.TipsAccent
import com.example.nipo.ui.theme.TipsBorder
import com.example.nipo.ui.theme.TipsMutedText

/** Icon-only reaction buttons; [selected] is "good"/"bad"/null and highlights the user's current pick. */
@Composable
fun ReactionRow(
    goodCount: Int,
    badCount: Int,
    selected: String?,
    onReactGood: () -> Unit,
    onReactBad: () -> Unit,
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        val goodSelected = selected == "good"
        OutlinedButton(
            onClick = onReactGood,
            border = BorderStroke(if (goodSelected) 2.dp else 1.dp, TipsAccent),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = if (goodSelected) TipsAccent.copy(alpha = 0.12f) else Color.Transparent,
            ),
            modifier = Modifier.weight(1f),
        ) {
            Icon(Icons.Default.ThumbUp, contentDescription = "まだ役立つ", tint = TipsAccent, modifier = Modifier.size(16.dp))
            Text(" $goodCount", color = TipsAccent, style = MaterialTheme.typography.labelMedium)
        }
        val badSelected = selected == "bad"
        OutlinedButton(
            onClick = onReactBad,
            border = BorderStroke(if (badSelected) 2.dp else 1.dp, TipsBorder),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = if (badSelected) TipsMutedText.copy(alpha = 0.12f) else Color.Transparent,
            ),
            modifier = Modifier.weight(1f),
        ) {
            Icon(
                Icons.Default.ThumbUp,
                contentDescription = "違ってた",
                tint = TipsMutedText,
                modifier = Modifier.size(16.dp).rotate(180f),
            )
            Text(" $badCount", color = TipsMutedText, style = MaterialTheme.typography.labelMedium)
        }
    }
}
