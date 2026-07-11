package com.example.nipo.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.nipo.ui.theme.LocalNipoModeColors

@Composable
fun LoadingOverlay(modifier: Modifier = Modifier) {
    val colors = LocalNipoModeColors.current
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f)),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = colors.onAccent)
    }
}
