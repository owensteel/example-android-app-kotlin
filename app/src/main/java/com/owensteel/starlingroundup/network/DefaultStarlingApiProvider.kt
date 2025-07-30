package com.owensteel.starlingroundup.network

import com.owensteel.starlingroundup.network.interceptors.AuthInterceptor
import com.owensteel.starlingroundup.network.interceptors.RegularHeadersInterceptor
import com.owensteel.starlingroundup.token.TokenManager
import com.owensteel.starlingroundup.util.SharedConstants.ApiConfig.BASE_URL
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultStarlingApiProvider @Inject constructor(
    private val tokenManager: TokenManager
) : StarlingApiProvider {

    override fun getApi(): StarlingApi {
        val okHttp = OkHttpClient.Builder()
            .certificatePinner(certificatePinner)
            // Fetch access token and add Authorization
            // header, and monitor for bad responses in
            // case token has expired for some other reason
            // than time
            .addInterceptor(AuthInterceptor(tokenManager))
            .addInterceptor(RegularHeadersInterceptor())
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(StarlingApi::class.java)
    }

}