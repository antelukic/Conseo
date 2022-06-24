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
import com.lukic.conseo.places.model.PlacesRepository
import com.lukic.conseo.utils.awaitTask
import com.lukic.restapi.firebase.models.NotificationData
import com.lukic.restapi.firebase.models.PushNotification
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

    val proceed = MutableLiveData<Boolean?>()
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

    fun addPlace(location: String) {
        try {
            viewModelScope.launch(Dispatchers.IO) {
                val place = getPlace(location)
                val imageUrl = storeImageToStorage(place)
                if (imageUrl != null) {
                    place.image = imageUrl.toString()
                    val document =
                        placesRepository.storePlace(place = place).awaitTask(viewModelScope)
                    if (document != null)
                        sendNotification(place)
                    else
                        proceed.postValue(false)
                } else
                    proceed.postValue(false)

            }
        } catch (e: Exception) {
            proceed.postValue(false)
            Log.e(TAG, e.message.toString())
        }
    }

    private suspend fun sendNotification(place: PlaceEntity){
        val response = placesRepository.sendPlaceAddedNotification(
            PushNotification(
                data = NotificationData(
                    title = "${name.value} just added",
                    message = "Be the first to visit",
                    senderID = place.placeID ?: UUID.randomUUID().toString()
                ),
                to = place.serviceName.toString()
            )
        )
        if(response.isSuccessful){
            Log.d(TAG, "sendNotification: ${place.serviceName}")
            proceed.postValue(true)
        } else {
            Log.e(TAG, "sendNotification: ${response.raw()}", )
        }
    }

    private suspend fun storeImageToStorage(place: PlaceEntity): Uri? {
        val baos = ByteArrayOutputStream()
        imageBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()
        return placesRepository.storeServiceImageToStorage(
            place = place,
            imageByteArray = data
        )
    }

    private fun getPlace(location: String): PlaceEntity {
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

        return SimpleDateFormat("dd/MM/yyyy").format(calendar.time)
    }

    private fun getTimeFromTimePicker(): String {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hour.value!!)
        calendar.set(Calendar.MINUTE, minute.value!!)

        return SimpleDateFormat("HH:mm").format(calendar.time)
    }

    fun deleteValues() {
        viewModelScope.launch(Dispatchers.Default) {
            name.postValue(null)
            additionalInfo.postValue(null)
            selectedItemPosition.postValue(null)
            hour.postValue(null)
            minute.postValue(null)
            year.postValue(null)
            month.postValue(null)
            day.postValue(null)
            location.postValue(null)
            searchText.postValue(null)
            latlng = null
            proceed.postValue(null)
            imageBitmap = null
        }
    }

}