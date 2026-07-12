package com.example.nipo.ui.soscreate

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.nipo.ui.theme.NipoTheme
import com.example.nipo.ui.theme.SosGradientEnd
import com.example.nipo.ui.theme.SosGradientStart
import kotlinx.coroutines.delay

/**
 * メール作成画面 → 送信ボタンにカーソルが入ってクリック → 送信中 → トースト通知
 * という一連の流れを表現するオーバーレイ。
 */
@Composable
fun SosEmailOverlay(
    visible: Boolean,
    onFinished: () -> Unit,
) {
    if (!visible) return

    val density = LocalDensity.current

    // カーソルの開始位置(カード右下あたり)→ ボタン中心(0,0)へ移動
    val cursorOffset = remember { Animatable(Offset(160f, 220f), Offset.VectorConverter) }
    val buttonScale = remember { Animatable(1f) }

    var showCursor by remember { mutableStateOf(true) }
    var showSpinner by remember { mutableStateOf(false) }
    var isSent by remember { mutableStateOf(false) }
    var showToast by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // 1. カーソルが送信ボタンへ移動
        cursorOffset.animateTo(
            targetValue = Offset(0f, 0f),
            animationSpec = tween(durationMillis = 900, easing = LinearEasing)
        )

        // 2. クリック(押し込み)アニメーション
        buttonScale.animateTo(0.9f, tween(90))
        buttonScale.animateTo(1.0f, tween(90))

        // クリック後はカーソルを消してボタンの状態表示に集中させる
        delay(120)
        showCursor = false

        // 3. 送信中(スピナー)
        showSpinner = true
        delay(1100)
        showSpinner = false
        isSent = true

        // 4. メール作成カードは少し余韻を置いてフェードアウトし、
        //    画面下からトースト通知が現れる
        delay(300)
        showToast = true
        delay(1800)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xAA000000)),
        contentAlignment = Alignment.Center
    ) {
        // ===== メール作成カード =====
        AnimatedVisibility(
            visible = !isSent,
            exit = fadeOut(tween(250)),
        ) {
            Surface(
                modifier = Modifier
                    .padding(28.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Column(modifier = Modifier.padding(20.dp)) {

                    // --- メールクライアント風ヘッダー ---
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "新規メッセージ",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333)
                        )
                        Text(
                            "SOS",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = SosGradientStart
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    EmailField(label = "宛先", value = "emergency-contact@nipo.app")
                    Spacer(Modifier.height(6.dp))
                    EmailField(label = "件名", value = "【緊急】SOS通知")

                    Spacer(Modifier.height(14.dp))

                    // 本文っぽく見せるダミーの行(テキスト表現を補強)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        TextLineBar(widthFraction = 1f)
                        TextLineBar(widthFraction = 0.85f)
                        TextLineBar(widthFraction = 0.6f)
                    }

                    Spacer(Modifier.height(24.dp))

                    // --- 送信ボタン + カーソル ---
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Row(
                            modifier = Modifier
                                .scale(buttonScale.value)
                                .clip(RoundedCornerShape(24.dp))
                                .background(SosGradientEnd)
                                .padding(horizontal = 28.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (showSpinner) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Text("送信中…", color = Color.White, style = MaterialTheme.typography.labelLarge)
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.Send,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text("送信", color = Color.White, style = MaterialTheme.typography.labelLarge)
                            }
                        }

                        // カーソル(ボタンに重なるよう中心基準でオフセット)
                        if (showCursor) {
                            MouseCursor(
                                modifier = Modifier
                                    .offset {
                                        with(density) {
                                            IntOffset(
                                                cursorOffset.value.x.dp.roundToPx(),
                                                cursorOffset.value.y.dp.roundToPx()
                                            )
                                        }
                                    }
                            )
                        }
                    }
                }
            }
        }

        // ===== 送信完了トースト =====
        AnimatedVisibility(
            visible = showToast,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp, start = 24.dp, end = 24.dp)
        ) {
            SosToast(text = "SOSメッセージを送信しました")
        }
    }
}

@Composable
private fun EmailField(label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            modifier = Modifier.width(44.dp)
        )
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF333333),
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun TextLineBar(widthFraction: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth(widthFraction)
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFFE8E8E8))
    )
}

/**
 * 一般的なマウスポインタの形。中を白で塗り、輪郭をやや太めの黒線で描く。
 */
@Composable
private fun MouseCursor(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(30.dp)) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(0f, h * 0.72f)
            lineTo(w * 0.24f, h * 0.54f)
            lineTo(w * 0.40f, h * 0.94f)
            lineTo(w * 0.54f, h * 0.88f)
            lineTo(w * 0.36f, h * 0.46f)
            lineTo(w * 0.66f, h * 0.46f)
            close()
        }
        // 白い塗り
        drawPath(path = path, color = Color.White)
        // やや太めの黒い輪郭
        drawPath(
            path = path,
            color = Color.Black,
            style = Stroke(width = size.minDimension * 0.09f, join = StrokeJoin.Round)
        )
    }
}

/**
 * Toast風の送信完了通知。
 */
@Composable
private fun SosToast(text: String) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = Color(0xFF2E2E2E),
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF4CAF50)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(13.dp)
                )
            }
            Text(
                text,
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SosEmailOverlayPreview() {
    NipoTheme {
        SosEmailOverlay(
            visible = true,
            onFinished = {}
        )
    }
}