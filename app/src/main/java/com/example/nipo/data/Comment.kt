package com.example.nipo.data

data class Comment(
    val id: String = "",
    val text: String = "",
    val photoUrl: String? = null,
    val authorUid: String = "",
    val createdAt: com.google.firebase.Timestamp? = null
)