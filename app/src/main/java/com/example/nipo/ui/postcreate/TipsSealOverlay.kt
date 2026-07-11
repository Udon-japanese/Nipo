package com.example.nipo.ui.postcreate

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.example.nipo.ui.common.WaxSeal
import com.example.nipo.ui.theme.SealMotion
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Letter-sealing overlay shown right after a successful Tips submission,
 * mirroring the mockup's `flapClose` (triangular envelope flap folding down
 * via a 3D rotateX) + `sealPop` (wax seal stamping in) sequence.
 */
@Composable
fun TipsSealOverlay(
    visible: Boolean,
    message: String = "手紙を投函しました",
    onFinished: () -> Unit,
) {
    if (!visible) return

    val flapRotation = remember { Animatable(180f) }
    val sealScale = remember { Animatable(0f) }
    val sealAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            delay(SealMotion.FLAP_DELAY_MS.toLong())
            flapRotation.animateTo(0f, tween(SealMotion.FLAP_DURATION_MS, easing = LinearOutSlowInEasing))
        }
        launch {
            delay(SealMotion.SEAL_POP_DELAY_MS.toLong())
            sealAlpha.animateTo(1f, tween(SealMotion.SEAL_POP_DURATION_MS))
            sealScale.animateTo(1f, tween(SealMotion.SEAL_POP_DURATION_MS))
        }
        delay(SealMotion.OVERLAY_TOTAL_MS.toLong())
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC20160C)),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier.size(width = 150.dp, height = 104.dp),
                contentAlignment = Alignment.TopCenter,
            ) {
                // Envelope body
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF2E2B8), RoundedCornerShape(4.dp))
                        .border(1.dp, Color(0xFFB8935A), RoundedCornerShape(4.dp))
                )
                // Triangular flap, folding down via a 3D rotateX around its top edge
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            rotationX = flapRotation.value
                            cameraDistance = 16f * density
                            transformOrigin = TransformOrigin(0.5f, 0f)
                        }
                ) {
                    val flapHeight = size.height * 0.52f
                    val path = Path().apply {
                        moveTo(0f, 0f)
                        lineTo(size.width, 0f)
                        lineTo(size.width / 2f, flapHeight)
                        close()
                    }
                    drawPath(path, color = Color(0xFFD8B57C))
                    drawPath(path, color = Color(0xFFB8935A), style = Stroke(width = 1.dp.toPx()))
                }
                Box(
                    modifier = Modifier
                        .padding(top = 38.dp)
                        .size(38.dp)
                        .scale(sealScale.value)
                        .graphicsLayer { alpha = sealAlpha.value },
                    contentAlignment = Alignment.Center,
                ) {
                    WaxSeal(size = 38.dp)
                }
            }
            Text(
                text = message,
                color = Color(0xFFF2E2B8),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 22.dp),
            )
        }
    }
}
