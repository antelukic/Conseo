package com.lukic.conseo.settings.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.conseo.database.entity.UserEntity
import com.google.firebase.auth.FirebaseAuth
import com.lukic.conseo.settings.model.SettingsRepository
import com.lukic.conseo.utils.AppPreferences
import com.lukic.conseo.utils.AppPrefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.qualifier.named
import org.koin.java.KoinJavaComponent


private const val TAG = "SettingsViewModel"

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val auth: FirebaseAuth,
) : ViewModel() {

    private val appPrefs: AppPreferences by KoinJavaComponent.inject(
        qualifier = named("SharedPreferences"),
        clazz = AppPrefs::class.java
    )

    private val _user = MutableLiveData<UserEntity?>()
    val user get() = _user as LiveData<UserEntity?>

    val distance = MutableLiveData<Int>()


    fun getCurrentUserData() {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.getUserById(auth.currentUser?.uid ?: "noID")
                .addOnCompleteListener { getUserTask ->
                    if (getUserTask.isSuccessful) {
                        val users = getUserTask.result.toObject(UserEntity::class.java)
                        _user.postValue(users)
                    } else {
                        Log.e(TAG, "getCurrentUserData: ${getUserTask.exception?.message}")
                        _user.postValue(null)
                    }
                }
        }
    }

    fun getDistance(){
            distance.postValue(appPrefs.getInt(AppPrefs.DISTANCE_KEY))
    }

    fun updateDistance(distance: Int) {
        appPrefs.putInt(key = AppPrefs.DISTANCE_KEY,value = distance)
        this.distance.postValue(distance)
    }

    fun changeName(userNewName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.getUserDocument(auth.currentUser?.uid ?: "noID", userNewName)
                .addOnCompleteListener { getUserDocumentTask ->
                    if (getUserDocumentTask.isSuccessful) {
                        val user = _user.value
                        user?.name = userNewName
                        _user.postValue(user)
                    } else {
                        Log.e(TAG, "changeName: ${getUserDocumentTask.exception?.message}")
                    }
                }
        }

    }

    fun logout() {
        viewModelScope.launch(Dispatchers.IO) {
            auth.signOut()
        }
    }
}