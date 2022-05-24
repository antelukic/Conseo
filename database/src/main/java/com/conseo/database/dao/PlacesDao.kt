package com.conseo.database.dao

import com.conseo.database.entity.PlaceEntity
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask

class PlacesDao(
    private val database: FirebaseFirestore,
    private val storage: FirebaseStorage
) {

    fun getAllItemsByService(service: String): Task<QuerySnapshot> {
        return database.collection(service).get()
    }

    fun storeService(place: PlaceEntity): Task<DocumentReference> {
        return database.collection(place.serviceName!!.lowercase()).add(place)
    }

    fun storeImageServiceToStorage(place: PlaceEntity, imageByteArray: ByteArray): UploadTask {
        return  storage.reference.child("${place.serviceName}/" + place.name).putBytes(imageByteArray)
    }

    fun getPlaceById(placeID: String, serviceType: String): Task<QuerySnapshot> {
        return database.collection(serviceType.lowercase()).whereEqualTo("placeID", placeID).get()
    }

    fun getAllBars(): Task<QuerySnapshot> {
        return database.collection("bars").get()
    }

    fun getAllRestaurants(): Task<QuerySnapshot> {
        return database.collection("restaurants").get()
    }

    fun getAllEvents(): Task<QuerySnapshot>{
        return database.collection("events").get()
    }
}