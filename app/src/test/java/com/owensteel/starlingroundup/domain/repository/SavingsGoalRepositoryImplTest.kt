package com.owensteel.starlingroundup.domain.repository

import com.owensteel.starlingroundup.model.CreateSavingsGoalResponse
import com.owensteel.starlingroundup.model.GetSavingsGoalsResponse
import com.owensteel.starlingroundup.model.Money
import com.owensteel.starlingroundup.model.SavingsGoal
import com.owensteel.starlingroundup.model.TransferResponse
import com.owensteel.starlingroundup.network.StarlingService
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import retrofit2.Response
import java.util.UUID

private const val FAKE_ACCOUNT_CURRENCY = "GBP"
private const val FAKE_SAVINGS_GOAL_STATE = "ACTIVE"

class SavingsGoalRepositoryImplTest {

    private val starlingService: StarlingService = mock()
    private lateinit var repository: SavingsGoalRepositoryImpl

    private val accountUid = UUID.randomUUID().toString()

    @Before
    fun setUp() {
        repository = SavingsGoalRepositoryImpl(starlingService)
    }

    @Test
    fun `transferToGoal returns body on success`() = runTest {
        val transferResponse = TransferResponse(true)
        whenever(
            starlingService.transferToSavingsGoal(
                any(), any(), any(), any()
            )
        ).thenReturn(Response.success(transferResponse))

        val result = repository.transferToGoal(
            savingsGoalUid = UUID.randomUUID().toString(),
            accountUid = accountUid,
            transferUid = UUID.randomUUID().toString(),
            roundUpAmount = Money(FAKE_ACCOUNT_CURRENCY, 100L)
        )

        assertNotNull(result)
    }

    @Test
    fun `transferToGoal returns null on error`() = runTest {
        whenever(starlingService.transferToSavingsGoal(any(), any(), any(), any()))
            .thenReturn(Response.error(400, "".toResponseBody(null)))

        val result = repository.transferToGoal(
            UUID.randomUUID().toString(),
            accountUid,
            UUID.randomUUID().toString(),
            Money(FAKE_ACCOUNT_CURRENCY, 100L)
        )

        assertNull(result)
    }

    @Test
    fun `transferToGoal returns null on exception`() = runTest {
        whenever(starlingService.transferToSavingsGoal(any(), any(), any(), any()))
            .thenThrow(RuntimeException("failure"))

        val result = repository.transferToGoal(
            UUID.randomUUID().toString(),
            accountUid,
            UUID.randomUUID().toString(),
            Money(FAKE_ACCOUNT_CURRENCY, 100L)
        )

        assertNull(result)
    }

    @Test
    fun `createGoal returns UID on success`() = runTest {
        val expectedUid = UUID.randomUUID().toString()
        val money = Money(FAKE_ACCOUNT_CURRENCY, 100L)
        val response = CreateSavingsGoalResponse(
            savingsGoalUid = expectedUid,
            success = true
        )
        whenever(starlingService.createSavingsGoal(eq(accountUid), any()))
            .thenReturn(Response.success(response))

        val result = repository.createGoal(accountUid, "Holiday Fund", money)

        assertEquals(expectedUid, result)
    }

    @Test
    fun `createGoal returns null on error`() = runTest {
        whenever(starlingService.createSavingsGoal(any(), any()))
            .thenReturn(Response.error(422, "".toResponseBody(null)))

        val result = repository.createGoal(
            accountUid,
            "Rent",
            Money(FAKE_ACCOUNT_CURRENCY, 100L)
        )

        assertNull(result)
    }

    @Test
    fun `createGoal returns null on exception`() = runTest {
        whenever(starlingService.createSavingsGoal(any(), any()))
            .thenThrow(RuntimeException("boom"))

        val result = repository.createGoal(
            accountUid,
            "Rainy Day",
            Money(FAKE_ACCOUNT_CURRENCY, 100L)
        )

        assertNull(result)
    }

    @Test
    fun `getAccountSavingsGoals returns goals list on success`() = runTest {
        val goalUid = UUID.randomUUID().toString()
        val goals = listOf(
            SavingsGoal(
                goalUid,
                "Holiday",
                Money(FAKE_ACCOUNT_CURRENCY, 200L),
                Money(FAKE_ACCOUNT_CURRENCY, 100L),
                savedPercentage = 50,
                state = FAKE_SAVINGS_GOAL_STATE
            )
        )
        val response = GetSavingsGoalsResponse(goals)
        whenever(starlingService.getSavingsGoals(accountUid))
            .thenReturn(Response.success(response))

        val result = repository.getAccountSavingsGoals(accountUid)

        assertEquals(1, result?.size)
        assertEquals(goalUid, result?.first()?.savingsGoalUid)
    }

    @Test
    fun `getAccountSavingsGoals returns null on error`() = runTest {
        whenever(starlingService.getSavingsGoals(accountUid))
            .thenReturn(Response.error(500, "".toResponseBody(null)))

        val result = repository.getAccountSavingsGoals(accountUid)

        assertNull(result)
    }

    @Test
    fun `getAccountSavingsGoals returns null on exception`() = runTest {
        whenever(starlingService.getSavingsGoals(accountUid))
            .thenThrow(RuntimeException("fail"))

        val result = repository.getAccountSavingsGoals(accountUid)

        assertNull(result)
    }
}
