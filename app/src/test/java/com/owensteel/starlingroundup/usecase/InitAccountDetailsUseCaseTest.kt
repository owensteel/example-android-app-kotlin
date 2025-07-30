package com.owensteel.starlingroundup.usecase

import com.owensteel.starlingroundup.domain.repository.AccountRepository
import com.owensteel.starlingroundup.model.Account
import com.owensteel.starlingroundup.model.AccountHolderIndividualResponse
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import java.util.Currency
import java.util.UUID

private const val FAKE_ACCOUNT_NAME = "Personal"
private const val FAKE_ACCOUNT_CURRENCY = "GBP"

class InitAccountDetailsUseCaseTest {

    private val accountRepository: AccountRepository = mock()
    private val testScheduler = TestCoroutineScheduler()
    private val testDispatcher = StandardTestDispatcher(testScheduler)

    private lateinit var initAccountDetailsUseCase: InitAccountDetailsUseCase

    private val accountUid = UUID.randomUUID().toString()
    private val categoryUid = UUID.randomUUID().toString()
    private val accountHolderName = "Joe"

    private val mockAccount = Account(
        accountUid,
        categoryUid,
        FAKE_ACCOUNT_CURRENCY,
        FAKE_ACCOUNT_NAME
    )

    private val mockAccountHolder = AccountHolderIndividualResponse(
        "Mr", accountHolderName, "Smith", "01/01/2000", "joe@example.com", "123456789"
    )

    @Before
    fun setUp() {
        initAccountDetailsUseCase = InitAccountDetailsUseCase(accountRepository, testDispatcher)
    }

    @Test
    fun `returns success when account and holder exist`() = runTest(testScheduler) {

        whenever(accountRepository.getPrimaryAccount()).thenReturn(mockAccount)
        whenever(accountRepository.getAccountHolder()).thenReturn(mockAccountHolder)

        val result = initAccountDetailsUseCase()

        assertTrue(result.isSuccess)
        val accountDetails = result.getOrNull()
        assertNotNull(accountDetails)
        assertEquals(accountUid, accountDetails!!.accountUid)
        assertEquals(categoryUid, accountDetails.categoryUid)
        assertEquals(accountHolderName, accountDetails.accountHolderName)
        assertEquals(Currency.getInstance(FAKE_ACCOUNT_CURRENCY), accountDetails.currency)

    }

    @Test
    fun `returns failure when account or holder is null`() = runTest(testScheduler) {
        // Account is null
        whenever(accountRepository.getPrimaryAccount()).thenReturn(null)
        whenever(accountRepository.getAccountHolder()).thenReturn(mockAccountHolder)

        val result1 = initAccountDetailsUseCase()
        assertTrue(result1.isFailure)
        assertTrue(result1.exceptionOrNull() is IllegalStateException)

        // Holder is null
        whenever(accountRepository.getPrimaryAccount()).thenReturn(mockAccount)
        whenever(accountRepository.getAccountHolder()).thenReturn(null)

        val result2 = initAccountDetailsUseCase()
        assertTrue(result2.isFailure)
        assertTrue(result2.exceptionOrNull() is IllegalStateException)
    }

    @Test
    fun `returns failure when repository throws`() = runTest(testScheduler) {
        whenever(accountRepository.getPrimaryAccount()).thenThrow(RuntimeException("boom"))
        whenever(accountRepository.getAccountHolder()).thenReturn(mockAccountHolder)

        val result = initAccountDetailsUseCase()
        assertTrue(result.isFailure)
        assertEquals("boom", result.exceptionOrNull()?.message)
    }

}