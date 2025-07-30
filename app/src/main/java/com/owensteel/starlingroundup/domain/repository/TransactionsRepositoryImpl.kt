package com.owensteel.starlingroundup.domain.repository

import com.owensteel.starlingroundup.model.TransactionFeedResponse
import com.owensteel.starlingroundup.network.StarlingService
import retrofit2.Response
import javax.inject.Inject

class TransactionsRepositoryImpl @Inject constructor(
    private val starlingService: StarlingService
) : TransactionsRepository {

    override suspend fun getTransactionsFeed(
        accountUid: String,
        categoryUid: String
    ): TransactionFeedResponse? {
        return try {
            val transactionFeedResponse: Response<TransactionFeedResponse> =
                starlingService.getTransactionsForCurrentWeek(
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