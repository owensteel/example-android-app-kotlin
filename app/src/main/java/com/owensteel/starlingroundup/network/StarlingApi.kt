package com.owensteel.starlingroundup.network

import com.owensteel.starlingroundup.model.AccountResponse
import com.owensteel.starlingroundup.model.TransactionFeedResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface StarlingApi {

    @GET("api/v2/accounts")
    suspend fun getAccounts(): Response<AccountResponse>

    @GET("api/v2/feed/account/{accountUid}/category/{categoryUid}/transactions-between")
    suspend fun getTransactions(
        @Path("accountUid") accountUid: String,
        @Path("categoryUid") categoryUid: String,
        @Query("minTransactionTimestamp") start: String,
        @Query("maxTransactionTimestamp") end: String
    ): Response<TransactionFeedResponse>

    // TODO: Savings goal endpoints

}