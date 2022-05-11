package com.lukic.conseo.places.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MapsViewModel(
): ViewModel() {

    val searchText = MutableLiveData<String>()

}