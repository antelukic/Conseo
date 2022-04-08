package com.lukic.conseo.viewmodel

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.conseo.database.entity.ServiceEntity
import com.google.firebase.storage.UploadTask
import com.lukic.conseo.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


private const val TAG = "AddServiceViewModel"

class AddServiceViewModel(
    private val appRepository: AppRepository
) : ViewModel() {

    val name = MutableLiveData<String>()
    val additionalInfo = MutableLiveData<String>()
    val selectedItemPosition = MutableLiveData<Int>()
    val hour = MutableLiveData<Int>()
    val minute = MutableLiveData<Int>()
    val year = MutableLiveData<Int>()
    val month = MutableLiveData<Int>()
    val day = MutableLiveData<Int>()
    val location = MutableLiveData<String>()

    val proceed = MutableLiveData(false)
    var imageBitmap: Bitmap? = null


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
            Log.d(TAG, e.message.toString())
        }
        imageBitmap = bitmap
        return bitmap
    }

    fun addService(location: String) {
        try {
            viewModelScope.launch(Dispatchers.IO) {
                val service = getService(location)
                Log.d(TAG, "imageBitmap" + imageBitmap.toString())
                val imageTask = storeImageToStorage(service)
                Log.d(TAG, "service $service")
                imageTask.addOnCompleteListener { uploadImageTask ->
                    if (uploadImageTask.isSuccessful) {
                        uploadImageTask.result.storage.downloadUrl.addOnCompleteListener { downloadUrlTask ->
                            if(downloadUrlTask.isSuccessful) {
                                service.image = downloadUrlTask.result.toString()
                                appRepository.storeService(service = service)
                                    .addOnCompleteListener { uploadServiceTask ->
                                        if (uploadServiceTask.isSuccessful) {
                                            proceed.postValue(true)
                                        } else
                                            proceed.postValue(false)
                                    }
                            } else {
                                proceed.postValue(false)
                            }
                        }
                    } else
                        proceed.postValue(false)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, e.message.toString())
        }
    }

    private fun storeImageToStorage(service: ServiceEntity): UploadTask {
        val baos = ByteArrayOutputStream()
        imageBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()
        return appRepository.storeServiceImageToStorage(
            service = service,
            imageByteArray = data
        )
    }

    private fun getService(location: String): ServiceEntity {
        return when (selectedItemPosition.value) {
            1 -> {
                ServiceEntity(
                    serviceName = "restaurants",
                    name = name.value,
                    location = location,
                    info = additionalInfo.value,
                    image = null
                )
            }
            2 -> {
                ServiceEntity(
                    serviceName = "events",
                    name = name.value,
                    location = location,
                    info = additionalInfo.value,
                    image = null,
                    date = getDateFromPicker(),
                    time = getTimeFromTimePicker()
                )
            }
            else -> {
                ServiceEntity(
                    serviceName = "bars",
                    name = name.value,
                    location = location,
                    info = additionalInfo.value,
                    image = null
                )
            }
        }
    }

    private fun getDateFromPicker(): String {
        val calendar = Calendar.getInstance()
        calendar.set(year.value!!, month.value!!, day.value!!)
        return calendar.toString()
    }

    private fun getTimeFromTimePicker(): String {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hour.value!!)
        calendar.set(Calendar.MINUTE, minute.value!!)

        return SimpleDateFormat("HH:mm").format(calendar.time)
    }

}