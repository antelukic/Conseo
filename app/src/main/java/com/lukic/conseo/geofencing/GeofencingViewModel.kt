package com.lukic.conseo.geofencing

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.conseo.database.entity.PlaceEntity
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.lukic.conseo.BuildConfig
import com.lukic.conseo.utils.AppPreferences
import com.lukic.conseo.utils.AppPrefs
import com.lukic.conseo.utils.awaitTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.qualifier.named
import org.koin.java.KoinJavaComponent

private const val TAG = "GeofencingViewModel"

class GeofencingViewModel(
    private val geofencingRepository: GeofencingRepository
) : ViewModel() {

    private val appPrefs: AppPreferences by KoinJavaComponent.inject(
        qualifier = named("SharedPreferences"),
        clazz = AppPrefs::class.java
    )

    private val _allPlaces = MutableLiveData<MutableList<PlaceEntity>?>()
    val allPlaces get() = _allPlaces as LiveData<MutableList<PlaceEntity>?>


    fun getAllPlaces() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val barDocuments = geofencingRepository.getAllBars().awaitTask(viewModelScope)
                if (barDocuments != null) {
                    val bars =
                        barDocuments.toObjects(PlaceEntity::class.java)
                    _allPlaces.postValue(bars)
                }
                val eventDocuments =
                    geofencingRepository.getAllEvents().awaitTask(viewModelScope)
                if (eventDocuments != null) {
                    val events = eventDocuments.toObjects(PlaceEntity::class.java)
                    _allPlaces.postValue(events)
                }
                val restaurantDocuments =
                    geofencingRepository.getAllRestaurants().awaitTask(viewModelScope)
                if (restaurantDocuments != null) {
                    val restaurants =
                        restaurantDocuments.toObjects(
                            PlaceEntity::class.java
                        )
                    _allPlaces.postValue(restaurants)
                }
            } catch (e: Exception) {
                Log.e(TAG, "getAllPlaces: ${e.message}")
            }
        }
    }

    fun passPlacesToGeonfencingList() {
        val tempGeofenceList = arrayListOf<Geofence>()
        allPlaces.value?.forEach { place ->
            tempGeofenceList.add(
                Geofence.Builder()
                    .setRequestId(place.placeID ?: "PlaceID ERROR")
                    .setCircularRegion(
                        place.latitude ?: 10.0,
                        place.longitude ?: 14.0,
                        10f
                    )
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                    .build()
            )
        }
        Companion.allPlaces = allPlaces.value
        geofenceList.postValue(tempGeofenceList)
    }

    fun getGeofencingRequest(): GeofencingRequest {
        return GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_DWELL or GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofences(geofenceList.value!!)
        }.build()
    }

    internal companion object {
        val geofenceList = MutableLiveData<ArrayList<Geofence>>()

        var allPlaces: List<PlaceEntity>? = null
    }
}