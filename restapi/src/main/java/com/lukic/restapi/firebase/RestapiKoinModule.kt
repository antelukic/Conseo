package com.lukic.restapi.firebase

import org.koin.dsl.module

val retrofitModules = module {
    single { RetrofitInstance() }
}