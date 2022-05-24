package com.lukic.conseo.geofencing

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.conseo.database.entity.PlaceEntity
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.lukic.conseo.utils.AppPrefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "GeofencingViewModel"

class GeofencingViewModel(
    private val geofencingRepository: GeofencingRepository,
    private val appPrefs: AppPrefs
) : ViewModel() {

    private val _allPlaces = MutableLiveData<MutableList<PlaceEntity>?>()
    val allPlaces get() = _allPlaces as LiveData<MutableList<PlaceEntity>?>


    fun getAllPlaces() {
        viewModelScope.launch(Dispatchers.IO) {
            geofencingRepository.getAllBars()
                .addOnCompleteListener { getAllPlacesTask ->
                    if (getAllPlacesTask.isSuccessful) {
                        val bars =
                            getAllPlacesTask.result.toObjects(PlaceEntity::class.java)
                        _allPlaces.postValue(bars)
                        geofencingRepository.getAllEvents()
                            .addOnCompleteListener { getAllEventsTask ->
                                if (getAllEventsTask.isSuccessful) {
                                    val events =
                                        getAllEventsTask.result.toObjects(PlaceEntity::class.java)
                                    _allPlaces.postValue(events)
                                    geofencingRepository.getAllRestaurants()
                                        .addOnCompleteListener { getAllRestaurantsTask ->
                                            if (getAllRestaurantsTask.isSuccessful) {
                                                val restaurants =
                                                    getAllRestaurantsTask.result.toObjects(
                                                        PlaceEntity::class.java
                                                    )
                                                _allPlaces.postValue(restaurants)
                                            } else {
                                                Log.e(
                                                    TAG,
                                                    "getAllPlaces: ${getAllRestaurantsTask.exception?.message}",
                                                )
                                            }

                                        }
                                } else {
                                    Log.e(
                                        TAG,
                                        "getAllPlaces: ${getAllEventsTask.exception?.message}",
                                    )
                                }
                            }
                    } else {
                        _allPlaces.postValue(null)
                        Log.e(TAG, "getAllPlaces: ERROR ${getAllPlacesTask.exception?.message}")
                    }
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
                        getUserDistanceInMeters().toFloat()
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
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofences(geofenceList.value!!)
        }.build()
    }


    private fun getUserDistanceInMeters(): Int {
        val distance = appPrefs.getInt(AppPrefs.distanceKey)
        return if(distance == 0)
            1
        else
            distance
    }

    internal companion object {
        val geofenceList = MutableLiveData<ArrayList<Geofence>>()

        var allPlaces: List<PlaceEntity>? = null
    }
}