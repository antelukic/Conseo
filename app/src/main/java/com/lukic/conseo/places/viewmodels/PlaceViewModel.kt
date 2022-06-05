package com.lukic.conseo.places.viewmodels

import android.annotation.SuppressLint
import android.location.Location
import android.util.Log
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
import kotlinx.coroutines.launch
import org.koin.core.qualifier.named
import org.koin.core.qualifier.qualifier
import org.koin.java.KoinJavaComponent.inject


class PlaceViewModel(
    private val placesRepository: PlacesRepository,
) : ViewModel() {

    private val appPrefs: AppPreferences by inject(qualifier = named("SharedPreferences"), clazz = AppPrefs::class.java)

    private val _adapterData = MutableLiveData<List<PlaceEntity>>()
    val adapterData get() = _adapterData as LiveData<List<PlaceEntity>>

    private val _userLatLng = MutableLiveData<LatLng?>()
    val userLatLng get() = _userLatLng as LiveData<LatLng?>

    private val client: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(
            MyApplication.getAppContext()
        )
    }


    fun getAllItemsByService(serviceName: String) {
        viewModelScope.launch {
            try {
                val result = placesRepository.getAllItemsByService(serviceName)
                result.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val services = task.result.toObjects(PlaceEntity::class.java)
                        val filteredPlaces = filterPlacesByLocationDistance(services)
                        _adapterData.postValue(filteredPlaces)
                    }
                }
            } catch (e: Exception) {
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
        client.lastLocation.addOnCompleteListener { task ->
            if (task.isSuccessful && task.result != null) {
                val lat = task.result.latitude
                val lng = task.result.longitude
                _userLatLng.postValue(LatLng(lat, lng))
            } else
                _userLatLng.postValue(null)
        }
    }
}