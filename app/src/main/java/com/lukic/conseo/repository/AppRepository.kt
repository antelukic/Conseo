package com.lukic.conseo.repository

import com.conseo.database.dao.ChatDao
import com.conseo.database.dao.ServiceDao
import com.conseo.database.dao.UsersDao
import com.conseo.database.entity.MessageEntity
import com.conseo.database.entity.ServiceEntity
import com.conseo.database.entity.UserEntity
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.UploadTask

private const val TAG = "AppRepository"
class AppRepository(
    private val usersDao: UsersDao,
    private val serviceDao: ServiceDao,
    private val chatDao: ChatDao
) {

    fun storeImageToStorage(imageByteArray: ByteArray, userEmail: String): UploadTask {
        return usersDao.storeImageToStorage(imageByteArray, userEmail)
    }

    fun registerAccount(email: String, password: String): Task<AuthResult> {
        return usersDao.registerAccount(email, password)
    }

    fun saveUserToDB(userEntity: UserEntity): Task<DocumentReference> {
        return usersDao.storeAccount(userEntity)
    }

    fun loginUser(email: String, password: String): Task<AuthResult> {
        return usersDao.loginUser(email, password)
    }

    fun getAllItemsByService(service: String): Task<QuerySnapshot> {
        return serviceDao.getAllItemsByService(service)
    }

    fun storeServiceImageToStorage(service: ServiceEntity, imageByteArray: ByteArray): UploadTask {
        return serviceDao.storeImageServiceToStorage(service = service, imageByteArray = imageByteArray)
    }

    fun storeService(service: ServiceEntity): Task<DocumentReference>{
        return serviceDao.storeService(service = service)
    }

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

    fun getUserById(id: String): Task<QuerySnapshot> {
        return usersDao.getUserById(id = id)
    }
}