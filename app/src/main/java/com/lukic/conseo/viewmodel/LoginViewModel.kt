package com.lukic.conseo.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

sealed class LoginError{
    object EmailOrPasswordNotValid: Error("Email or password is not correct")
}

class LoginViewModel: ViewModel() {

    var email: String = ""
    var password: String = ""

    var _error = MutableLiveData<Error?>()

    fun clearError(){
        _error.value = null
    }
}