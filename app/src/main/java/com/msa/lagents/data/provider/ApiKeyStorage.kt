package com.msa.lagents.data.provider

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class ApiKeyStorage(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPrefs = EncryptedSharedPreferences.create(
        context,
        "lagents_api_keys",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun storeApiKey(alias: String, apiKey: String) {
        sharedPrefs.edit().putString(alias, apiKey).apply()
    }

    fun getApiKey(alias: String): String? {
        return sharedPrefs.getString(alias, null)
    }

    fun deleteApiKey(alias: String) {
        sharedPrefs.edit().remove(alias).apply()
    }

    fun clearAll() {
        sharedPrefs.edit().clear().apply()
    }
}
