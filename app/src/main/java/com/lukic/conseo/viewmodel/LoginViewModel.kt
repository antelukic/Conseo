package com.lukic.conseo.viewmodel

import android.content.Context
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseException
import com.lukic.conseo.MyApplication
import com.lukic.conseo.R
import com.lukic.conseo.repository.AppRepository
import kotlinx.coroutines.launch

sealed class LoginError(val message: String){
    object EmailOrPasswordNotValid: LoginError("Email or password is not correct")
    object SomethingWentWrong: LoginError("Something went wrong. Please try again")
}

private const val TAG = "LoginViewModel"
class LoginViewModel(
    private val appRepository: AppRepository
): ViewModel() {

    val email = MutableLiveData<String>()
    val password = MutableLiveData<String>()
    val biometricsEnabled = MutableLiveData<Boolean?>()
    val proceed = MutableLiveData<Boolean>()

    var error = MutableLiveData<LoginError?>()

    init {
        isBiometricEnabled()
    }

    private fun validatePassword(): Boolean {

        if(password.value?.length == null || password.value!!.length < 6) {
            Log.d(TAG, "1")
            return false
        }
        if(email.value == null || !email.value!!.contains("@")) {
            Log.d(TAG, "2")
            return false
        }
        return true
    }

    fun loginUser(){
        viewModelScope.launch {
            if (validatePassword()) {
                try {
                    val result = appRepository.loginUser(email.value!!.trim(), password.value!!.trim())
                    result.addOnSuccessListener {
                        Log.d(TAG, "1 " +result.result.user.toString())
                        proceed.postValue(true)
                    }
                        .addOnFailureListener {
                            Log.d(TAG, "12 " +result.result.user.toString())
                            error.postValue(LoginError.SomethingWentWrong)
                        }
                        .addOnCanceledListener {
                            Log.d(TAG, "13 " +result.result.user.toString())
                            error.postValue(LoginError.SomethingWentWrong)
                        }
                } catch (e: FirebaseException) {
                    error.postValue(LoginError.EmailOrPasswordNotValid)
                }
            } else {
                error.postValue(LoginError.EmailOrPasswordNotValid)
            }
        }
    }

    fun loginUsingBiometrics(){
        val prefs = MyApplication.getAppContext().getSharedPreferences(
            MyApplication.getAppContext().getString(R.string.email_and_password),
            Context.MODE_PRIVATE
        )
        email.postValue(prefs.getString(
            MyApplication.getAppContext().getString(R.string.email), ""
        ))
        password.postValue(
        prefs.getString(
            MyApplication.getAppContext().getString(R.string.password), ""
        ))
        if(!email.value.isNullOrEmpty() && !password.value.isNullOrEmpty())
            loginUser()
    }

    private fun isBiometricEnabled(){
        viewModelScope.launch {
            if(BiometricManager.from(MyApplication.getAppContext()).canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS) {
                val prefs = MyApplication.getAppContext().getSharedPreferences(MyApplication.getAppContext().getString(R.string.email_and_password),
                    Context.MODE_PRIVATE
                )
                val isEnabled = prefs.getString(
                    MyApplication.getAppContext().getString(R.string.email), null
                )
                Log.d(TAG, isEnabled.toString())
                biometricsEnabled.postValue(isEnabled != null)
            } else {
                biometricsEnabled.postValue(null)
            }
        }
    }


    fun clearError(){
        error.value = null
    }

    fun allowBiometricAuthentication() {
        val prefs = MyApplication.getAppContext().getSharedPreferences(
            MyApplication.getAppContext().getString(R.string.email_and_password),
            Context.MODE_PRIVATE
        )
        Log.d(TAG, email.value.toString())
        Log.d(TAG, password.value.toString())
        prefs.edit().putString(
            MyApplication.getAppContext().getString(R.string.email), email.value!!
        ).apply()
        prefs.edit().putString(
            MyApplication.getAppContext().getString(R.string.password), password.value!!
        ).apply()
    }
}