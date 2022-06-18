package com.lukic.conseo.utils

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

class EncryptedAppPrefs(
    private val context: Context
): AppPreferences() {

    private val masterKeyAlias: String by lazy { MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC) }

    private val encPrefs = EncryptedSharedPreferences.create(
        FILE_NAME,
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    override fun putString(value: String, key: String) {
        encPrefs.edit().putString(value, key).apply()
    }

    override fun getString(key: String): String {
        return encPrefs.getString(key, STRING_DEF_VALUE) ?: STRING_DEF_VALUE
    }

    override fun putInt(value: Int, key: String) {
        encPrefs.edit().putInt(key, value).apply()
    }

    override fun getInt(key: String): Int {
        return encPrefs.getInt(key, INT_DEF_VALUE)
    }

    override fun putBoolean(key: String, value: Boolean) {
        encPrefs.edit().putBoolean(key, value)
    }

    override fun getBoolean(key: String): Boolean {
        return encPrefs.getBoolean(key, BOOLEAN_DEF_VALUE)
    }

    companion object {

        private const val FILE_NAME = "EncryptedPrefs"

    }
}

