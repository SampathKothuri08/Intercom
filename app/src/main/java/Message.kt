package com.group2.intercom

import com.google.firebase.Timestamp

data class Message (
    val senderID: String="",
    val receiverID: String="",
    val msg_sent: String="",
    val msg_received: String="",
    val timestamp: Timestamp=Timestamp.now()
)
