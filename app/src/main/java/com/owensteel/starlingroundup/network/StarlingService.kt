package com.owensteel.starlingroundup.network

import android.content.Context
import com.owensteel.starlingroundup.BuildConfig
import com.owensteel.starlingroundup.model.AccountHolderIndividualResponse
import com.owensteel.starlingroundup.model.AccountResponse
import com.owensteel.starlingroundup.model.CreateSavingsGoalRequest
import com.owensteel.starlingroundup.model.CreateSavingsGoalResponse
import com.owensteel.starlingroundup.model.GetSavingsGoalsResponse
import com.owensteel.starlingroundup.model.TokenResponse
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
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.CertificatePinner
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.DayOfWeek
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

@Module
@InstallIn(SingletonComponent::class)
object StarlingService {

    /*

        API Clients

     */

    // Create authenticated API client
    // This is the primary client for API calls
    private fun createAuthenticatedApi(tokenManager: TokenManager): StarlingApi {
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

    // Unauthenticated client necessary for calls to token refresh API
    private fun createAuthApi(): StarlingAuthApi {
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

    // OAuth functions

    suspend fun refreshAccessToken(refreshToken: String): Response<TokenResponse> {
        val api = createAuthApi()
        return api.refreshAccessToken(
            grantType = "refresh_token",
            refreshToken = refreshToken,
            clientId = BuildConfig.CLIENT_ID,
            clientSecret = BuildConfig.CLIENT_SECRET
        )
    }

    /*

        API functions

     */

    // Transactions feed

    suspend fun getTransactionsForCurrentWeek(
        context: Context,
        tokenManager: TokenManager,
        accountUid: String,
        categoryUid: String
    ): Response<TransactionFeedResponse> {
        // Get timestamps for the start of the week
        // and now
        val now = ZonedDateTime.now()
        val startOfWeek = now.with(DayOfWeek.MONDAY).toLocalDate().atStartOfDay(now.zone)
        val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

        val api = createAuthenticatedApi(tokenManager)
        return api.getTransactionsForCurrentWeek(
            accountUid = accountUid,
            categoryUid = categoryUid,
            fromIso = formatter.format(startOfWeek),
            toIso = formatter.format(now)
        )
    }

    // Savings goals

    suspend fun transferToSavingsGoal(
        context: Context,
        tokenManager: TokenManager,
        accountUid: String,
        goalUid: String,
        transferUid: String,
        request: TransferRequest
    ): Response<TransferResponse> {
        val api = createAuthenticatedApi(tokenManager)
        return api.transferToSavingsGoal(
            accountUid = accountUid,
            goalUid = goalUid,
            transferUid = transferUid,
            transfer = request
        )
    }

    suspend fun createSavingsGoal(
        context: Context,
        tokenManager: TokenManager,
        accountUid: String,
        request: CreateSavingsGoalRequest
    ): Response<CreateSavingsGoalResponse> {
        val api = createAuthenticatedApi(tokenManager)
        return api.createSavingsGoal(
            accountUid = accountUid,
            request = request
        )
    }

    suspend fun getSavingsGoals(
        tokenManager: TokenManager,
        accountUid: String
    ): Response<GetSavingsGoalsResponse> {
        val api = createAuthenticatedApi(tokenManager)
        return api.getSavingsGoals(
            accountUid = accountUid
        )
    }

    // Account details

    suspend fun getAccountDetails(
        context: Context,
        tokenManager: TokenManager
    ): Response<AccountResponse> {
        val api = createAuthenticatedApi(tokenManager)
        return api.getAccountDetails()
    }

    suspend fun getAccountHolderIndividual(
        context: Context,
        tokenManager: TokenManager
    ): Response<AccountHolderIndividualResponse> {
        val api = createAuthenticatedApi(tokenManager)
        return api.getAccountHolderIndividual()
    }

}

/*

    Regular headers for requests

 */

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