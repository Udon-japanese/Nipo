package com.example.nipo.data

enum class SosCategory(val label: String, val showsEmergencyBanner: Boolean) {
    LOST("道に迷った/人を探している", false),
    ILLNESS("体調不良", true),
    LOST_ITEM("忘れ物・落とし物", false),
    OTHER("その他の困りごと", false),
}
