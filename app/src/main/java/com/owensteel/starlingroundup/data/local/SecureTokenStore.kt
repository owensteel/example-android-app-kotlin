package com.owensteel.starlingroundup.data.local

import androidx.datastore.preferences.core.byteArrayPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import com.owensteel.starlingroundup.util.SharedConstants.PreferenceKeys.ACCESS_TOKEN_EXPIRY
import com.owensteel.starlingroundup.util.SharedConstants.PreferenceKeys.ACCESS_TOKEN_IV
import com.owensteel.starlingroundup.util.SharedConstants.PreferenceKeys.ENCRYPTED_ACCESS_TOKEN
import com.owensteel.starlingroundup.util.SharedConstants.PreferenceKeys.ENCRYPTED_REFRESH_TOKEN
import com.owensteel.starlingroundup.util.SharedConstants.PreferenceKeys.REFRESH_TOKEN_EXPIRY
import com.owensteel.starlingroundup.util.SharedConstants.PreferenceKeys.REFRESH_TOKEN_IV
import javax.inject.Inject

/*

    Token utility

    Use CryptoManager to get and save Access and Refresh tokens

 */

class SecureTokenStore @Inject constructor(
    private val dataStoreManager: DataStoreManager,
    private val crypto: CryptoManager = CryptoManager() // allows mocking
) {
    private suspend fun saveToken(
        token: String,
        tokenKey: String,
        ivKey: String,
        expiresInSeconds: Long? = null
    ) {
        val tokenKeyPrefKey = byteArrayPreferencesKey(tokenKey)
        val ivKeyPrefKey = byteArrayPreferencesKey(ivKey)
        val (iv, encrypted) = crypto.encrypt(token)

        dataStoreManager.write(tokenKeyPrefKey, encrypted)
        dataStoreManager.write(ivKeyPrefKey, iv)

        // Record expiry for token
        if (
            expiresInSeconds != null
            && (tokenKey == ENCRYPTED_ACCESS_TOKEN || tokenKey == ENCRYPTED_REFRESH_TOKEN)
        ) {
            val expiryTime = System.currentTimeMillis() + (expiresInSeconds * 1000)
            // Handle expiry recording for both Access and Refresh tokens (though
            // we can apparently rely on updated Refresh tokens being sent to us
            // by the server)
            val tokenExpiryPrefKey = if (tokenKey == ENCRYPTED_ACCESS_TOKEN)
                longPreferencesKey(ACCESS_TOKEN_EXPIRY) else longPreferencesKey(REFRESH_TOKEN_EXPIRY)
            dataStoreManager.write(tokenExpiryPrefKey, expiryTime)
        }
    }

    private suspend fun getToken(tokenKey: String, ivKey: String): String? {
        val tokenKeyPrefKey = byteArrayPreferencesKey(tokenKey)
        val ivKeyPrefKey = byteArrayPreferencesKey(ivKey)

        val encrypted = dataStoreManager.read(tokenKeyPrefKey)
        val iv = dataStoreManager.read(ivKeyPrefKey)
        return if (encrypted != null && iv != null) {
            crypto.decrypt(iv, encrypted)
        } else null
    }

    suspend fun deleteSavedRefreshToken() {
        // In case the saved refresh token is
        // expired or invalid, this allows us to
        // fallback to the initial hardcoded one
        dataStoreManager.remove(byteArrayPreferencesKey(ENCRYPTED_REFRESH_TOKEN))
    }

    suspend fun saveRefreshToken(token: String) {
        saveToken(
            token,
            ENCRYPTED_REFRESH_TOKEN,
            REFRESH_TOKEN_IV
        )
    }

    suspend fun getRefreshToken(): String? {
        return getToken(
            ENCRYPTED_REFRESH_TOKEN,
            REFRESH_TOKEN_IV
        )
    }

    suspend fun saveAccessToken(token: String, expiresInSeconds: Int) {
        saveToken(
            token,
            ENCRYPTED_ACCESS_TOKEN,
            ACCESS_TOKEN_IV,
            expiresInSeconds.toLong()
        )
    }

    suspend fun getAccessToken(): String? {
        return getToken(
            ENCRYPTED_ACCESS_TOKEN,
            ACCESS_TOKEN_IV
        )
    }

    suspend fun getAccessTokenExpiryTime(): Long? {
        return dataStoreManager.read(longPreferencesKey(ACCESS_TOKEN_EXPIRY))
    }

}