package com.lukic.conseo.places.model

import com.conseo.database.dao.CommentsDao
import com.conseo.database.dao.PlacesDao
import com.conseo.database.entity.CommentsEntity
import com.conseo.database.entity.PlaceEntity
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.UploadTask

class PlacesRepository(
    private val placesDao: PlacesDao,
    private val commentsDao: CommentsDao
) {


    fun getAllItemsByService(service: String): Task<QuerySnapshot> {
        return placesDao.getAllItemsByService(service)
    }

    fun storeServiceImageToStorage(place: PlaceEntity, imageByteArray: ByteArray): UploadTask {
        return placesDao.storeImageServiceToStorage(place = place, imageByteArray = imageByteArray)
    }

    fun storeService(place: PlaceEntity): Task<DocumentReference> {
        return placesDao.storeService(place = place)
    }

    fun getPlaceByID(placeID: String, serviceType: String): Task<QuerySnapshot> {
        return placesDao.getPlaceById(placeID = placeID, serviceType = serviceType)
    }

    fun getCommentsForPlace(placeID: String): Task<QuerySnapshot> {
        return commentsDao.getCommentsForPlace(placeID = placeID)
    }

    @Throws(IllegalArgumentException::class)
    fun postComment(comment: CommentsEntity): Task<DocumentReference> {
        if(comment.placeID.isNullOrEmpty())
            throw IllegalArgumentException("PlaceID cannot be null")
        else
            return commentsDao.postComment(comment)
    }

}