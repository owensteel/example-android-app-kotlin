package com.owensteel.starlingroundup.data.local

import android.content.Context
import androidx.datastore.preferences.core.byteArrayPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

/*

    Token utility

    Use CryptoManager to get and save token

 */

private const val DATASTORE_IDENTIFIER = "secure_token_store"
private const val TOKEN_KEY_IDENTIFIER = "encrypted_token"
private const val IV_KEY_IDENTIFIER = "token_iv"

private val Context.dataStore by preferencesDataStore(DATASTORE_IDENTIFIER)

class SecureTokenStore(private val context: Context) {

    private val tokenKey = byteArrayPreferencesKey(TOKEN_KEY_IDENTIFIER)
    private val ivKey = byteArrayPreferencesKey(IV_KEY_IDENTIFIER)
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