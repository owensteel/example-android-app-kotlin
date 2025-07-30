package com.owensteel.starlingroundup.usecase

import com.owensteel.starlingroundup.domain.repository.SavingsGoalRepository
import com.owensteel.starlingroundup.model.Money
import com.owensteel.starlingroundup.model.SavingsGoal
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.UUID

private const val FAKE_ACCOUNT_CURRENCY = "GBP"
private const val FAKE_SAVINGS_GOAL_STATE = "ACTIVE"

class GetSavingsGoalsUseCaseTest {

    private lateinit var savingsGoalsUseCase: GetSavingsGoalsUseCase

    private val savingsGoalRepository: SavingsGoalRepository = mock()
    private val testScheduler = TestCoroutineScheduler()
    private val testDispatcher = StandardTestDispatcher(testScheduler)

    @Before
    fun setUp() {
        savingsGoalsUseCase = GetSavingsGoalsUseCase(savingsGoalRepository, testDispatcher)
    }

    @Test
    fun `returns savings goals when repository returns list`() = runTest(testScheduler) {

        val accountUid = UUID.randomUUID().toString()
        val goals = listOf(
            SavingsGoal(
                savingsGoalUid = UUID.randomUUID().toString(),
                name = "TestGoal1",
                target = Money(FAKE_ACCOUNT_CURRENCY, 1000L),
                totalSaved = Money(FAKE_ACCOUNT_CURRENCY, 500L),
                savedPercentage = 50,
                state = FAKE_SAVINGS_GOAL_STATE
            ),
            SavingsGoal(
                savingsGoalUid = UUID.randomUUID().toString(),
                name = "TestGoal2",
                target = Money(FAKE_ACCOUNT_CURRENCY, 1000L),
                totalSaved = Money(FAKE_ACCOUNT_CURRENCY, 500L),
                savedPercentage = 50,
                state = FAKE_SAVINGS_GOAL_STATE
            )
        )
        whenever(savingsGoalRepository.getAccountSavingsGoals(accountUid)).thenReturn(goals)

        val result = savingsGoalsUseCase(accountUid)

        assertEquals(goals, result)

    }

    @Test
    fun `returns empty list when repository returns null`() = runTest(testScheduler) {

        val accountUid = UUID.randomUUID().toString()
        whenever(savingsGoalRepository.getAccountSavingsGoals(accountUid)).thenReturn(null)

        val result = savingsGoalsUseCase(accountUid)

        assertTrue(result.isEmpty())

    }

}