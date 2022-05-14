package com.lukic.conseo.places.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.conseo.database.entity.PlaceEntity
import com.lukic.conseo.places.model.PlacesRepository
import kotlinx.coroutines.launch

private const val TAG = "SingleServiceViewModel"
class PlaceViewModel (
    private val placesRepository: PlacesRepository
    ): ViewModel() {

    val adapterData = MutableLiveData<List<PlaceEntity>>()


    fun getAllItemsByService(serviceName: String) {
        viewModelScope.launch {
            try {
                val result = placesRepository.getAllItemsByService(serviceName)
                result.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val services = task.result.toObjects(PlaceEntity::class.java)
                        adapterData.postValue(services as List<PlaceEntity>)
                        Log.d(TAG, adapterData.toString())
                    }
                    Log.d(TAG, result.toString())
                }
            } catch(e: Exception){
                Log.e(TAG, e.message.toString())
            }
        }
    }
}