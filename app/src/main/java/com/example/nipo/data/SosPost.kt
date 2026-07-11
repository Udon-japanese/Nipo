package com.example.nipo.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint

enum class SosStatus { OPEN, CLOSED }

data class SosPost(
    val id: String = "",
    val authorUid: String = "",
    val location: GeoPoint? = null,
    val title: String = "",
    val category: String = "",   // SosCategory.name、任意タグのため空文字="未選択"
    val text: String = "",
    val photoUrl: String? = null,
    val status: String = SosStatus.OPEN.name,
    val createdAt: Timestamp? = null,
    val closedAt: Timestamp? = null,
)
