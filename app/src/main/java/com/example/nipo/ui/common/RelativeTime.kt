package com.example.nipo.ui.common

import com.google.firebase.Timestamp
import java.util.concurrent.TimeUnit

fun formatRelativeTime(timestamp: Timestamp?): String {
    if (timestamp == null) return "たった今"
    val diffMs = System.currentTimeMillis() - timestamp.toDate().time
    val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMs)
    val hours = TimeUnit.MILLISECONDS.toHours(diffMs)
    val days = TimeUnit.MILLISECONDS.toDays(diffMs)
    val weeks = days / 7
    val months = days / 30
    return when {
        minutes < 1 -> "たった今"
        minutes < 60 -> "${minutes}分前"
        hours < 24 -> "${hours}時間前"
        days < 7 -> "${days}日前"
        weeks < 5 -> "${weeks}週間前"
        else -> "${months}ヶ月前"
    }
}
