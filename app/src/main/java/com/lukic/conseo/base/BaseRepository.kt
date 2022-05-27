package com.lukic.conseo.base

import com.conseo.database.dao.UsersDao
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot

class BaseRepository(
    private val usersDao: UsersDao
) {

    fun getUserById(userID: String): Task<DocumentSnapshot> {
        return usersDao.getUserById(userID)
    }

    fun updateUserToken(userID: String, token: String): Task<Void> {
        return usersDao.updateUserToken(userID, token)
    }
}