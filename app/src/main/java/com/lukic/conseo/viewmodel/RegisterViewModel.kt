package com.lukic.conseo.viewmodel

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.conseo.database.entity.UserEntity
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.lukic.conseo.MyApplication
import com.lukic.conseo.R
import com.lukic.conseo.repository.AppRepository
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.IOException

sealed class RegisterError(val message: String){
    object PasswordsDontMatch: RegisterError("Passwords don't match")
    object InvalidEmail: RegisterError("Invalid email")
    object EmptyInput: RegisterError("This field is required to proceed")
}
private const val TAG = "RegisterViewModel"
class RegisterViewModel(
    private val appRepository: AppRepository
): ViewModel() {

    val isAccountSaved = MutableLiveData<Boolean>()

    val name = MutableLiveData<String>()
    val email = MutableLiveData<String>()
    val password = MutableLiveData<String>()
    val repeatPassword = MutableLiveData<String>()
    val error = MutableLiveData<RegisterError?>()
    val age = MutableLiveData<Int>(16)
    val gender = MutableLiveData<String>()
    val proceed = MutableLiveData<Boolean>()


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
        if (!email.value!!.contains("@")) {
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
        }
        return bitmap
    }

    fun registerAccount(imageBitmap: Bitmap?) = viewModelScope.launch {
        appRepository.registerAccount(email.value!!, password.value!!)
            .addOnSuccessListener {
                saveUserToDB(imageBitmap)
            }
            .addOnCanceledListener {
                isAccountSaved.postValue(false)
            }
            .addOnFailureListener{
                Log.e(TAG, it.message.toString())
                isAccountSaved.postValue(false)
            }

    }

    private fun saveUserToDB(imageBitmap: Bitmap?) {
        viewModelScope.launch(Dispatchers.IO) {

            val imageUrl = if(imageBitmap == null) {
                val image = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
                storeImageToStorage(image)
            } else
                storeImageToStorage(imageBitmap)
            imageUrl.await()
                .addOnSuccessListener { taskSnapshot ->
                    Log.d(TAG, imageUrl.toString())
                    try {
                        val response = appRepository.saveUserToDB(
                            UserEntity(
                                name = name.value!!,
                                email = email.value!!,
                                password = password.value!!,
                                image = taskSnapshot.storage.downloadUrl.toString(),
                                age = age.value!!,
                                gender = gender.value!!
                            )
                        )
                        response.addOnSuccessListener {
                            isAccountSaved.postValue(true)
                        }
                        response.addOnFailureListener {
                            Log.e(TAG, it.message.toString())
                            isAccountSaved.postValue(false)
                        }
                        response.addOnCanceledListener {
                            isAccountSaved.postValue(false)
                        }
                    } catch (e: Exception) {
                        isAccountSaved.postValue(false)
                        Log.e(TAG, e.message.toString())
                    }
                }
                .addOnCanceledListener {
                    isAccountSaved.postValue(false)
                }
                .addOnFailureListener{
                    Log.e(TAG, it.message.toString())
                    isAccountSaved.postValue(false)
                }
        }
    }

        private fun storeImageToStorage(imageBitmap: Bitmap): Deferred<StorageTask<UploadTask.TaskSnapshot>> =
            viewModelScope.async {
                val baos = ByteArrayOutputStream()
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val data = baos.toByteArray()
                return@async appRepository.storeImageToStorage(data, email.value!!)
            }

}
