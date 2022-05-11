package com.lukic.conseo.loginregister.viewmodels

import android.content.Context
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lukic.conseo.MyApplication
import com.lukic.conseo.R
import com.lukic.conseo.model.LoginRegisterRepository
import kotlinx.coroutines.launch

sealed class LoginError(val message: String){
    object EmailOrPasswordNotValid: LoginError("Email or password is not correct")
    object SomethingWentWrong: LoginError("Something went wrong. Please try again")
}

private const val TAG = "LoginViewModel"
class LoginViewModel(
    private val loginRegisterRepository: LoginRegisterRepository
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
            return false
        }
        if(email.value == null || !email.value!!.contains("@")) {
            return false
        }
        return true
    }

    fun loginUser(){
        viewModelScope.launch {
            if (validatePassword()) {
                loginRegisterRepository.loginUser(email.value!!.trim(), password.value!!.trim())
                        .addOnCompleteListener { result ->
                            if(result.isSuccessful)
                                proceed.postValue(true)
                            else {
                                if(result.isCanceled){
                                    Log.e(TAG, result.exception?.message.toString())
                                }else {
                                    proceed.postValue(false)
                                    error.postValue(LoginError.EmailOrPasswordNotValid)
                                }
                            }
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
        prefs.edit().putString(
            MyApplication.getAppContext().getString(R.string.email), email.value!!
        ).apply()
        prefs.edit().putString(
            MyApplication.getAppContext().getString(R.string.password), password.value!!
        ).apply()
    }
}