package com.example.nipo.data

import com.google.firebase.Timestamp

data class SosMessage(
    val id: String = "",
    val senderUid: String = "",
    val text: String = "",
    val sentAt: Timestamp? = null,
)
