package com.lukic.conseo.utils

abstract class AppPreferences {

    abstract fun putString(value: String, key: String)

    abstract fun getString(key: String): String

    abstract fun putInt(value: Int, key: String)

    abstract fun getInt(key: String): Int

    companion object{
        const val STRING_DEF_VALUE = "StringDefValue"
        const val INT_DEF_VALUE = 0
    }

}