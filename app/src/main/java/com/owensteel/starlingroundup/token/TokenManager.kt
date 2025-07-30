package com.owensteel.starlingroundup.token

import com.owensteel.starlingroundup.BuildConfig
import com.owensteel.starlingroundup.data.local.SecureTokenStore
import com.owensteel.starlingroundup.model.TokenResponse
import com.owensteel.starlingroundup.network.StarlingAuthApiProvider
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

/*

    Fetches access token using refresh token

 */

@Singleton
class TokenManager @Inject constructor(
    private val tokenStore: SecureTokenStore,
    private val authApiProvider: StarlingAuthApiProvider
) {

    private val mutex = Mutex()

    private suspend fun getValidAccessToken(): String {
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

    fun getValidAccessTokenBlocking(): String = runBlocking {
        return@runBlocking getValidAccessToken()
    }

    private suspend fun invalidateAndRefreshCurrentAccessToken(): String {
        return fetchFromLocalAndRefresh()
    }

    fun invalidateAndRefreshCurrentAccessTokenBlocking(): String = runBlocking {
        return@runBlocking invalidateAndRefreshCurrentAccessToken()
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

        val response = refreshAccessToken(refreshToken)

        return handleTokenResponse(response)
    }

    private suspend fun handleTokenResponse(response: Response<TokenResponse>): String {
        if (!response.isSuccessful) {
            // Handle invalid refresh token (code 400, invalid_grant)
            if (response.code() == 400 && tokenStore.getRefreshToken() != null) {
                // Saved refresh token is probably expired
                // Fallback to the initial one
                tokenStore.deleteSavedRefreshToken()
                // Retry
                return fetchFromLocalAndRefresh()
            } else {
                throw Exception("Failed to refresh access token: ${response.code()}")
            }
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

    // OAuth API calls

    private suspend fun refreshAccessToken(refreshToken: String): Response<TokenResponse> {
        return authApiProvider.getAuthApi().refreshAccessToken(
            grantType = "refresh_token",
            refreshToken = refreshToken,
            clientId = BuildConfig.CLIENT_ID,
            clientSecret = BuildConfig.CLIENT_SECRET
        )
    }

}