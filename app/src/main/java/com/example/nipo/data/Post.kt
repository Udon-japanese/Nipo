package com.example.nipo.data

data class Post(
    val id: String = "",
    val title: String = "",
    val label: String = PostLabel.TROUBLED.name,
    val locationDetail: String = "",
    val geo: com.google.firebase.firestore.GeoPoint? = null,
    val placeName: String? = null,
    val photoUrl: String? = null,
    val authorUid: String = "",
    val createdAt: com.google.firebase.Timestamp? = null
)
