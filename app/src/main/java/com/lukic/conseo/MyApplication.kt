package com.lukic.conseo

import android.app.Application
import android.content.Context
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.conseo.database.databaseModule
import com.google.firebase.messaging.FirebaseMessaging
import com.lukic.restapi.firebase.retrofitModules
import lv.chi.photopicker.ChiliPhotoPicker
import lv.chi.photopicker.loader.ImageLoader
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
                    retrofitModules(BuildConfig.FCM_KEY, BuildConfig.FCM_URL)
                )
            )
        }

        ChiliPhotoPicker.init(
            loader = GlideImageLoader(),
            authority = "com.lukic.conseo.fileprovider"
        )

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

internal class GlideImageLoader: ImageLoader {

    override fun loadImage(context: Context, view: ImageView, uri: Uri) {
        Glide.with(context)
            .load(uri)
            .placeholder(R.mipmap.ic_launcher)
            .centerCrop()
            .into(view)
    }
}

