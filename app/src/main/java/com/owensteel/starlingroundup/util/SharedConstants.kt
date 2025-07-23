package com.owensteel.starlingroundup.util

import androidx.datastore.preferences.core.byteArrayPreferencesKey

object SharedConstants {

    const val DATASTORE_NAME = "secure_token_store"

    object PreferenceKeys {
        val ENCRYPTED_TOKEN = byteArrayPreferencesKey("encrypted_token")
        val TOKEN_IV = byteArrayPreferencesKey("token_iv")
    }

    object ApiHeaders {
        const val AUTHORIZATION = "Authorization"
        const val ACCEPT = "Accept"
        const val USER_AGENT = "User-Agent"
    }

    object ApiConfig {
        const val BASE_URL = "https://api-sandbox.starlingbank.com"
    }

}