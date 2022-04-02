package com.lukic.conseo.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.conseo.database.entity.SingleServiceEntity
import com.lukic.conseo.repository.AppRepository
import kotlinx.coroutines.launch

private const val TAG = "SingleServiceViewModel"
class SingleServiceViewModel (
    private val appRepository: AppRepository
    ): ViewModel() {

    val _adapterData = MutableLiveData<List<SingleServiceEntity>>()


    fun getAllItemsByService(service: String) {
        viewModelScope.launch {
            try {
                val result = appRepository.getAllItemsByService(service)
                result.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val services = task.result.toObjects(SingleServiceEntity::class.java)
                        _adapterData.postValue(services as List<SingleServiceEntity>)
                    }
                }
            } catch(e: Exception){
                Log.e(TAG, e.message.toString())
            }
        }
    }
}