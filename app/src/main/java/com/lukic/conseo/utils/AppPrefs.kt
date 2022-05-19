package com.lukic.conseo.utils

import android.content.Context
import android.content.SharedPreferences
import com.lukic.conseo.MyApplication

class AppPrefs {

    private val sharedPreferences = MyApplication.getAppContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)

    fun storeInt(key: String, value: Int){
        sharedPreferences.edit().putInt(key, value).apply()
    }

    fun getInt(key: String): Int{
        return sharedPreferences.getInt(key, 0)
    }

    companion object{
        const val distanceKey = "distance_key"
    }
}