package com.lukic.restapi.firebase

import org.koin.dsl.module

fun retrofitModules(fcmKey: String, baseUrl: String) = module {
    single { AuthorizationInterceptor(fcmKey) }
    single { RetrofitInstance(baseUrl, get()) }
}