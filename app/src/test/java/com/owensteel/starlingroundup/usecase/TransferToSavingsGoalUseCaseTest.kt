package com.owensteel.starlingroundup.usecase

import com.owensteel.starlingroundup.domain.repository.SavingsGoalRepository
import com.owensteel.starlingroundup.model.Money
import com.owensteel.starlingroundup.model.TransferResponse
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import java.util.UUID

private const val FAKE_ACCOUNT_CURRENCY = "GBP"

class TransferToSavingsGoalUseCaseTest {

    private val savingsGoalRepository: SavingsGoalRepository = mock()
    private val testScheduler = TestCoroutineScheduler()
    private val testDispatcher = StandardTestDispatcher(testScheduler)

    private lateinit var useCase: TransferToSavingsGoalUseCase

    private val fakeAccountUid = UUID.randomUUID().toString()

    @Before
    fun setUp() {
        useCase = TransferToSavingsGoalUseCase(savingsGoalRepository, testDispatcher)
    }

    @Test
    fun `transferToSavingsGoal returns success and calls repo`() = runTest(testScheduler) {
        // Given
        val accountUid = fakeAccountUid
        val goalUid = UUID.randomUUID().toString()
        val amount = Money(FAKE_ACCOUNT_CURRENCY, 100)

        // When
        val result = useCase.transferToSavingsGoal(accountUid, goalUid, amount)

        // Then
        assertTrue(result.isSuccess)
        verify(savingsGoalRepository).transferToGoal(
            eq(goalUid),
            eq(accountUid),
            any(), // Transfer UUID
            eq(amount)
        )
    }

    @Test
    fun `transferToSavingsGoal returns failure on exception`() = runTest(testScheduler) {
        val accountUid = fakeAccountUid
        val goalUid = UUID.randomUUID().toString()
        val amount = Money(FAKE_ACCOUNT_CURRENCY, 100)

        whenever(savingsGoalRepository.transferToGoal(any(), any(), any(), any()))
            .thenThrow(RuntimeException("fail"))

        val result = useCase.transferToSavingsGoal(accountUid, goalUid, amount)

        assertTrue(result.isFailure)
        assertEquals("fail", result.exceptionOrNull()?.message)
    }

    @Test
    fun `createAndTransferToNewSavingsGoal returns success when both steps succeed`() =
        runTest(testScheduler) {
            val accountUid = fakeAccountUid
            val goalName = "TestGoal"
            val target = Money(FAKE_ACCOUNT_CURRENCY, 1000)
            val roundUp = Money(FAKE_ACCOUNT_CURRENCY, 123)

            whenever(savingsGoalRepository.createGoal(accountUid, goalName, target))
                .thenReturn("new-goal-id")

            // Stub the inner transfer
            whenever(savingsGoalRepository.transferToGoal(any(), any(), any(), any()))
                .thenReturn(TransferResponse(true))

            val result =
                useCase.createAndTransferToNewSavingsGoal(accountUid, goalName, target, roundUp)

            assertTrue(result.isSuccess)
            verify(savingsGoalRepository).createGoal(accountUid, goalName, target)
            verify(savingsGoalRepository).transferToGoal(any(), eq(accountUid), any(), eq(roundUp))
        }

    @Test
    fun `createAndTransferToNewSavingsGoal returns failure if goal creation fails`() =
        runTest(testScheduler) {
            whenever(savingsGoalRepository.createGoal(any(), any(), any()))
                .thenReturn(null)

            val result = useCase.createAndTransferToNewSavingsGoal(
                fakeAccountUid,
                "TestGoal",
                Money(FAKE_ACCOUNT_CURRENCY, 1000),
                Money(FAKE_ACCOUNT_CURRENCY, 100)
            )

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is IllegalStateException)
        }

    @Test
    fun `createAndTransferToNewSavingsGoal returns failure if exception thrown`() =
        runTest(testScheduler) {
            whenever(savingsGoalRepository.createGoal(any(), any(), any()))
                .thenThrow(RuntimeException("boom"))

            val result = useCase.createAndTransferToNewSavingsGoal(
                fakeAccountUid,
                "TestGoal",
                Money(FAKE_ACCOUNT_CURRENCY, 1000),
                Money(FAKE_ACCOUNT_CURRENCY, 100)
            )

            assertTrue(result.isFailure)
            assertEquals("boom", result.exceptionOrNull()?.message)
        }
}
