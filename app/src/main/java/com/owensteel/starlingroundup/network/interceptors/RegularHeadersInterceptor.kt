package com.owensteel.starlingroundup.network.interceptors

import com.owensteel.starlingroundup.util.SharedConstants.ApiHeaders.ACCEPT
import com.owensteel.starlingroundup.util.SharedConstants.ApiHeaders.USER_AGENT
import okhttp3.Interceptor

/*

    Regular headers for requests

 */

// Static HTTP client settings
private const val HTTP_CLIENT_ACCEPT_VALUE = "application/json"
private const val HTTP_CLIENT_USER_AGENT = "OwenSteel-StarlingChallenge"

class RegularHeadersInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val original = chain.request()
        val request = original.newBuilder()
            .header(ACCEPT, HTTP_CLIENT_ACCEPT_VALUE)
            .header(USER_AGENT, HTTP_CLIENT_USER_AGENT)
            .method(original.method, original.body)
            .build()
        return chain.proceed(request)
    }
}