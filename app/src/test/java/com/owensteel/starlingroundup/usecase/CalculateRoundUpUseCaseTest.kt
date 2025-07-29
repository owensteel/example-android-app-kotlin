package com.owensteel.starlingroundup.usecase

import com.owensteel.starlingroundup.model.Money
import com.owensteel.starlingroundup.model.Transaction
import com.owensteel.starlingroundup.util.SharedConstants.Transactions.TRANSACTION_DIRECTION_IN
import com.owensteel.starlingroundup.util.SharedConstants.Transactions.TRANSACTION_DIRECTION_OUT
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.UUID

private const val FAKE_ACCOUNT_CURRENCY = "GBP"
private const val FAKE_TRANSACTION_SOURCE = "MASTER_CARD"
private val FAKE_TRANSACTION_SPENDING_CATEGORY = UUID.randomUUID().toString()

class CalculateRoundUpUseCaseTest {

    private lateinit var calculateRoundUpUseCase: CalculateRoundUpUseCase

    // Example transactions to test round-up cutoff and calculations:
    // OUT £2.65 at 9am
    // OUT £4.35 at 10am
    // OUT £5.20 at 10:10am
    // OUT £0.87 at 10:20am
    // IN £4.23 at 10:25am
    private val fakeTransactions = listOf(
        Transaction(
            feedItemUid = UUID.randomUUID().toString(),
            amount = Money(
                FAKE_ACCOUNT_CURRENCY,
                265L
            ),
            direction = TRANSACTION_DIRECTION_OUT,
            transactionTime = "2025-01-01T09:00:00Z",
            spendingCategory = FAKE_TRANSACTION_SPENDING_CATEGORY,
            source = FAKE_TRANSACTION_SOURCE
        ),
        Transaction(
            feedItemUid = UUID.randomUUID().toString(),
            amount = Money(
                FAKE_ACCOUNT_CURRENCY,
                435L
            ),
            direction = TRANSACTION_DIRECTION_OUT,
            transactionTime = "2025-01-01T10:00:00Z",
            spendingCategory = FAKE_TRANSACTION_SPENDING_CATEGORY,
            source = FAKE_TRANSACTION_SOURCE
        ),
        Transaction(
            feedItemUid = UUID.randomUUID().toString(),
            amount = Money(
                FAKE_ACCOUNT_CURRENCY,
                520L
            ),
            direction = TRANSACTION_DIRECTION_OUT,
            transactionTime = "2025-01-01T10:10:00Z",
            spendingCategory = FAKE_TRANSACTION_SPENDING_CATEGORY,
            source = FAKE_TRANSACTION_SOURCE
        ),
        Transaction(
            feedItemUid = UUID.randomUUID().toString(),
            amount = Money(
                FAKE_ACCOUNT_CURRENCY,
                87L
            ),
            direction = TRANSACTION_DIRECTION_OUT,
            transactionTime = "2025-01-01T10:20:00Z",
            spendingCategory = FAKE_TRANSACTION_SPENDING_CATEGORY,
            source = FAKE_TRANSACTION_SOURCE
        ),
        Transaction(
            feedItemUid = UUID.randomUUID().toString(),
            amount = Money(
                FAKE_ACCOUNT_CURRENCY,
                423L
            ),
            direction = TRANSACTION_DIRECTION_IN,
            transactionTime = "2025-01-01T10:25:00Z",
            spendingCategory = FAKE_TRANSACTION_SPENDING_CATEGORY,
            source = FAKE_TRANSACTION_SOURCE
        )
    )

    @Before
    fun setUp() = runBlocking {
        calculateRoundUpUseCase = CalculateRoundUpUseCase()
    }

    @Test
    fun `RoundUpUseCase calculates correct total from list of transactions`() = runTest {
        // Cutoff that allows all but one fake transaction
        val fakeCutoffTimestamp = "2025-01-01T09:30:00Z"
        assertEquals(
            calculateRoundUpUseCase.invoke(
                fakeTransactions,
                fakeCutoffTimestamp
            ),
            158L
        )
    }

}