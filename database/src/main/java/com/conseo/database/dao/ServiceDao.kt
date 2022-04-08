package com.conseo.database.dao

import com.conseo.database.entity.ServiceEntity
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask

class ServiceDao(
    private val database: FirebaseFirestore,
    private val storage: FirebaseStorage
) {

    fun getAllItemsByService(service: String): Task<QuerySnapshot> {
        return database.collection(service).get()
    }

    fun storeService(service: ServiceEntity): Task<DocumentReference> {
        return database.collection(service.serviceName!!.lowercase()).add(service)
    }

    fun storeImageServiceToStorage(service: ServiceEntity, imageByteArray: ByteArray): UploadTask {
        return  storage.reference.child("${service.serviceName}/" + service.name).putBytes(imageByteArray)
    }
}