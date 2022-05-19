package com.lukic.conseo

import android.app.Application
import android.content.Context
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.conseo.database.databaseModule
import org.koin.core.context.startKoin

class MyApplication : Application() {

    init {
        instance = this
    }

    override fun onCreate() {
        super.onCreate()
        startKoin {
            modules(
                listOf(
                    databaseModule,
                    repositoryModule,
                    viewModelModules,
                    utilsModule
                )
            )
        }
    }

    companion object {
        private var instance: MyApplication? = null

        fun getInstance(): MyApplication = instance ?: MyApplication()
        fun getAppContext(): Context {
            return instance?.applicationContext!!
        }
    }
}

