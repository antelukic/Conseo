package com.lukic.conseo.places.viewmodels

import android.annotation.SuppressLint
import android.location.Location
import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.conseo.database.entity.PlaceEntity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.lukic.conseo.MyApplication
import com.lukic.conseo.places.model.PlacesRepository
import com.lukic.conseo.utils.AppPreferences
import com.lukic.conseo.utils.AppPrefs
import com.lukic.conseo.utils.awaitTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.qualifier.named
import org.koin.core.qualifier.qualifier
import org.koin.java.KoinJavaComponent.inject

private const val TAG = "PlaceViewModel"

class PlaceViewModel(
    private val placesRepository: PlacesRepository,
) : ViewModel() {

    private val appPrefs: AppPreferences by inject(
        qualifier = named("SharedPreferences"),
        clazz = AppPrefs::class.java
    )

    private val _adapterData = MutableLiveData<List<PlaceEntity>>()
    val adapterData get() = _adapterData as LiveData<List<PlaceEntity>>

    private val _userLatLng = MutableLiveData<LatLng?>()
    val userLatLng get() = _userLatLng as LiveData<LatLng?>

    private val client: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(
            MyApplication.getAppContext()
        )
    }

    val loaderVisibility = MutableLiveData(View.GONE)

    fun getAllItemsByService(serviceName: String) {
        viewModelScope.launch {
            try {
                loaderVisibility.postValue(View.VISIBLE)
                val querySnapshot =
                    placesRepository.getAllItemsByService(serviceName).awaitTask(viewModelScope)
                if (querySnapshot != null) {
                    val services = querySnapshot.toObjects(PlaceEntity::class.java)
                    val filteredPlaces = filterPlacesByLocationDistance(services)
                    _adapterData.postValue(filteredPlaces)
                    loaderVisibility.postValue(View.GONE)
                } else {
                    loaderVisibility.postValue(View.GONE)
                    Log.e(TAG, "getAllItemsByService: querySnapshot is null")
                }
            } catch (e: Exception) {
                loaderVisibility.postValue(View.GONE)
                Log.e("PlaceViewModel", e.message.toString())
            }
        }
    }

    private fun filterPlacesByLocationDistance(services: List<PlaceEntity>): List<PlaceEntity> {
        return services.filter { place -> getPlacesWithinRange(place) }
    }

    private fun getPlacesWithinRange(place: PlaceEntity): Boolean {
        return if (userLatLng.value != null) {
            val userLocation = Location("userLocation").apply {
                latitude = userLatLng.value!!.latitude
                longitude = userLatLng.value!!.longitude
            }

            val placeLoc = Location("temp").apply {
                latitude = place.latitude ?: 0.0
                longitude = place.longitude ?: 0.0
            }
            val distance = userLocation.distanceTo(placeLoc)
            distance.toInt() <= getDistanceFromPrefs()
        } else
            false
    }

    private fun getDistanceFromPrefs(): Int {
        return appPrefs.getInt(AppPrefs.DISTANCE_KEY) * 1000
    }


    @SuppressLint("MissingPermission")
    fun getUserLocation() {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                val location = client.lastLocation.awaitTask(viewModelScope)
                if (location != null) {
                    val lat = location.latitude
                    val lng = location.longitude
                    _userLatLng.postValue(LatLng(lat, lng))
                } else
                    _userLatLng.postValue(null)
            } catch (e: Exception) {
                _userLatLng.postValue(null)
                Log.e(TAG, "getUserLocation: ${e.message}")
            }
        }
    }
}