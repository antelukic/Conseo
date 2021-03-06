package com.lukic.conseo.utils

import android.content.Context
import android.content.SharedPreferences
import com.lukic.conseo.MyApplication

class AppPrefs: AppPreferences() {

    private val sharedPreferences = MyApplication.getAppContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)

    override fun getString(key: String): String {
        return sharedPreferences.getString(key, STRING_DEF_VALUE) ?: STRING_DEF_VALUE
    }

    override fun getInt(key: String): Int{
        return sharedPreferences.getInt(key, INT_DEF_VALUE)
    }

    override fun putString(value: String, key: String) {
        sharedPreferences.edit().putString(value, key).apply()
    }
    override fun putInt(value: Int, key: String) {
        sharedPreferences.edit().putInt(key, value).apply()
    }

    companion object{
        const val DISTANCE_KEY = "distance_key"

    }
}