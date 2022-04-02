package com.conseo.database.dao

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage

class ServiceDao(
    private val database: FirebaseFirestore,
    private val storage: FirebaseStorage
) {

    fun getAllItemsByService(service: String): Task<QuerySnapshot> {
        return database.collection(service).get()
    }
}