package com.owensteel.starlingroundup.network

import android.content.Context
import com.owensteel.starlingroundup.model.AccountHolderIndividualResponse
import com.owensteel.starlingroundup.model.AccountResponse
import com.owensteel.starlingroundup.model.TransactionFeedResponse
import com.owensteel.starlingroundup.model.TransferRequest
import com.owensteel.starlingroundup.model.TransferResponse
import com.owensteel.starlingroundup.token.TokenManager
import com.owensteel.starlingroundup.util.SharedConstants.ApiConfig.API_SERVER_CERT_HASH
import com.owensteel.starlingroundup.util.SharedConstants.ApiConfig.BASE_URL
import com.owensteel.starlingroundup.util.SharedConstants.ApiConfig.HOSTNAME
import com.owensteel.starlingroundup.util.SharedConstants.ApiHeaders.ACCEPT
import com.owensteel.starlingroundup.util.SharedConstants.ApiHeaders.AUTHORIZATION
import com.owensteel.starlingroundup.util.SharedConstants.ApiHeaders.USER_AGENT
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/*

    Retrofit Client
    Communicates with server, with security considerations

*/

// Static HTTP client settings
private const val HTTP_CLIENT_ACCEPT_VALUE = "application/json"
private const val HTTP_CLIENT_USER_AGENT = "OwenSteel-StarlingChallenge"

// Uses Certificate Pinner (programmatic solution)
val certificatePinner = CertificatePinner.Builder()
    .add(HOSTNAME, API_SERVER_CERT_HASH)
    .build()

object StarlingService {

    private fun createAuthenticatedApi(token: String): StarlingApi {
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

    // Unauthenticated client necessary for calls to token refresh API
    fun createAuthApi(): StarlingAuthApi {
        val okHttp = OkHttpClient.Builder()
            .certificatePinner(certificatePinner)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(StarlingAuthApi::class.java)
    }

    // API functions

    suspend fun getTransactionsForCurrentWeek(
        context: Context,
        tokenManager: TokenManager,
        accountUid: String,
        categoryUid: String
    ): Response<TransactionFeedResponse> {
        // Get timestamps for the start of the week
        // and now
        val now = ZonedDateTime.now()
        val startOfWeek = now.with(java.time.DayOfWeek.MONDAY).toLocalDate().atStartOfDay(now.zone)
        val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

        // Fetch token here so we may pass it in the
        // header
        val token = tokenManager.getValidAccessToken()
        val api = createAuthenticatedApi(token)
        return api.getTransactionsForCurrentWeek(
            bearerToken = "Bearer $token",
            accountUid = accountUid,
            categoryUid = categoryUid,
            fromIso = formatter.format(startOfWeek),
            toIso = formatter.format(now)
        )
    }

    suspend fun roundUpTransfer(
        context: Context,
        tokenManager: TokenManager,
        accountUid: String,
        goalUid: String,
        transferUid: String,
        request: TransferRequest
    ): Response<TransferResponse> {
        // Fetch token here so we may pass it in the
        // header
        val token = tokenManager.getValidAccessToken()
        val api = createAuthenticatedApi(token)
        return api.roundUpTransfer(
            bearerToken = "Bearer $token",
            accountUid = accountUid,
            goalUid = goalUid,
            transferUid = transferUid,
            transfer = request
        )
    }

    suspend fun getAccountDetails(
        context: Context,
        tokenManager: TokenManager
    ): Response<AccountResponse> {
        val token = tokenManager.getValidAccessToken()
        val api = createAuthenticatedApi(token)
        return api.getAccountDetails(
            bearerToken = "Bearer $token"
        )
    }

    suspend fun getAccountHolderIndividual(
        context: Context,
        tokenManager: TokenManager
    ): Response<AccountHolderIndividualResponse> {
        val token = tokenManager.getValidAccessToken()
        val api = createAuthenticatedApi(token)
        return api.getAccountHolderIndividual(
            bearerToken = "Bearer $token"
        )
    }

}