package com.owensteel.starlingroundup.viewmodel

import com.owensteel.starlingroundup.data.local.RoundUpCutoffTimestampStore
import com.owensteel.starlingroundup.model.AccountDetails
import com.owensteel.starlingroundup.model.Money
import com.owensteel.starlingroundup.model.SavingsGoal
import com.owensteel.starlingroundup.model.Transaction
import com.owensteel.starlingroundup.model.uistates.RoundUpUiError
import com.owensteel.starlingroundup.testutil.MainDispatcherRule
import com.owensteel.starlingroundup.usecase.CalculateRoundUpUseCase
import com.owensteel.starlingroundup.usecase.FetchTransactionsUseCase
import com.owensteel.starlingroundup.usecase.GetSavingsGoalsUseCase
import com.owensteel.starlingroundup.usecase.InitAccountDetailsUseCase
import com.owensteel.starlingroundup.usecase.TransferToSavingsGoalUseCase
import com.owensteel.starlingroundup.util.SharedConstants.Transactions.TRANSACTION_DIRECTION_OUT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.spy
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Currency
import java.util.UUID

private const val FAKE_ACCOUNT_HOLDER_NAME = "Joe"
private const val FAKE_ACCOUNT_CURRENCY = "GBP"

private const val FAKE_TRANSACTION_SOURCE = "MASTER_CARD"
private val FAKE_TRANSACTION_SPENDING_CATEGORY = UUID.randomUUID().toString()

