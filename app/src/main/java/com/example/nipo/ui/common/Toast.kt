package com.example.nipo.ui.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.nipo.ui.theme.PillShape
import kotlinx.coroutines.delay

class NipoToastState {
    var message by mutableStateOf<String?>(null)
        private set

    suspend fun show(text: String, durationMs: Long = 2000) {
        message = text
        delay(durationMs)
        message = null
    }
}

@Composable
fun rememberNipoToastState(): NipoToastState = remember { NipoToastState() }

@Composable
fun NipoToastHost(state: NipoToastState, modifier: Modifier = Modifier) {
    AnimatedVisibility(
        visible = state.message != null,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier,
    ) {
        Text(
            text = state.message.orEmpty(),
            color = Color.White,
            modifier = Modifier
                .clip(PillShape)
                .background(Color(0xFF2B2620))
                .padding(horizontal = 18.dp, vertical = 10.dp),
        )
    }
}
