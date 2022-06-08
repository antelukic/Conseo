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
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.lukic.conseo.loginregister.model.LoginRegisterRepository
import com.lukic.conseo.MyApplication
import com.lukic.conseo.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.util.regex.Pattern

sealed class RegisterError(val message: String) {
    object PasswordsDontMatch : RegisterError("Passwords don't match")
    object InvalidEmail : RegisterError("Invalid email")
    object EmptyInput : RegisterError("This field is required to proceed")
    object PasswordError :
        RegisterError("Invalid Password. Password must contain number, special character and letter.")
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
    val loaderVisibility = MutableLiveData<Int>(View.GONE)
    private var userID = ""


    fun onProceedClicked() {
        viewModelScope.launch(Dispatchers.Default) {
            val valid = checkInputs()
            Log.d(TAG, "onProceedClicked: valid $valid")
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
        loaderVisibility.postValue(View.VISIBLE)
        if (isValidEmail()) {
            loginRegisterRepository.registerAccount(email.value!!.trim(), password.value!!)
                .addOnCompleteListener { authResult ->
                    if (authResult.isSuccessful) {
                        userID = authResult.result.user?.uid.toString()
                        saveUserToDB(imageBitmap)
                    } else {
                        isAccountSaved.postValue(false)
                        loaderVisibility.postValue(View.GONE)
                        Log.e(
                            TAG,
                            "registerAccount: ${authResult.exception?.message.toString()}"
                        )
                    }
                }
        } else {
            Log.e(TAG, "registerAccount: Email ERROR")
        }
    }

    private fun isValidEmail(): Boolean {
        Log.d(TAG, "isValidEmail: ${!TextUtils.isEmpty(email.value)}")
        Log.d(
            TAG,
            "isValidEmail: ${
                android.util.Patterns.EMAIL_ADDRESS.matcher(email.value.toString()).matches()
            }"
        )
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
            imageUrl.addOnCompleteListener { taskSnapshot ->
                if (taskSnapshot.isSuccessful) {
                    getDownloadUrl(taskSnapshot)
                } else {
                    loaderVisibility.postValue(View.GONE)
                    Log.e(TAG, "saveUserToDB: ${taskSnapshot.exception?.message.toString()}")
                }
            }
        }
    }

    private fun getDownloadUrl(taskSnapshot: Task<UploadTask.TaskSnapshot>) {
        if (taskSnapshot.isSuccessful) {
            val user = UserEntity(
                id = userID,
                name = name.value!!,
                email = email.value!!.trim(),
                password = password.value!!.trim(),
                image = null,
                age = age.value!!,
                gender = gender.value!!,
                token = getTokenFromPrefs()
            )
            taskSnapshot.result.storage.downloadUrl.addOnCompleteListener { downloadUrlTaskResult ->
                if (downloadUrlTaskResult.isSuccessful) {
                    user.image = downloadUrlTaskResult.result.toString()
                    saveUserToDB(user = user)
                } else {
                    loaderVisibility.postValue(View.GONE)
                    isAccountSaved.postValue(false)
                    Log.e(
                        TAG,
                        "saveUserToDB: ${downloadUrlTaskResult.exception?.message.toString()}",
                    )
                }
            }
        } else {
            isAccountSaved.postValue(false)
            Log.e(TAG, "getDownloadUrl: ${taskSnapshot.exception?.message.toString()}")
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

    private fun saveUserToDB(user: UserEntity) {
        if (!user.token.isNullOrEmpty()) {
            loginRegisterRepository.saveUserToDB(
                userEntity = user
            ).addOnCompleteListener { saveUserTaskResult ->
                loaderVisibility.postValue(View.GONE)
                if (saveUserTaskResult.isSuccessful)
                    isAccountSaved.postValue(true)
                else {
                    isAccountSaved.postValue(false)
                    Log.e(
                        TAG,
                        "saveUserToDB: ${saveUserTaskResult.exception?.message.toString()}",
                    )
                }
            }
        }
    }

    private fun storeImageToStorage(imageBitmap: Bitmap): StorageTask<UploadTask.TaskSnapshot> {
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
