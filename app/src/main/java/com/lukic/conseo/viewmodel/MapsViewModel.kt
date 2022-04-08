package com.lukic.conseo.viewmodel

import android.widget.ArrayAdapter
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lukic.conseo.MyApplication
import com.lukic.conseo.R
import com.lukic.conseo.repository.AppRepository

class MapsViewModel(
    private val appRepository: AppRepository
): ViewModel() {

    val searchText = MutableLiveData<String>()

}