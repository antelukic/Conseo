package com.lukic.conseo.loginregister.model

import android.net.Uri
import com.conseo.database.dao.UsersDao
import com.conseo.database.entity.UserEntity
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.storage.UploadTask

class LoginRegisterRepository(
    private val usersDao: UsersDao
) {

    suspend fun storeImageToStorage(imageByteArray: ByteArray, userEmail: String): Uri? {
        return usersDao.storeImageToStorage(imageByteArray, userEmail)
    }

    fun registerAccount(email: String, password: String): Task<AuthResult> {
        return usersDao.registerAccount(email, password)
    }

    fun saveUserToDB(userEntity: UserEntity): Task<Void> {
        return usersDao.storeAccount(userEntity)
    }

    fun loginUser(email: String, password: String): Task<AuthResult> {
        return usersDao.loginUser(email, password)
    }

}