package com.owensteel.starlingroundup.network.interceptors

import com.owensteel.starlingroundup.token.TokenManager
import com.owensteel.starlingroundup.util.SharedConstants.ApiHeaders.AUTHORIZATION
import okhttp3.Interceptor

/*

    Attaches access token to request
    and monitors for bad responses

 */

class AuthInterceptor(
    private val tokenManager: TokenManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        var request = chain.request()

        // Attach token in Authorization header
        val token = tokenManager.getValidAccessTokenBlocking()
        request = request.newBuilder()
            .header(AUTHORIZATION, "Bearer $token")
            .build()

        // Monitor response
        val response = chain.proceed(request)

        // Access token may have expired for some external
        // reason, refresh access code and retry once
        if (response.code == 403) {
            response.close()

            val newToken = tokenManager.invalidateAndRefreshCurrentAccessTokenBlocking()

            // Retry request with new access token
            val newRequest = request.newBuilder()
                .header(AUTHORIZATION, "Bearer $newToken")
                .build()

            return chain.proceed(newRequest)
        }

        return response
    }
}