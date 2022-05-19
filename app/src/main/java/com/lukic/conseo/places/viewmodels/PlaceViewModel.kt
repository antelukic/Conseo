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
import kotlinx.coroutines.launch

private const val TAG = "SingleServiceViewModel"

class PlaceViewModel(
    private val placesRepository: PlacesRepository
) : ViewModel() {

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
                        getPlacesWithinRange(services)
                        _adapterData.postValue(services as List<PlaceEntity>)
                    }
                    Log.d(TAG, result.toString())
                }
            } catch (e: Exception) {
                Log.e(TAG, e.message.toString())
            }
        }
    }

    private fun getPlacesWithinRange(services: List<PlaceEntity>) {
        if(userLatLng.value != null) {
            val userLocation = Location("userLocation").apply {
                latitude = userLatLng.value!!.latitude
                longitude = userLatLng.value!!.longitude
            }
            services.forEach { place ->
                val placeLoc = Location("temp").apply {
                    latitude = place.latitude ?: 0.0
                    longitude = place.logitude ?: 0.0
                }
                val distance = userLocation.distanceTo(placeLoc)
                Log.d(TAG, "getPlacesWithinRange: distance $distance")
            }
        }
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