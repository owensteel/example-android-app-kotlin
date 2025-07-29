package com.owensteel.starlingroundup.usecase

import com.owensteel.starlingroundup.di.IoDispatcher
import com.owensteel.starlingroundup.domain.repository.TransactionsRepository
import com.owensteel.starlingroundup.model.Transaction
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FetchTransactionsUseCase @Inject constructor(
    private val transactionsRepository: TransactionsRepository,
    @IoDispatcher val dispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(accountUid: String, categoryUid: String): List<Transaction> =
        withContext(dispatcher) {
            val transactionsFeed = transactionsRepository.getTransactionsFeed(
                accountUid,
                categoryUid
            )

            if (transactionsFeed == null) {
                return@withContext emptyList<Transaction>()
            }

            return@withContext transactionsFeed.feedItems
        }

}