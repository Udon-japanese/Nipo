package com.example.nipo.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

fun sosHeaderGradient(): Brush = Brush.linearGradient(listOf(SosGradientStart, SosGradientEnd))

/** Light pastel-blue gradient used for the SOS info banner ("半径500m以内の人に..."), distinct from the saturated header gradient. */
fun sosInfoGradient(): Brush = Brush.linearGradient(listOf(Color(0xFFEAF1F8), Color(0xFFDBE9F5)))
