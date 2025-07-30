package com.owensteel.starlingroundup.usecase

import com.owensteel.starlingroundup.domain.repository.TransactionsRepository
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import java.util.UUID

class FetchTransactionsUseCaseTest {

    private lateinit var fetchTransactionsUseCase: FetchTransactionsUseCase

    private val transactionsRepository: TransactionsRepository = mock()
    private val testScheduler = TestCoroutineScheduler()
    private val testDispatcher = StandardTestDispatcher(testScheduler)

    @Before
    fun setUp() {
        fetchTransactionsUseCase = FetchTransactionsUseCase(
            transactionsRepository,
            testDispatcher
        )
    }

    @Test
    fun `returns transactions when repository returns feed`() = runTest(testScheduler) {
        val accountUid = UUID.randomUUID().toString()
        val categoryUid = UUID.randomUUID().toString()
        whenever(transactionsRepository.getTransactionsFeed(accountUid, categoryUid)).thenReturn(
            null
        )

        val result = fetchTransactionsUseCase(accountUid, categoryUid)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `returns empty list when repository returns null`() = runTest(testScheduler) {
        whenever(transactionsRepository.getTransactionsFeed(any(), any())).thenReturn(null)

        val result =
            fetchTransactionsUseCase(UUID.randomUUID().toString(), UUID.randomUUID().toString())
        assertTrue(result.isEmpty())
    }

}