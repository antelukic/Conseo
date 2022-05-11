package com.lukic.conseo.places.model

import com.conseo.database.dao.PlacesDao
import com.conseo.database.entity.ServiceEntity
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.UploadTask

class PlacesRepository(
    private val placesDao: PlacesDao
) {


    fun getAllItemsByService(service: String): Task<QuerySnapshot> {
        return placesDao.getAllItemsByService(service)
    }

    fun storeServiceImageToStorage(service: ServiceEntity, imageByteArray: ByteArray): UploadTask {
        return placesDao.storeImageServiceToStorage(service = service, imageByteArray = imageByteArray)
    }

    fun storeService(service: ServiceEntity): Task<DocumentReference> {
        return placesDao.storeService(service = service)
    }

}