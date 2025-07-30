package com.owensteel.starlingroundup.network

import com.owensteel.starlingroundup.model.AccountHolderIndividualResponse
import com.owensteel.starlingroundup.model.AccountResponse
import com.owensteel.starlingroundup.model.CreateSavingsGoalRequest
import com.owensteel.starlingroundup.model.CreateSavingsGoalResponse
import com.owensteel.starlingroundup.model.GetSavingsGoalsResponse
import com.owensteel.starlingroundup.model.TransactionFeedResponse
import com.owensteel.starlingroundup.model.TransferRequest
import com.owensteel.starlingroundup.model.TransferResponse
import com.owensteel.starlingroundup.util.SharedConstants.ApiConfig.API_SERVER_CERT_HASH
import com.owensteel.starlingroundup.util.SharedConstants.ApiConfig.HOSTNAME
import okhttp3.CertificatePinner
import retrofit2.Response
import java.time.DayOfWeek
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/*

    Retrofit Client
    Communicates with server, with security considerations

*/

// Uses Certificate Pinner (programmatic solution)
val certificatePinner = CertificatePinner.Builder()
    .add(HOSTNAME, API_SERVER_CERT_HASH)
    .build()

@Singleton
class StarlingService @Inject constructor(
    private val apiProvider: StarlingApiProvider
) {

    private fun createAuthenticatedApi(): StarlingApi {
        return apiProvider.getApi()
    }

    // Transactions feed

    suspend fun getTransactionsForCurrentWeek(
        accountUid: String,
        categoryUid: String
    ): Response<TransactionFeedResponse> {
        // Get timestamps for the start of the week
        // and now
        val now = ZonedDateTime.now()
        val startOfWeek = now.with(DayOfWeek.MONDAY).toLocalDate().atStartOfDay(now.zone)
        val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

        val api = createAuthenticatedApi()
        return api.getTransactionsForCurrentWeek(
            accountUid = accountUid,
            categoryUid = categoryUid,
            fromIso = formatter.format(startOfWeek),
            toIso = formatter.format(now)
        )
    }

    // Savings goals

    suspend fun transferToSavingsGoal(
        accountUid: String,
        goalUid: String,
        transferUid: String,
        request: TransferRequest
    ): Response<TransferResponse> {
        val api = createAuthenticatedApi()
        return api.transferToSavingsGoal(
            accountUid = accountUid,
            goalUid = goalUid,
            transferUid = transferUid,
            transfer = request
        )
    }

    suspend fun createSavingsGoal(
        accountUid: String,
        request: CreateSavingsGoalRequest
    ): Response<CreateSavingsGoalResponse> {
        val api = createAuthenticatedApi()
        return api.createSavingsGoal(
            accountUid = accountUid,
            request = request
        )
    }

    suspend fun getSavingsGoals(
        accountUid: String
    ): Response<GetSavingsGoalsResponse> {
        val api = createAuthenticatedApi()
        return api.getSavingsGoals(
            accountUid = accountUid
        )
    }

    // Account details

    suspend fun getAccountDetails(): Response<AccountResponse> {
        val api = createAuthenticatedApi()
        return api.getAccountDetails()
    }

    suspend fun getAccountHolderIndividual(): Response<AccountHolderIndividualResponse> {
        val api = createAuthenticatedApi()
        return api.getAccountHolderIndividual()
    }

}

