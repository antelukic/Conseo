package com.conseo.database.dao

import com.conseo.database.entity.UserEntity
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
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

    fun storeAccount(userEntity: UserEntity): Task<Void> {
        return database.collection("users").document(userEntity.id ?: "noID").set(userEntity)
    }

    fun loginUser(email: String, password: String): Task<AuthResult> {
        return firebase.auth.signInWithEmailAndPassword(email, password)
    }

    fun getAllUsers(): Task<QuerySnapshot> {
        return database.collection("users").get()
    }

    fun getUserById(id: String): Task<DocumentSnapshot> {
        return database.collection("users").document(id).get()
    }

    fun updateUserDocument(userID: String, name: String): Task<Void> {
        return database.collection("users").document(userID).update("name", name)
    }
}