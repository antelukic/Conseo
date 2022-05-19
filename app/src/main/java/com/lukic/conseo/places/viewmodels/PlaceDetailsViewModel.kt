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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.w3c.dom.Comment
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

    val errorOccured = MutableLiveData(false)

    init {
        getCurrentUserDetails()
    }

    private fun getCurrentUserDetails() {
        viewModelScope.launch(Dispatchers.IO) {
            chatsRepository.getUserById(Firebase.auth.currentUser?.uid ?: "")
                .addOnCompleteListener { getUserTask ->
                    if (getUserTask.isSuccessful)
                        _user.postValue(
                            getUserTask.result.toObject(UserEntity::class.java)
                        )
                    else
                        Log.e(
                            TAG,
                            "getCurrentUserDetails: ERROR ${getUserTask.exception?.message}",
                        )
                }
        }
    }


    fun getPlaceByID(placeID: String, serviceType: String) {
        viewModelScope.launch(Dispatchers.IO) {
            placesRepository.getPlaceByID(placeID = placeID, serviceType = serviceType)
                .addOnCompleteListener { getPlaceTask ->
                    if (getPlaceTask.isSuccessful) {
                        val placeObject = getPlaceTask.result.toObjects(PlaceEntity::class.java)
                        Log.d(TAG, "getPlaceByID: placeobject $placeObject")
                        if (!placeObject.isNullOrEmpty())
                            _place.postValue(placeObject.first())
                    } else {
                        errorOccured.postValue(true)
                        Log.e(TAG, "getPlaceByID: ERROR ${getPlaceTask.exception?.message}")
                    }
                }
        }
    }

    fun getCommentsBy(placeID: String) {
        viewModelScope.launch(Dispatchers.IO) {
            placesRepository.getCommentsForPlace(placeID = placeID)
                .addOnCompleteListener { getCommentsTask ->
                    if (getCommentsTask.isSuccessful) {
                        val commentsObject =
                            getCommentsTask.result.toObjects(CommentsEntity::class.java)
                        Log.d(TAG, "getPlaceByID: placeobject $commentsObject")
                        _comments.postValue(commentsObject)
                    } else {
                        errorOccured.postValue(true)
                        Log.e(TAG, "getCommentsBy: ERROR ${getCommentsTask.exception?.message}")
                    }

                }
        }
    }

    fun postComment() {
        try {
            viewModelScope.launch(Dispatchers.IO) {
                val comment = CommentsEntity(
                    body = postComment.value,
                    title = _user.value?.name,
                    placeID = place.value?.placeID,
                    image = _user.value?.image
                )
                placesRepository.postComment(comment)
                    .addOnCompleteListener { postCommentTask ->
                        if (postCommentTask.isSuccessful) {
                            val commentsTemp = comments.value as ArrayList<CommentsEntity>? ?: arrayListOf()
                            commentsTemp.add(comment)
                            _comments.postValue(commentsTemp)
                            postComment.postValue("")
                        } else {
                            Log.e(TAG, "postComment: ERROR ${postCommentTask.exception?.message}")
                        }
                    }
            }
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "postComment: ERROR ${e.message}")
        }
    }

}