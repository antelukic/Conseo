package com.lukic.conseo.base

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.conseo.database.entity.UserEntity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.lukic.conseo.MyApplication
import com.lukic.conseo.utils.awaitTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "BaseViewModel"

class BaseViewModel(
    private val baseRepository: BaseRepository
) : ViewModel() {

    val bottomNavVisibility = MutableLiveData<Boolean>()

    fun checkUserToken() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val documentSnapshot =
                    baseRepository.getUserById(Firebase.auth.currentUser?.uid.toString())
                        .awaitTask(viewModelScope)
                if (documentSnapshot != null) {
                    val user = documentSnapshot.toObject(UserEntity::class.java)
                    if (user?.token != getTokenFromPrefs()) {
                        updateUserToken()
                    } else {
                        val token = FirebaseMessaging.getInstance().token.awaitTask(viewModelScope)
                        if (user.token != token) {
                            updateUserToken()
                        }
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "checkUserToken: ${e.message}")
            }
        }
    }

    private fun updateUserToken() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = baseRepository.updateUserToken(
                    Firebase.auth.currentUser?.uid.toString(),
                    getTokenFromPrefs()
                ).awaitTask(viewModelScope)
                if (result != null) {
                    Log.d(TAG, "updateUserToken: updated token")
                } else {
                    Log.e(TAG, "updateUserToken: token not updated")
                }

            } catch (e: Exception) {
                Log.e(TAG, "updateUserToken: ${e.message}")
            }
        }
    }

    private fun getTokenFromPrefs(): String {
        return MyApplication.getAppContext().getSharedPreferences(
            MyApplication.getAppContext().getString(
                com.lukic.restapi.R.string.token
            ), Context.MODE_PRIVATE
        ).getString(
            MyApplication.getAppContext().getString(
                com.lukic.restapi.R.string.token_key
            ), "NoToken"
        ).toString()

    }
}