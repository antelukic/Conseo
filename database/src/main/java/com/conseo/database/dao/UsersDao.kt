package com.conseo.database.dao

import android.net.Uri
import com.conseo.database.entity.UserEntity
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.coroutines.tasks.asDeferred
import kotlinx.coroutines.tasks.await

class UsersDao(
    private val storage: FirebaseStorage,
    private val firebase: Firebase,
    private val database: FirebaseFirestore
) {

    fun registerAccount(email: String, password: String): Task<AuthResult> {
        return firebase.auth.createUserWithEmailAndPassword(email, password)
    }

    suspend fun storeImageToStorage(imageByteArray: ByteArray, userEmail: String): Uri? {
        return storage.reference.child("userImages/$userEmail").putBytes(imageByteArray).await().storage.downloadUrl.await()
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

    fun updateUserToken(userID: String, token: String): Task<Void>{
        return database.collection("users").document(userID).update("token", token)
    }
}