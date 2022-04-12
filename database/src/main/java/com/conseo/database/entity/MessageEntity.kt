package com.conseo.database.entity

data class MessageEntity(
    val senderID: String? = null,
    val recieverID: String? = null,
    val message: String? = null
)

data class Chat(
    val messageID: String? = null
)