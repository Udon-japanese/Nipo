package com.example.nipo.ui.common

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.nipo.ui.theme.LocalNipoModeColors

@Composable
fun AppTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val colors = LocalNipoModeColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Text("←", color = colors.accent, style = MaterialTheme.typography.titleLarge)
            }
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = colors.accent,
            modifier = Modifier.padding(start = if (onBack != null) 4.dp else 12.dp),
        )
        androidx.compose.foundation.layout.Spacer(Modifier.weight(1f))
        actions()
    }
}
