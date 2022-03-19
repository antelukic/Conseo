package com.lukic.conseo

import android.app.Application
import android.content.Context
import com.conseo.database.databaseModule
import org.koin.core.context.startKoin

class MyApplication: Application() {

    init{
        instance = this
    }

    override fun onCreate() {
        super.onCreate()
        startKoin{
            modules(
                listOf(
                    databaseModule,
                    repositoryModule,
                    viewModelModules
                )
            )
        }
    }

    companion object {
        private var instance: MyApplication? = null

        fun getAppContext(): Context {
            return instance?.applicationContext!!
        }
    }
}