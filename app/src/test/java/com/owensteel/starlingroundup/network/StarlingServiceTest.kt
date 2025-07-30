package com.owensteel.starlingroundup.network

import com.owensteel.starlingroundup.model.Account
import com.owensteel.starlingroundup.model.AccountHolderIndividualResponse
import com.owensteel.starlingroundup.model.AccountResponse
import com.owensteel.starlingroundup.model.CreateSavingsGoalRequest
import com.owensteel.starlingroundup.model.CreateSavingsGoalResponse
import com.owensteel.starlingroundup.model.GetSavingsGoalsResponse
import com.owensteel.starlingroundup.model.Money
import com.owensteel.starlingroundup.model.TransactionFeedResponse
import com.owensteel.starlingroundup.model.TransferRequest
import com.owensteel.starlingroundup.model.TransferResponse
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.whenever
import retrofit2.Response
import java.time.ZonedDateTime
import java.util.UUID

private const val FAKE_ACCOUNT_NAME = "Personal"
private const val FAKE_ACCOUNT_CURRENCY = "GBP"

class StarlingServiceTest {

    private lateinit var starlingService: StarlingService
    private lateinit var mockApi: StarlingApi

    private val mockApiProvider: StarlingApiProvider = mock()

    private val accountUid = UUID.randomUUID().toString()
    private val categoryUid = UUID.randomUUID().toString()

    private val mockAccount = Account(
        accountUid,
        categoryUid,
        FAKE_ACCOUNT_CURRENCY,
        FAKE_ACCOUNT_NAME
    )

    @Before
    fun setup() {
        mockApi = mock()
        whenever(mockApiProvider.getApi()).thenReturn(mockApi)
        starlingService = StarlingService(mockApiProvider)
    }

    @Test
    fun `getAccountDetails returns success`() = runTest {
        val expected = AccountResponse(
            accounts = listOf(mockAccount)
        )
        whenever(mockApi.getAccountDetails()).thenReturn(Response.success(expected))

        val result = starlingService.getAccountDetails()

        assertTrue(result.isSuccessful)
        assertEquals(expected, result.body())
    }

    @Test
    fun `getAccountHolderIndividual returns 403 error`() = runTest {
        val errorResponse = Response.error<AccountHolderIndividualResponse>(
            403, "".toResponseBody(null)
        )
        whenever(mockApi.getAccountHolderIndividual()).thenReturn(errorResponse)

        val result = starlingService.getAccountHolderIndividual()

        assertFalse(result.isSuccessful)
        assertEquals(403, result.code())
    }

    @Test
    fun `getSavingsGoals returns goals`() = runTest {
        val mockGoals = GetSavingsGoalsResponse(listOf())
        whenever(mockApi.getSavingsGoals(accountUid)).thenReturn(Response.success(mockGoals))

        val result = starlingService.getSavingsGoals(accountUid)

        assertTrue(result.isSuccessful)
        assertEquals(mockGoals, result.body())
    }

    @Test
    fun `createSavingsGoal returns expected response`() = runTest {
        val savingsGoalUid = UUID.randomUUID().toString()

        val request = CreateSavingsGoalRequest(
            "Test",
            FAKE_ACCOUNT_CURRENCY,
            Money(
                FAKE_ACCOUNT_CURRENCY,
                100L
            ),
            null
        )
        val expectedResponse = CreateSavingsGoalResponse(
            savingsGoalUid, true
        )
        whenever(mockApi.createSavingsGoal(accountUid, request)).thenReturn(
            Response.success(
                expectedResponse
            )
        )

        val result = starlingService.createSavingsGoal(accountUid, request)

        assertTrue(result.isSuccessful)
        assertEquals(savingsGoalUid, result.body()?.savingsGoalUid)
    }

    @Test
    fun `transferToSavingsGoal handles error response`() = runTest {
        val savingsGoalUid = UUID.randomUUID().toString()
        val transferUid = UUID.randomUUID().toString()

        val request = TransferRequest(
            Money(
                FAKE_ACCOUNT_CURRENCY,
                100L
            )
        )
        val response = Response.error<TransferResponse>(400, "".toResponseBody(null))
        whenever(
            mockApi.transferToSavingsGoal(
                accountUid,
                savingsGoalUid,
                transferUid,
                request
            )
        ).thenReturn(
            response
        )

        val result = starlingService.transferToSavingsGoal(
            accountUid,
            savingsGoalUid,
            transferUid,
            request
        )
        assertFalse(result.isSuccessful)
        assertEquals(400, result.code())
    }

    @Test
    fun `getTransactionsForCurrentWeek sends correct params`() = runTest {
        val captor = argumentCaptor<String>()
        val response = TransactionFeedResponse(listOf())
        whenever(
            mockApi.getTransactionsForCurrentWeek(
                accountUid = any(),
                categoryUid = any(),
                fromIso = captor.capture(),
                toIso = captor.capture()
            )
        ).thenReturn(Response.success(response))

        val result = starlingService.getTransactionsForCurrentWeek(accountUid, categoryUid)

        assertTrue(result.isSuccessful)
        assertEquals(2, captor.allValues.size)
        val from = ZonedDateTime.parse(captor.firstValue)
        val to = ZonedDateTime.parse(captor.secondValue)
        assertTrue(from.isBefore(to))
    }
}
