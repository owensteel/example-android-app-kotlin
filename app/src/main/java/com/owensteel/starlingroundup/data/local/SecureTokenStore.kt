package com.owensteel.starlingroundup.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.owensteel.starlingroundup.util.SharedConstants.DATASTORE_NAME
import com.owensteel.starlingroundup.util.SharedConstants.PreferenceKeys.ENCRYPTED_TOKEN
import com.owensteel.starlingroundup.util.SharedConstants.PreferenceKeys.TOKEN_IV
import kotlinx.coroutines.flow.first

/*

    Token utility

    Use CryptoManager to get and save token

 */

private val Context.dataStore by preferencesDataStore(DATASTORE_NAME)

class SecureTokenStore(private val context: Context) {

    private val tokenKey = ENCRYPTED_TOKEN
    private val ivKey = TOKEN_IV
    private val crypto = CryptoManager(context)

    suspend fun saveToken(token: String) {
        val (iv, encrypted) = crypto.encrypt(token)
        context.dataStore.edit { prefs ->
            prefs[tokenKey] = encrypted
            prefs[ivKey] = iv
        }
    }

    suspend fun getToken(): String? {
        val prefs = context.dataStore.data.first()
        val encrypted = prefs[tokenKey]
        val iv = prefs[ivKey]
        return if (encrypted != null && iv != null) {
            crypto.decrypt(iv, encrypted)
        } else null
    }

}