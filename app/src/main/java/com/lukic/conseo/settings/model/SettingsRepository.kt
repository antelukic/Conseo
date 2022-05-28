package com.lukic.conseo.settings.model

import com.conseo.database.dao.UsersDao
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot

class SettingsRepository(
    private val userDao: UsersDao,
) {

    fun getUserById(userID: String): Task<DocumentSnapshot> {
        return userDao.getUserById(userID)
    }

    fun getUserDocument(userID: String, newUserName: String): Task<Void> {
        return userDao.updateUserDocument(userID, newUserName)
    }
}