package com.owensteel.starlingroundup.token

import android.content.Context
import com.owensteel.starlingroundup.BuildConfig
import com.owensteel.starlingroundup.data.local.SecureTokenStore
import com.owensteel.starlingroundup.model.TokenResponse
import com.owensteel.starlingroundup.network.StarlingService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.Response
import javax.inject.Inject

/*

    Fetches access token using refresh token

 */

class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val tokenStore = SecureTokenStore(context)
    private val mutex = Mutex()

    suspend fun getValidAccessToken(): String {
        return mutex.withLock {
            // We could cache the access token to avoid
            // frequent decryption, but this could make
            // the token vulnerable to exposure if memory
            // is compromised or inspected

            val storedToken = tokenStore.getAccessToken()
            if (storedToken != null && !isAccessTokenExpired()) {
                return@withLock storedToken
            }

            // Token is either missing or expired, so
            // refresh it
            return@withLock fetchFromLocalAndRefresh()
        }
    }

    private suspend fun fetchFromLocalAndRefresh(): String {
        // Refresh token can either be a newer one (saved
        // in datastore) or the initial refresh token saved
        // locally as a fallback for cold starts
        val refreshToken: String = try {
            tokenStore.getRefreshToken()
                ?: BuildConfig.INIT_REFRESH_TOKEN
        } catch (e: Exception) {
            throw IllegalStateException("No refresh token found")
        }

        val response = StarlingService.refreshAccessToken(refreshToken)

        return handleTokenResponse(response)
    }

    private suspend fun handleTokenResponse(response: Response<TokenResponse>): String {
        if (!response.isSuccessful) {
            throw Exception("Failed to refresh access token: ${response.code()}")
        }

        val token = response.body() ?: throw Exception("Empty token response")

        // Save access token
        tokenStore.saveAccessToken(
            token.access_token,
            token.expires_in
        )

        // Save new refresh token, if provided
        token.refresh_token?.let { tokenStore.saveRefreshToken(it) }

        return token.access_token
    }

    private suspend fun isAccessTokenExpired(): Boolean {
        val expiryTime = tokenStore.getAccessTokenExpiryTime()
        val now = System.currentTimeMillis()
        return expiryTime == null || now >= expiryTime
    }

}