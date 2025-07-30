package com.owensteel.starlingroundup.domain.repository

import com.owensteel.starlingroundup.model.Money
import com.owensteel.starlingroundup.model.Transaction
import com.owensteel.starlingroundup.model.TransactionFeedResponse
import com.owensteel.starlingroundup.network.StarlingService
import com.owensteel.starlingroundup.util.SharedConstants.Transactions.TRANSACTION_DIRECTION_OUT
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import retrofit2.Response
import java.util.UUID

private const val FAKE_ACCOUNT_CURRENCY = "GBP"
private const val FAKE_TRANSACTION_SOURCE = "MASTER_CARD"

class TransactionsRepositoryImplTest {

    private val starlingService: StarlingService = mock()
    private lateinit var repository: TransactionsRepositoryImpl

    private val accountUid = UUID.randomUUID().toString()
    private val categoryUid = UUID.randomUUID().toString()

    @Before
    fun setUp() {
        repository = TransactionsRepositoryImpl(starlingService)
    }

    @Test
    fun `getTransactionsFeed returns data on success`() = runTest {
        val feedItemUid = UUID.randomUUID().toString()

        val transaction = Transaction(
            feedItemUid,
            Money(
                FAKE_ACCOUNT_CURRENCY,
                100L
            ),
            TRANSACTION_DIRECTION_OUT,
            spendingCategory = categoryUid,
            transactionTime = "2025-01-01T09:00:00Z",
            source = FAKE_TRANSACTION_SOURCE
        )
        val feed = TransactionFeedResponse(listOf(transaction))

        whenever(starlingService.getTransactionsForCurrentWeek(accountUid, categoryUid))
            .thenReturn(Response.success(feed))

        val result = repository.getTransactionsFeed(accountUid, categoryUid)

        assertNotNull(result)
        assertEquals(1, result?.feedItems?.size)
        assertEquals(feedItemUid, result?.feedItems?.first()?.feedItemUid)
    }

    @Test
    fun `getTransactionsFeed returns null on API error`() = runTest {
        whenever(starlingService.getTransactionsForCurrentWeek(any(), any()))
            .thenReturn(Response.error(403, "".toResponseBody(null)))

        val result = repository.getTransactionsFeed(accountUid, categoryUid)

        assertNull(result)
    }

    @Test
    fun `getTransactionsFeed returns null on exception`() = runTest {
        whenever(starlingService.getTransactionsForCurrentWeek(any(), any()))
            .thenThrow(RuntimeException("fail"))

        val result = repository.getTransactionsFeed(accountUid, categoryUid)

        assertNull(result)
    }
}
