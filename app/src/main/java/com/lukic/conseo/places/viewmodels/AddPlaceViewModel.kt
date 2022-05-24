package com.lukic.conseo.places.viewmodels

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.conseo.database.entity.PlaceEntity
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.UploadTask
import com.lukic.conseo.places.model.PlacesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


private const val TAG = "AddServiceViewModel"

class AddPlaceViewModel(
    private val placesRepository: PlacesRepository
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

    val searchText = MutableLiveData<String>()


    var latlng: LatLng? = null

    val proceed = MutableLiveData<Boolean>()
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
                                placesRepository.storeService(place = service)
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

    private fun storeImageToStorage(place: PlaceEntity): UploadTask {
        val baos = ByteArrayOutputStream()
        imageBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()
        return placesRepository.storeServiceImageToStorage(
            place = place,
            imageByteArray = data
        )
    }

    private fun getService(location: String): PlaceEntity {
        return when (selectedItemPosition.value) {
            1 -> {
                PlaceEntity(
                    creatorID = FirebaseAuth.getInstance().currentUser?.uid.toString(),
                    serviceName = "restaurants",
                    name = name.value,
                    location = location,
                    info = additionalInfo.value,
                    image = null,
                    placeID = UUID.randomUUID().toString(),
                    latitude = latlng?.latitude ?: 0.0,
                    longitude = latlng?.longitude ?: 0.0
                )
            }
            2 -> {
                PlaceEntity(
                    creatorID = FirebaseAuth.getInstance().currentUser?.uid.toString(),
                    serviceName = "events",
                    name = name.value,
                    location = location,
                    info = additionalInfo.value,
                    image = null,
                    date = getDateFromPicker(),
                    time = getTimeFromTimePicker(),
                    placeID = UUID.randomUUID().toString(),
                    latitude = latlng?.latitude ?: 0.0,
                    longitude = latlng?.longitude ?: 0.0
                )
            }
            else -> {
                PlaceEntity(
                    creatorID = FirebaseAuth.getInstance().currentUser?.uid.toString(),
                    serviceName = "bars",
                    name = name.value,
                    location = location,
                    info = additionalInfo.value,
                    image = null,
                    placeID = UUID.randomUUID().toString(),
                    latitude = latlng?.latitude ?: 0.0,
                    longitude = latlng?.longitude ?: 0.0
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