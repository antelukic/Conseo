package com.lukic.restapi.firebase

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitInstance(fcmUrl: String, val authorizationInterceptor: AuthorizationInterceptor) {

    private val loggingInterceptor by lazy { HttpLoggingInterceptor().also { it.level = HttpLoggingInterceptor.Level.BODY } }

    private val client by lazy { OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addNetworkInterceptor(authorizationInterceptor)
        .build() }

    private val retrofit by lazy {
        Retrofit.Builder()
            .client(client)
            .baseUrl(fcmUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api by lazy {
        retrofit.create(NotificationAPI::class.java)
    }

    internal companion object{

        const val CONTENT_TYPE = "application/json"
    }
}