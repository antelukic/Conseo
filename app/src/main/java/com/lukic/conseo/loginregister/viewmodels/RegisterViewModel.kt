package com.lukic.conseo.loginregister.viewmodels

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.conseo.database.entity.UserEntity
import com.lukic.conseo.loginregister.model.LoginRegisterRepository
import com.lukic.conseo.MyApplication
import com.lukic.conseo.R
import com.lukic.conseo.utils.awaitTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.IOException

sealed class RegisterError(val message: String) {
    object PasswordsDontMatch : RegisterError("Passwords don't match")
    object InvalidEmail : RegisterError("Invalid email")
    object EmptyInput : RegisterError("This field is required to proceed")
}

private const val TAG = "RegisterViewModel"

class RegisterViewModel(
    private val loginRegisterRepository: LoginRegisterRepository
) : ViewModel() {

    val isAccountSaved = MutableLiveData<Boolean>()

    val name = MutableLiveData<String>()
    val email = MutableLiveData<String>()
    val password = MutableLiveData<String>()
    val repeatPassword = MutableLiveData<String>()
    val error = MutableLiveData<RegisterError?>()
    val age = MutableLiveData(16)
    val gender = MutableLiveData<String>()
    val proceed = MutableLiveData<Boolean>()
    val loaderVisibility = MutableLiveData(View.GONE)
    private var userID = ""


    fun onProceedClicked() {
        viewModelScope.launch(Dispatchers.Default) {
            val valid = checkInputs()
            if (valid)
                proceed.postValue(true)
        }
    }

    private fun checkInputs(): Boolean {

        if (name.value.isNullOrEmpty()) {
            error.postValue(RegisterError.EmptyInput)
            return false
        }
        if (email.value.isNullOrEmpty()) {
            error.postValue(RegisterError.EmptyInput)
            return false
        }
        if (password.value.isNullOrEmpty()) {
            error.postValue(RegisterError.EmptyInput)
            return false
        }
        if (repeatPassword.value.isNullOrEmpty()) {
            error.postValue(RegisterError.EmptyInput)
            return false
        }
        if (password.value != repeatPassword.value) {
            error.postValue(RegisterError.PasswordsDontMatch)
            return false
        }
        if (gender.value.isNullOrEmpty()) {
            error.postValue(RegisterError.EmptyInput)
            return false
        }
        if (!isValidEmail()) {
            error.postValue(RegisterError.InvalidEmail)
            return false
        }

        return true
    }

    fun clearError() {
        error.value = null
    }


    fun getBitmap(file: Uri, cr: ContentResolver): Bitmap? {
        var bitmap: Bitmap? = null
        try {
            val inputStream = cr.openInputStream(file)
            bitmap = BitmapFactory.decodeStream(inputStream)

            try {
                inputStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        } catch (e: FileNotFoundException) {
            Log.e(TAG, "getBitmap: ${e.message}")
        }
        return bitmap
    }

    fun registerAccount(imageBitmap: Bitmap?) = viewModelScope.launch {
        try {
            loaderVisibility.postValue(View.VISIBLE)
            if (isValidEmail()) {
                val isRegistered =
                    loginRegisterRepository.registerAccount(email.value!!.trim(), password.value!!)
                        .awaitTask(viewModelScope)
                if (isRegistered?.user != null) {
                    saveUserToDB(imageBitmap)
                } else {
                    isAccountSaved.postValue(false)
                    loaderVisibility.postValue(View.GONE)
                }
            }
        }catch (e: Exception){
            Log.e(TAG, "registerAccount: ${e.message}")
            isAccountSaved.postValue(false)
            loaderVisibility.postValue(View.GONE)
        }
    }


    private fun isValidEmail(): Boolean {
        return !TextUtils.isEmpty(email.value) && android.util.Patterns.EMAIL_ADDRESS.matcher(
            email.value.toString()
        ).matches()
    }

    private fun saveUserToDB(imageBitmap: Bitmap?) {
        viewModelScope.launch(Dispatchers.IO) {

            val imageUrl = if (imageBitmap == null) {
                val image = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
                storeImageToStorage(image)
            } else
                storeImageToStorage(imageBitmap)
            if (imageUrl != null) {
                val user = UserEntity(
                    id = userID,
                    name = name.value!!,
                    email = email.value!!.trim(),
                    password = password.value!!.trim(),
                    image = imageUrl.toString(),
                    age = age.value!!,
                    gender = gender.value!!,
                    token = getTokenFromPrefs()
                )
                saveUserToDB(user = user)
            } else {
                loaderVisibility.postValue(View.GONE)
                Log.e(TAG, "saveUserToDB: download url is null")
            }
        }
    }

    private fun getTokenFromPrefs(): String? {
        val prefs = MyApplication.getAppContext().getSharedPreferences(
            MyApplication.getAppContext().getString(R.string.token),
            Context.MODE_PRIVATE
        )
        return prefs.getString(
            MyApplication.getAppContext().getString(R.string.token_key),
            null
        )
    }

    private suspend fun saveUserToDB(user: UserEntity) {
        try {
            if (!user.token.isNullOrEmpty()) {
                val isSaved = loginRegisterRepository.saveUserToDB(
                    userEntity = user
                ).awaitTask(this.viewModelScope)
                loaderVisibility.postValue(View.GONE)
                isAccountSaved.postValue(isSaved != null)
            }
        } catch (e: Exception) {
            isAccountSaved.postValue(false)
            Log.e(TAG, "saveUserToDB: ERROR ${e.message}")
        }
    }


    private suspend fun storeImageToStorage(imageBitmap: Bitmap): Uri? {
        val baos = ByteArrayOutputStream()
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()
        return loginRegisterRepository.storeImageToStorage(data, email.value!!)
    }

    fun deleteValues() {
        name.postValue("")
        email.postValue("")
        password.postValue("")
        repeatPassword.postValue("")
        error.postValue(null)
        age.postValue(16)
        gender.postValue("")
    }

}

