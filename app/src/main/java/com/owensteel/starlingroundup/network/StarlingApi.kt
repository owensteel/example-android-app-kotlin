package com.owensteel.starlingroundup.network

import com.owensteel.starlingroundup.model.AccountResponse
import com.owensteel.starlingroundup.model.TransactionFeedResponse
import com.owensteel.starlingroundup.model.TransferRequest
import com.owensteel.starlingroundup.model.TransferResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface StarlingApi {

    @GET("api/v2/accounts")
    suspend fun getAccountDetails(
        @Header("Authorization") bearerToken: String
    ): Response<AccountResponse>

    @GET("api/v2/feed/account/{accountUid}/category/{categoryUid}/transactions-between")
    suspend fun getTransactionsForCurrentWeek(
        @Header("Authorization") bearerToken: String,
        @Path("accountUid") accountUid: String,
        @Path("categoryUid") categoryUid: String,
        @Query("minTransactionTimestamp") fromIso: String,
        @Query("maxTransactionTimestamp") toIso: String
    ): Response<TransactionFeedResponse>

    @PUT("api/v2/account/{accountUid}/savings-goals/{savingsGoalUid}/add-money/{transferUid}")
    suspend fun roundUpTransfer(
        @Header("Authorization") bearerToken: String,
        @Path("accountUid") accountUid: String,
        @Path("savingsGoalUid") goalUid: String,
        @Path("transferUid") transferUid: String,
        @Body transfer: TransferRequest
    ): Response<TransferResponse>

}