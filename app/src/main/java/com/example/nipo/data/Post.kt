package com.example.nipo.data

data class Post(
    val id: String = "",
    val title: String = "",
    val label: String = PostTag.TRASH_CAN.name,
    val locationDetail: String = "",
    val geo: com.google.firebase.firestore.GeoPoint? = null,
    val placeName: String? = null,
    val photoUrls: List<String> = emptyList(),
    val authorUid: String = "",
    val createdAt: com.google.firebase.Timestamp? = null,
    val goodCount: Int = 0,
    val badCount: Int = 0,
    val isPossiblyOutdated: Boolean = false,
)
