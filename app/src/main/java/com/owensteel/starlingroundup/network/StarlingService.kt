package com.owensteel.starlingroundup.network

import android.content.Context
import com.owensteel.starlingroundup.data.local.SecureTokenStore
import com.owensteel.starlingroundup.util.SharedConstants.ApiConfig.API_SERVER_CERT_HASH
import com.owensteel.starlingroundup.util.SharedConstants.ApiConfig.BASE_URL
import com.owensteel.starlingroundup.util.SharedConstants.ApiHeaders.ACCEPT
import com.owensteel.starlingroundup.util.SharedConstants.ApiHeaders.AUTHORIZATION
import com.owensteel.starlingroundup.util.SharedConstants.ApiHeaders.USER_AGENT
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/*

    Retrofit Client
    Communicates with server, with security considerations

*/

// Static HTTP client settings
private const val HTTP_CLIENT_ACCEPT_VALUE = "application/json"
private const val HTTP_CLIENT_USER_AGENT = "OwenSteel-StarlingChallenge"

// Uses Certificate Pinner (programmatic solution)
val certificatePinner = CertificatePinner.Builder()
    .add(BASE_URL, API_SERVER_CERT_HASH)
    .build()

object StarlingService {

    suspend fun create(context: Context): StarlingApi {
        // Get our access token from encrypted storage
        val tokenStorage = SecureTokenStore(context)
        // Since getToken is a suspend function (because it's reading from
        // DataStore), and Retrofit's addInterceptor is not suspendable,
        // we have to use this function as a suspend wrapper to retrieve the
        // token first, and only then build the Retrofit client
        val token = tokenStorage.getToken() ?: throw IllegalStateException("Missing API token")

        val okHttp = OkHttpClient.Builder()
            .certificatePinner(certificatePinner)
            .addInterceptor { chain ->
                val original = chain.request()
                val request = original.newBuilder()
                    .header(AUTHORIZATION, "Bearer $token")
                    .header(ACCEPT, HTTP_CLIENT_ACCEPT_VALUE)
                    .header(USER_AGENT, HTTP_CLIENT_USER_AGENT)
                    .method(original.method, original.body)
                    .build()
                chain.proceed(request)
            }
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(StarlingApi::class.java)
    }

}