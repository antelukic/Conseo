package com.conseo.database.entity

import com.google.firebase.Timestamp

data class MessageEntity(
    val senderID: String? = null,
    val recieverID: String? = null,
    val message: String? = null,
    val time: Timestamp? = null
)

data class Chat(
    val messageID: String? = null
)