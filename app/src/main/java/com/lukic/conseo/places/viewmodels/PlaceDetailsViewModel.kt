package com.lukic.conseo.places.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.conseo.database.entity.CommentsEntity
import com.conseo.database.entity.PlaceEntity
import com.conseo.database.entity.UserEntity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.lukic.conseo.chat.model.ChatRepository
import com.lukic.conseo.places.model.PlacesRepository
import com.lukic.conseo.utils.awaitTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException


private const val TAG = "PlaceDetailsViewModel"

class PlaceDetailsViewModel(
    private val placesRepository: PlacesRepository,
    private val chatsRepository: ChatRepository
) : ViewModel() {

    private val _place = MutableLiveData<PlaceEntity>()
    val place get() = _place as LiveData<PlaceEntity>

    private val _comments = MutableLiveData<List<CommentsEntity>>()
    val comments get() = _comments as LiveData<List<CommentsEntity>>

    private val _user = MutableLiveData<UserEntity>()

    val postComment = MutableLiveData<String>()

    val errorOccurred = MutableLiveData(false)

    init {
        getCurrentUserDetails()
    }

    private fun getCurrentUserDetails() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val documentSnapshot =
                    chatsRepository.getUserById(Firebase.auth.currentUser?.uid ?: "")
                        .awaitTask(viewModelScope)
                if (documentSnapshot != null)
                    _user.postValue(
                        documentSnapshot.toObject(UserEntity::class.java)
                    )
                else
                    Log.e(
                        TAG,
                        "getCurrentUserDetails: user is null",
                    )
            } catch (e: Exception) {
                Log.e(TAG, "getCurrentUserDetails: ${e.message}")
            }
        }
    }

    fun getPlaceByID(placeID: String, serviceType: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val querySnapshot =
                    placesRepository.getPlaceByID(placeID = placeID, serviceType = serviceType)
                        .awaitTask(viewModelScope)
                if (querySnapshot != null) {
                    val placeObject = querySnapshot.toObjects(PlaceEntity::class.java)
                    if (placeObject.isNotEmpty())
                        _place.postValue(placeObject.first())
                } else {
                    errorOccurred.postValue(true)
                    Log.e(TAG, "getPlaceByID: querySnapshot is null")
                }
            } catch (e: Exception) {
                Log.e(TAG, "getPlaceByID: ${e.message}")
            }
        }
    }

    fun getCommentsBy(placeID: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val querySnapshot = placesRepository.getCommentsForPlace(placeID = placeID)
                    .awaitTask(viewModelScope)
                if (querySnapshot != null) {
                    val commentsObject =
                        querySnapshot.toObjects(CommentsEntity::class.java)
                    _comments.postValue(commentsObject)
                } else {
                    errorOccurred.postValue(true)
                    Log.e(TAG, "getCommentsBy: ERROR querySnapshot is null")
                }
            } catch (e: Exception) {
                Log.e(TAG, "getCommentsBy: ${e.message}")
            }
        }
    }

    fun postComment() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val comment = CommentsEntity(
                    body = postComment.value,
                    title = _user.value?.name,
                    placeID = place.value?.placeID,
                    image = _user.value?.image
                )
                val querySnapshot = placesRepository.postComment(comment).awaitTask(viewModelScope)
                if (querySnapshot != null) {
                    val commentsTemp = comments.value as ArrayList<CommentsEntity>? ?: arrayListOf()
                    commentsTemp.add(comment)
                    _comments.postValue(commentsTemp)
                    postComment.postValue("")
                } else {
                    Log.e(TAG, "postComment: querySnapshot is null")
                }
            } catch (e: IllegalArgumentException) {
                Log.e(TAG, "postComment: ERROR ${e.message}")
            }
        }

    }

}