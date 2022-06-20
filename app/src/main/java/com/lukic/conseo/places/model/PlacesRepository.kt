package com.lukic.conseo.places.model

import android.net.Uri
import com.conseo.database.dao.CommentsDao
import com.conseo.database.dao.PlacesDao
import com.conseo.database.entity.CommentsEntity
import com.conseo.database.entity.PlaceEntity
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.UploadTask
import com.lukic.restapi.firebase.RetrofitInstance
import com.lukic.restapi.firebase.models.PushNotification
import kotlinx.coroutines.tasks.await
import okhttp3.ResponseBody
import retrofit2.Response

class PlacesRepository(
    private val placesDao: PlacesDao,
    private val commentsDao: CommentsDao,
    private val retrofitInstance: RetrofitInstance
) {


    fun getAllItemsByService(service: String): Task<QuerySnapshot> {
        return placesDao.getAllItemsByService(service)
    }

    suspend fun storeServiceImageToStorage(place: PlaceEntity, imageByteArray: ByteArray): Uri? {
        return placesDao.storeImageServiceToStorage(place = place, imageByteArray = imageByteArray).await().storage.downloadUrl.await()
    }

    fun storePlace(place: PlaceEntity): Task<DocumentReference> {
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

    suspend fun sendPlaceAddedNotification(notification: PushNotification): Response<ResponseBody> {
        return retrofitInstance.api.postNotification(notification)
    }

}