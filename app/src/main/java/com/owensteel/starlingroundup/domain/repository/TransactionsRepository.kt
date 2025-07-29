package com.owensteel.starlingroundup.domain.repository

import com.owensteel.starlingroundup.model.TransactionFeedResponse

interface TransactionsRepository {
    suspend fun getTransactionsFeed(
        accountUid: String,
        categoryUid: String
    ): TransactionFeedResponse?
}