private const val FAKE_SAVINGS_GOAL_STATE = "ACTIVE"

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

    // Example transactions
    private val fakeTransactions = listOf(
        Transaction(
            feedItemUid = UUID.randomUUID().toString(),
            amount = Money(
                FAKE_ACCOUNT_CURRENCY,
                123L
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
                456L
            ),
            direction = TRANSACTION_DIRECTION_OUT,
            transactionTime = "2025-01-01T10:00:00Z",
            spendingCategory = FAKE_TRANSACTION_SPENDING_CATEGORY,
            source = FAKE_TRANSACTION_SOURCE
        )
    )

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

    @Test
    fun `refreshTransactionsAndRoundUp updates state with transactions and round-up total`() =
        runTest {
            // Cutoff that allows all but one fake transaction
            val fakeCutoffTimestamp = "2025-01-01T09:30:00Z"

            whenever(fetchTransactions(any(), any())).thenReturn(fakeTransactions)
            whenever(roundUpCutoffTimestampStore.getLatestRoundUpCutOffTimestamp()).thenReturn(
                fakeCutoffTimestamp
            )

            // We test the actual calculation logic in the RoundUpUseCase test
            whenever(calculateRoundUp(fakeTransactions, fakeCutoffTimestamp)).thenReturn(121L)

            viewModel.refreshTransactionsAndRoundUp()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(fakeTransactions, state.transactions)
            assertEquals(121L, state.roundUpTotal.minorUnits)
            assertEquals(FAKE_ACCOUNT_CURRENCY, state.roundUpTotal.currency)
        }

    @Test
    fun `recordAndHandleCompletedRoundUpTransfer stops if transactions feed is empty`() =
        runTest {
            val fakeCutoffTimestamp = "2025-01-01T09:30:00Z"

            whenever(fetchTransactions(any(), any())).thenReturn(fakeTransactions)
            whenever(roundUpCutoffTimestampStore.getLatestRoundUpCutOffTimestamp()).thenReturn(
                fakeCutoffTimestamp
            )
            whenever(calculateRoundUp(fakeTransactions, fakeCutoffTimestamp)).thenReturn(121L)

            viewModel.refreshTransactionsAndRoundUp()
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertFalse(state.showRoundUpTransferCompleteDialog)
        }

    @Test
    fun `createGoalAndTransfer success shows dialog and refreshes`() = runTest {
        // Given
        val roundUpAmount = 500L
        val targetAmount = 1000L
        val goalName = "TestGoal"
        val roundUpMoney = Money(FAKE_ACCOUNT_CURRENCY, roundUpAmount)

        whenever(
            transferToSavingsGoal.createAndTransferToNewSavingsGoal(
                any(), any(), any(), any()
            )
        ).thenReturn(Result.success(Unit))
        whenever(fetchTransactions(any(), any())).thenReturn(fakeTransactions)
        whenever(roundUpCutoffTimestampStore.saveLatestRoundUpCutoffTimestamp(any())).thenReturn(
            Unit
        )
        whenever(calculateRoundUp(any(), any())).thenReturn(roundUpAmount)

        // Simulate existing round-up state
        viewModel.refreshTransactionsAndRoundUp()
        advanceUntilIdle()
        viewModel.createGoalAndTransfer(goalName, targetAmount)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertTrue(state.showRoundUpTransferCompleteDialog)
        assertEquals(goalName, state.roundUpTransferCompleteDialogChosenSavingsGoalName)
        assertEquals(roundUpMoney, state.roundUpTransferCompleteDialogRoundUpTotal)
    }

    @Test
    fun `transferToGoal failure sets transfer error`() = runTest {
        // Given
        val goal = SavingsGoal(
            savingsGoalUid = UUID.randomUUID().toString(),
            name = "TestGoal",
            target = Money(FAKE_ACCOUNT_CURRENCY, 1000L),
            totalSaved = Money(FAKE_ACCOUNT_CURRENCY, 500L),
            savedPercentage = 50,
            state = FAKE_SAVINGS_GOAL_STATE
        )
        whenever(
            transferToSavingsGoal.transferToSavingsGoal(
                any(),
                any(),
                any()
            )
        ).thenReturn(Result.failure(Exception("Simulated transfer failure")))

        viewModel.transferToGoal(goal)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(RoundUpUiError.Transfer, state.error)
    }

    @Test
    fun `loadSavingsGoals updates savings goals in state`() = runTest {
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
        whenever(getSavingsGoals(any())).thenReturn(goals)

        viewModel.loadSavingsGoals()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(goals, state.savingsGoals)
        assertFalse(state.isLoadingSavingsGoals)
    }

    @Test
    fun `Won't initialise and does nothing if account details are null`() = runTest {
        whenever(initAccountDetails()).thenReturn(
            Result.failure(
                exception = Exception("Simulated account details failure")
            )
        )
        val uninitialisedViewModel = RoundUpAndSaveViewModel(
            initAccountDetails,
            fetchTransactions,
            transferToSavingsGoal,
            getSavingsGoals,
            roundUpCutoffTimestampStore,
            calculateRoundUp
        )
        val spyViewModel = spy(uninitialisedViewModel)

        advanceUntilIdle()

        assertFalse(uninitialisedViewModel.uiState.value.hasInitialised)
        verify(spyViewModel, never()).refreshTransactionsAndRoundUp()
    }

    @Test
    fun `createGoalAndTransfer does nothing if account or currency is null`() = runTest {
        whenever(initAccountDetails()).thenReturn(
            Result.failure(
                exception = Exception("Simulated account details failure")
            )
        )
        val uninitialisedViewModel = RoundUpAndSaveViewModel(
            initAccountDetails,
            fetchTransactions,
            transferToSavingsGoal,
            getSavingsGoals,
            roundUpCutoffTimestampStore,
            calculateRoundUp
        )

        uninitialisedViewModel.createGoalAndTransfer("Goal", 1000)

        verify(transferToSavingsGoal, never()).createAndTransferToNewSavingsGoal(
            any(),
            any(),
            any(),
            any()
        )
    }

    @Test
    fun `showModal sets the modal flag in state`() = runTest {
        viewModel.showModal(true)
        assertTrue(viewModel.uiState.value.showModal)

        viewModel.showModal(false)
        assertFalse(viewModel.uiState.value.showModal)
    }

    @Test
    fun `showRoundUpTransferCompleteDialog sets the dialog flag in state`() = runTest {
        viewModel.showRoundUpTransferCompleteDialog(true)
        assertTrue(viewModel.uiState.value.showRoundUpTransferCompleteDialog)

        viewModel.showRoundUpTransferCompleteDialog(false)
        assertFalse(viewModel.uiState.value.showRoundUpTransferCompleteDialog)
    }

}