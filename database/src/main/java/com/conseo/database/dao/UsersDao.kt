package com.conseo.database.dao

import android.graphics.Bitmap
import com.conseo.database.entity.UserEntity
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask

class UsersDao(
    private val storage: FirebaseStorage,
    private val firebase: Firebase,
    private val database: FirebaseFirestore
) {

    fun registerAccount(email: String, password: String): Task<AuthResult> {
        return firebase.auth.createUserWithEmailAndPassword(email, password)
    }

    fun storeImageToStorage(imageByteArray: ByteArray, userEmail: String): UploadTask {
        return storage.reference.child("userImages/" + userEmail).putBytes(imageByteArray)
    }

    fun storeAccount(userEntity: UserEntity): Task<DocumentReference> {
        return database.collection("users").add(userEntity)
    }

    fun loginUser(email: String, password: String): Task<AuthResult> {
        return firebase.auth.signInWithEmailAndPassword(email, password)
    }
}