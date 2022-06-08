package com.lukic.conseo

import android.app.Application
import android.content.Context
import com.conseo.database.databaseModule
import com.google.firebase.messaging.FirebaseMessaging
import com.lukic.restapi.firebase.retrofitModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MyApplication : Application() {

    init {
        instance = this
    }

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MyApplication)
            modules(
                listOf(
                    databaseModule,
                    repositoryModule,
                    viewModelModules,
                    utilsModules,
                    retrofitModules
                )
            )
        }

        FirebaseMessaging.getInstance().isAutoInitEnabled = true
    }

    companion object {
        private var instance: MyApplication? = null

        fun getInstance(): MyApplication = instance ?: MyApplication()
        fun getAppContext(): Context {
            return instance?.applicationContext!!
        }
    }
}

