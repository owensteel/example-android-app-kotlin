package com.owensteel.starlingroundup.domain.repository

import com.owensteel.starlingroundup.model.TransactionFeedResponse
import com.owensteel.starlingroundup.token.TokenManager

interface TransactionsRepository {
    suspend fun getTransactionsFeed(
        tokenManager: TokenManager,
        accountUid: String,
        categoryUid: String
    ): TransactionFeedResponse?
}