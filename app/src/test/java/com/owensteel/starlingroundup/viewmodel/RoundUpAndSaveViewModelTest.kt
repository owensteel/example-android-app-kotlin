package com.owensteel.starlingroundup.viewmodel

import com.owensteel.starlingroundup.data.local.RoundUpCutoffTimestampStore
import com.owensteel.starlingroundup.model.AccountDetails
import com.owensteel.starlingroundup.testutil.MainDispatcherRule
import com.owensteel.starlingroundup.usecase.CalculateRoundUpUseCase
import com.owensteel.starlingroundup.usecase.FetchTransactionsUseCase
import com.owensteel.starlingroundup.usecase.GetSavingsGoalsUseCase
import com.owensteel.starlingroundup.usecase.InitAccountDetailsUseCase
import com.owensteel.starlingroundup.usecase.TransferToSavingsGoalUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import java.util.Currency
import java.util.UUID

private const val FAKE_ACCOUNT_HOLDER_NAME = "Joe"
private const val FAKE_ACCOUNT_CURRENCY = "GBP"

@OptIn(ExperimentalCoroutinesApi::class)
class RoundUpAndSaveViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: RoundUpAndSaveViewModel

    private val initAccountDetails: InitAccountDetailsUseCase = mock()
    private val fetchTransactions: FetchTransactionsUseCase = mock()
    private val transferToSavingsGoal: TransferToSavingsGoalUseCase = mock()
    private val getSavingsGoals: GetSavingsGoalsUseCase = mock()
    private val roundUpCutoffTimestampStore: RoundUpCutoffTimestampStore = mock()
    private val calculateRoundUp: CalculateRoundUpUseCase = mock()

    @Before
    fun setUp() = runBlocking {
        Dispatchers.setMain(UnconfinedTestDispatcher())

        // Default mocks
        whenever(initAccountDetails()).thenReturn(
            Result.success(
                AccountDetails(
                    accountUid = UUID.randomUUID().toString(),
                    categoryUid = UUID.randomUUID().toString(),
                    accountHolderName = FAKE_ACCOUNT_HOLDER_NAME,
                    currency = Currency.getInstance(FAKE_ACCOUNT_CURRENCY)
                )
            )
        )
        whenever(fetchTransactions(any(), any())).thenReturn(emptyList())
        whenever(roundUpCutoffTimestampStore.getLatestRoundUpCutOffTimestamp()).thenReturn(null)
        whenever(calculateRoundUp(any(), any())).thenReturn(0L)

        viewModel = RoundUpAndSaveViewModel(
            initAccountDetails,
            fetchTransactions,
            transferToSavingsGoal,
            getSavingsGoals,
            roundUpCutoffTimestampStore,
            calculateRoundUp
        )
    }

    @Test
    fun `initialise should populate UI state with account details`() = runTest {
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(FAKE_ACCOUNT_HOLDER_NAME, state.accountHolderName)
        assertEquals(Currency.getInstance(FAKE_ACCOUNT_CURRENCY), state.accountCurrency)
        assertTrue(state.hasInitialised)
    }

}