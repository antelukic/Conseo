package com.conseo.database.dao

import com.conseo.database.entity.Chat
import com.conseo.database.entity.MessageEntity
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot

class ChatDao(
    private val database: FirebaseFirestore
    ) {

    fun storeChat(room: String, userID: String): Task<DocumentReference> {
        return database.collection("chats").document(userID).collection("chat").add(Chat(messageID = room))
    }

    fun sendMessage(message: MessageEntity, room: String): Task<DocumentReference> {
        return database.collection("messages").document(room).collection("message").add(message)
    }

    fun getChats(userID: String): Task<QuerySnapshot> {
        return database.collection("chats").document(userID).collection("chat").get()
    }

    fun getAllUserMessages(room: String): Task<QuerySnapshot> {
        return database.collection("messages").document(room).collection("message").orderBy("time", Query.Direction.DESCENDING).get()
    }
}