package com.lukic.conseo.repository

import android.util.Log
import com.conseo.database.dao.UsersDao
import com.conseo.database.entity.UserEntity
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.UploadTask

private const val TAG = "AppRepository"
class AppRepository(
    private val usersDao: UsersDao
) {

    fun storeImageToStorage(imageByteArray: ByteArray, userEmail: String): UploadTask {
        return usersDao.storeImageToStorage(imageByteArray, userEmail)
    }

    fun registerAccount(email: String, password: String): Task<AuthResult> {
        val nes =  usersDao.registerAccount(email, password)
        return nes
    }

    fun saveUserToDB(userEntity: UserEntity): Task<DocumentReference> {
        return usersDao.storeAccount(userEntity)
    }

    fun loginUser(email: String, password: String): Task<AuthResult> {
        return usersDao.loginUser(email, password)
    }
}