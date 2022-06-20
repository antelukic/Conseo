package com.lukic.conseo.settings.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.conseo.database.entity.UserEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.lukic.conseo.settings.model.SettingsRepository
import com.lukic.conseo.utils.AppPreferences
import com.lukic.conseo.utils.AppPrefs
import com.lukic.conseo.utils.awaitTask
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

    val barsChecked = MutableLiveData<Boolean>()
    val restaurantsChecked = MutableLiveData<Boolean>()
    val eventsChecked = MutableLiveData<Boolean>()

    fun getCurrentUserData() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val documentSnapshot =
                    settingsRepository.getUserById(auth.currentUser?.uid ?: "noID")
                        .awaitTask(viewModelScope)
                if (documentSnapshot != null) {
                    val users = documentSnapshot.toObject(UserEntity::class.java)
                    _user.postValue(users)
                } else {
                    Log.e(TAG, "getCurrentUserData: document snapshot is null")
                    _user.postValue(null)
                }
            } catch (e: Exception) {
                Log.e(TAG, "getCurrentUserData: ${e.message}")
                _user.postValue(null)
            }
        }
    }

    fun getDistanceInKm() {
        distance.postValue(appPrefs.getInt(AppPrefs.DISTANCE_KEY))
    }

    fun updateDistance(value: Float) = viewModelScope.launch(Dispatchers.Default) {
        distance.postValue(value.toInt())
    }

    fun changeName() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d(TAG, "changeName: ${user.value}")
                val documentSnapshot = user.value?.name?.let {
                    settingsRepository.getUserDocument(auth.currentUser?.uid ?: "noID", it)
                        .awaitTask(viewModelScope)
                }
                if (documentSnapshot != null) {
                    _user.postValue(user.value)
                } else {
                    Log.e(TAG, "changeName: documentSnapshot is null")
                }
            } catch (e: Exception) {
                Log.e(TAG, "changeName: ${e.message}")
            }
        }

    }

    fun logout() {
        viewModelScope.launch(Dispatchers.IO) {
            auth.signOut()
        }
    }

    fun storeDistance() {
        appPrefs.putInt(key = AppPrefs.DISTANCE_KEY, value = distance.value ?: 0)
    }

    fun getAllSubscribedTopics() = viewModelScope.launch(Dispatchers.IO) {
        Log.d(TAG, "getAllSubscribedTopics: bars checked ${appPrefs.getBoolean(AppPrefs.BARS_KEY)}")
        Log.d(
            TAG,
            "getAllSubscribedTopics: restaurants checked ${appPrefs.getBoolean(AppPrefs.EVENTS_KEY)}"
        )
        Log.d(
            TAG,
            "getAllSubscribedTopics: events checked ${appPrefs.getBoolean(AppPrefs.RESTAURANTS_KEY)}"
        )
        barsChecked.postValue(appPrefs.getBoolean(AppPrefs.BARS_KEY))
        eventsChecked.postValue(appPrefs.getBoolean(AppPrefs.EVENTS_KEY))
        restaurantsChecked.postValue(appPrefs.getBoolean(AppPrefs.RESTAURANTS_KEY))
    }

    fun saveNotificationsChoice() = viewModelScope.launch(Dispatchers.IO) {
        if (eventsChecked.value == true) {
            FirebaseMessaging.getInstance().subscribeToTopic("events")
        } else {
            FirebaseMessaging.getInstance().unsubscribeFromTopic("events")
        }
        if (restaurantsChecked.value == true) {
            FirebaseMessaging.getInstance().subscribeToTopic("restaurants")
        } else {
            FirebaseMessaging.getInstance().unsubscribeFromTopic("restaurants")
        }
        if (barsChecked.value == true) {
            FirebaseMessaging.getInstance().subscribeToTopic("bars")
        } else {
            FirebaseMessaging.getInstance().unsubscribeFromTopic("bars")
        }

        appPrefs.putBoolean(key = AppPrefs.BARS_KEY, barsChecked.value ?: false)
        appPrefs.putBoolean(key = AppPrefs.EVENTS_KEY, eventsChecked.value ?: false)
        appPrefs.putBoolean(key = AppPrefs.RESTAURANTS_KEY, restaurantsChecked.value ?: false)
    }
}