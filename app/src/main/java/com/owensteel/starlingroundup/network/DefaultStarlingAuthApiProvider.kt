package com.owensteel.starlingroundup.network

import com.owensteel.starlingroundup.network.interceptors.RegularHeadersInterceptor
import com.owensteel.starlingroundup.util.SharedConstants.ApiConfig.BASE_URL
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultStarlingAuthApiProvider @Inject constructor(): StarlingAuthApiProvider {

    override fun getAuthApi(): StarlingAuthApi {
        val okHttp = OkHttpClient.Builder()
            .certificatePinner(certificatePinner)
            // Just add regular headers
            .addInterceptor(RegularHeadersInterceptor())
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(StarlingAuthApi::class.java)
    }

}