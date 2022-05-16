package com.lukic.conseo

import android.app.Application
import android.content.Context
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.conseo.database.databaseModule
import lv.chi.photopicker.ChiliPhotoPicker
import lv.chi.photopicker.loader.ImageLoader
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
                    viewModelModules
                )
            )
        }
        ChiliPhotoPicker.init(
            loader = GlideImageLoader(),
            authority = "com.lukic.conseo.fileprovider"
        )
    }

    companion object {
        private var instance: MyApplication? = null

        fun getInstance(): MyApplication = instance ?: MyApplication()
        fun getAppContext(): Context {
            return instance?.applicationContext!!
        }
    }
}

class GlideImageLoader(): ImageLoader {
    override fun loadImage(context: Context, view: ImageView, uri: Uri) {
        Glide.with(context).load(uri).placeholder(R.mipmap.ic_launcher).into(view)
    }
}