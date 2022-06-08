package com.lukic.conseo.loginregister.viewmodels

import android.util.Log
import android.view.View
import androidx.biometric.BiometricManager
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lukic.conseo.MyApplication
import com.lukic.conseo.R
import com.lukic.conseo.loginregister.model.LoginRegisterRepository
import com.lukic.conseo.utils.AppPreferences
import com.lukic.conseo.utils.EncryptedAppPrefs
import kotlinx.coroutines.launch
import org.koin.core.qualifier.named
import org.koin.java.KoinJavaComponent

sealed class LoginError(val message: String) {
    object EmailOrPasswordNotValid : LoginError("Email or password is not correct")
    object SomethingWentWrong : LoginError("Something went wrong. Please try again")
}

private const val TAG = "LoginViewModel"

class LoginViewModel(
    private val loginRegisterRepository: LoginRegisterRepository
) : ViewModel() {

    private val encPrefs: AppPreferences by KoinJavaComponent.inject(
        qualifier = named("EncryptedSharedPreferences"),
        clazz = EncryptedAppPrefs::class.java
    )

    val email = MutableLiveData<String>()
    val password = MutableLiveData<String>()
    val biometricsEnabled = MutableLiveData<Boolean?>()
    val proceed = MutableLiveData<Boolean>()
    val loaderVisibility = MutableLiveData<Int>(View.GONE)

    var error = MutableLiveData<LoginError?>()

    init {
        isBiometricEnabled()
    }

    private fun validatePassword(): Boolean {

        if (password.value?.length == null || password.value!!.length < 6) {
            return false
        }
        if (email.value == null || !email.value!!.contains("@")) {
            return false
        }
        return true
    }

    fun loginUser() {
        viewModelScope.launch {
            loaderVisibility.postValue(View.VISIBLE)
            if (validatePassword()) {
                loginRegisterRepository.loginUser(email.value!!.trim(), password.value!!.trim())
                    .addOnCompleteListener { result ->
                        loaderVisibility.postValue(View.GONE)
                        if (result.isSuccessful)
                            proceed.postValue(true)
                        else {
                            if (result.isCanceled) {
                                Log.e(TAG, result.exception?.message.toString())
                            } else {
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

    fun loginUsingBiometrics() {

        email.postValue(
            encPrefs.getString(
                MyApplication.getAppContext().getString(R.string.email)
            )
        )
        password.postValue(
            encPrefs.getString(
                MyApplication.getAppContext().getString(R.string.password)
            )
        )
        if (!email.value.isNullOrEmpty() && !password.value.isNullOrEmpty())
            loginUser()
    }

    private fun isBiometricEnabled() {
        viewModelScope.launch {
            if (BiometricManager.from(MyApplication.getAppContext())
                    .canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS
            ) {

                val isEnabled = encPrefs.getString(
                    MyApplication.getAppContext().getString(R.string.email)
                )
                biometricsEnabled.postValue(isEnabled != AppPreferences.STRING_DEF_VALUE)
            } else {
                biometricsEnabled.postValue(null)
            }
        }
    }


    fun clearError() {
        error.value = null
    }

    fun allowBiometricAuthentication() {
        encPrefs.putString(
            MyApplication.getAppContext().getString(R.string.email),
            email.value ?: AppPreferences.STRING_DEF_VALUE
        )
        encPrefs.putString(
            MyApplication.getAppContext().getString(R.string.password),
            password.value ?: AppPreferences.STRING_DEF_VALUE
        )
    }
}