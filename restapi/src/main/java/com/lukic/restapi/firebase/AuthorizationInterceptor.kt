package com.lukic.restapi.firebase

import okhttp3.Interceptor
import okhttp3.Response

class AuthorizationInterceptor(private val fcmKey: String): Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val builder = chain.request().newBuilder()
        builder.addHeader("Authorization", "key=${fcmKey}")
        builder.addHeader("Content-Type", "application/json")

        return chain.proceed(builder.build())
    }
}