package com.lukic.restapi.firebase

import com.lukic.restapi.firebase.models.PushNotification
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface NotificationAPI {

    @POST("fcm/send")
    suspend fun postNotification(
        @Body notification: PushNotification
    ): Response<ResponseBody>

}