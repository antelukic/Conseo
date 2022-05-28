package com.lukic.conseo.base

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.conseo.database.entity.UserEntity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.lukic.conseo.MyApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "BaseViewModel"

class BaseViewModel(
    private val baseRepository: BaseRepository
) : ViewModel() {

    val bottomNavVisibility = MutableLiveData<Boolean>()

    fun checkUserToken() {
        viewModelScope.launch(Dispatchers.IO) {
            baseRepository.getUserById(Firebase.auth.currentUser?.uid.toString())
                .addOnCompleteListener { getUserTask ->
                    if (getUserTask.isSuccessful) {
                        val user = getUserTask.result.toObject(UserEntity::class.java)
                        if (user?.token != getTokenFromPrefs()) {
                            updateUserToken()
                        } else {
                            Log.e(TAG, "checkUserToken: ${getUserTask.exception?.message}")
                        }
                    }
                }
        }
    }

    private fun updateUserToken() {
        viewModelScope.launch(Dispatchers.IO) {
            baseRepository.updateUserToken(
                Firebase.auth.currentUser?.uid.toString(),
                getTokenFromPrefs()
            )
                .addOnCompleteListener { updateUserToken ->
                    if (updateUserToken.isSuccessful) {
                        Log.d(TAG, "updateUserToken: updated token")
                    } else {
                        Log.e(TAG, "updateUserToken: ${updateUserToken.exception?.message}")
                    }
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