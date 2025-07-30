package com.owensteel.starlingroundup.domain.repository

import com.owensteel.starlingroundup.model.Account
import com.owensteel.starlingroundup.model.AccountHolderIndividualResponse
import com.owensteel.starlingroundup.model.AccountResponse
import com.owensteel.starlingroundup.network.StarlingService
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import retrofit2.Response
import java.util.UUID

private const val FAKE_ACCOUNT_NAME = "Personal"
private const val FAKE_ACCOUNT_CURRENCY = "GBP"

class AccountRepositoryImplTest {

    private lateinit var repository: AccountRepositoryImpl
    private val starlingService: StarlingService = mock()

    private val accountUid = UUID.randomUUID().toString()
    private val categoryUid = UUID.randomUUID().toString()

    private val mockAccount = Account(
        accountUid,
        categoryUid,
        FAKE_ACCOUNT_NAME,
        FAKE_ACCOUNT_CURRENCY
    )

    private val mockAccountHolder = AccountHolderIndividualResponse(
        "Mr", "Joe", "Smith", "01/01/2000", "joe@example.com", "123456789"
    )

    @Before
    fun setUp() {
        repository = AccountRepositoryImpl(starlingService)
    }

    @Test
    fun `getPrimaryAccount returns first account on success`() = runTest {
        val response = AccountResponse(listOf(mockAccount))
        whenever(starlingService.getAccountDetails())
            .thenReturn(Response.success(response))

        val result = repository.getPrimaryAccount()

        assertNotNull(result)
        assertEquals(accountUid, result?.accountUid)
    }

    @Test
    fun `getPrimaryAccount returns null if no accounts`() = runTest {
        val response = AccountResponse(emptyList())
        whenever(starlingService.getAccountDetails())
            .thenReturn(Response.success(response))

        val result = repository.getPrimaryAccount()

        assertNull(result)
    }

    @Test
    fun `getPrimaryAccount returns null on error response`() = runTest {
        whenever(starlingService.getAccountDetails())
            .thenReturn(Response.error(404, "".toResponseBody(null)))

        val result = repository.getPrimaryAccount()

        assertNull(result)
    }

    @Test
    fun `getPrimaryAccount returns null on exception`() = runTest {
        whenever(starlingService.getAccountDetails())
            .thenThrow(RuntimeException("boom"))

        val result = repository.getPrimaryAccount()

        assertNull(result)
    }

    @Test
    fun `getAccountHolder returns response body on success`() = runTest {
        val holder = mockAccountHolder
        whenever(starlingService.getAccountHolderIndividual())
            .thenReturn(Response.success(holder))

        val result = repository.getAccountHolder()

        assertNotNull(result)
        assertEquals(mockAccountHolder.firstName, result?.firstName)
    }

    @Test
    fun `getAccountHolder returns null on error response`() = runTest {
        whenever(starlingService.getAccountHolderIndividual())
            .thenReturn(Response.error(403, "".toResponseBody(null)))

        val result = repository.getAccountHolder()

        assertNull(result)
    }

    @Test
    fun `getAccountHolder returns null on exception`() = runTest {
        whenever(starlingService.getAccountHolderIndividual())
            .thenThrow(RuntimeException("fail"))

        val result = repository.getAccountHolder()

        assertNull(result)
    }
}
