package com.lukic.conseo.chat.model

import com.conseo.database.dao.ChatDao
import com.conseo.database.dao.UsersDao
import com.conseo.database.entity.MessageEntity
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.lukic.restapi.firebase.RetrofitInstance
import com.lukic.restapi.firebase.models.PushNotification
import okhttp3.ResponseBody
import retrofit2.Response

class ChatRepository(
    private val chatDao: ChatDao,
    private val usersDao: UsersDao,
    private val fcmRetrofit: RetrofitInstance
) {
    fun storeChat(room: String, userID: String): Task<DocumentReference> {
        return chatDao.storeChat(room = room, userID = userID)
    }

    fun getMessages(room: String): Task<QuerySnapshot> {
        return chatDao.getAllUserMessages(room = room)
    }

    fun getChats(userID: String): Task<QuerySnapshot> {
        return chatDao.getChats(userID = userID)
    }

    fun getAllUsers(): Task<QuerySnapshot> {
        return usersDao.getAllUsers()
    }

    fun sendMessage(message: MessageEntity, room: String): Task<DocumentReference> {
        return chatDao.sendMessage(message = message, room = room)
    }

    fun getUserById(id: String): Task<DocumentSnapshot> {
        return usersDao.getUserById(id = id)
    }

    suspend fun sendChatNotification(notification: PushNotification): Response<ResponseBody> {
        return fcmRetrofit.api.postNotification(notification)
    }
}