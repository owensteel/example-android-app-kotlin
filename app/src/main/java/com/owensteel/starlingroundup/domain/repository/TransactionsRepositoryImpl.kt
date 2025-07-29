package com.owensteel.starlingroundup.domain.repository

import com.owensteel.starlingroundup.model.TransactionFeedResponse
import com.owensteel.starlingroundup.network.StarlingService
import com.owensteel.starlingroundup.token.TokenManager
import retrofit2.Response
import javax.inject.Inject

class TransactionsRepositoryImpl @Inject constructor(
    private val tokenManager: TokenManager
) : TransactionsRepository {

    override suspend fun getTransactionsFeed(
        accountUid: String,
        categoryUid: String
    ): TransactionFeedResponse? {
        return try {
            val transactionFeedResponse: Response<TransactionFeedResponse> =
                StarlingService.getTransactionsForCurrentWeek(
                    tokenManager,
                    accountUid,
                    categoryUid
                )
            if (transactionFeedResponse.isSuccessful) {
                transactionFeedResponse.body()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

}