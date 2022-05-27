package com.lukic.restapi.firebase

import com.lukic.restapi.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitInstance {

    private val loggingInterceptor by lazy { HttpLoggingInterceptor().also { it.level = HttpLoggingInterceptor.Level.BODY } }

    private val client by lazy { OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build() }

    private val retrofit by lazy {
        Retrofit.Builder()
            .client(client)
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api by lazy {
        retrofit.create(NotificationAPI::class.java)
    }

    internal companion object{

        private const val BASE_URL = "https://fcm.googleapis.com"
        const val SERVER_KEY = "AAAASt8ul4A:APA91bG0Zkis-pHZbdMXztbhmkYO33-ORIJD-Xtn8k-vVvW4q5fmRBZRO2rCtXO9Z1SSBQ5d_Cn1ZLw5caKB6LyungnNViWfKihzPveKlgx0NpWVOO2RvNm31ok7EiD8oe_118p0EStJ "
         const val CONTENT_TYPE = "application/json"
    }
}