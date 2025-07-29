package com.owensteel.starlingroundup.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.owensteel.starlingroundup.data.local.RoundUpCutoffTimestampStore
import com.owensteel.starlingroundup.domain.model.AccountDetails
import com.owensteel.starlingroundup.model.Money
import com.owensteel.starlingroundup.model.Transaction
import com.owensteel.starlingroundup.model.uistates.RoundUpUiError
import com.owensteel.starlingroundup.model.uistates.RoundUpUiState
import com.owensteel.starlingroundup.usecase.CalculateRoundUpUseCase
import com.owensteel.starlingroundup.usecase.FetchTransactionsUseCase
import com.owensteel.starlingroundup.usecase.GetSavingsGoalsUseCase
import com.owensteel.starlingroundup.usecase.InitAccountDetailsUseCase
import com.owensteel.starlingroundup.usecase.TransferToSavingsGoalUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Currency
import javax.inject.Inject

private val roundUpCutoffTimestampFallback = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(
    Instant.EPOCH.atOffset(
        ZoneOffset.UTC
    )
)

@HiltViewModel
class RoundUpAndSaveViewModel @Inject constructor(
    private val initAccountDetails: InitAccountDetailsUseCase,
    private val fetchTransactions: FetchTransactionsUseCase,
    private val transferToSavingsGoal: TransferToSavingsGoalUseCase,
    private val getSavingsGoals: GetSavingsGoalsUseCase,
    private val roundUpCutoffTimestampStore: RoundUpCutoffTimestampStore,
    private val calculateRoundUp: CalculateRoundUpUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoundUpUiState())
    val uiState: StateFlow<RoundUpUiState> = _uiState

    private var accountUid: String? = null
    private var categoryUid: String? = null
    private var currency: Currency? = null

    private var cachedTransactions: List<Transaction> = emptyList()
    private var lastRoundUpTotal: Long = 0L

    init {
        viewModelScope.launch {
            initialise()
        }
    }

    private suspend fun initialise() {
        _uiState.update {
            it.copy(hasInitialised = false)
        }

        val accountResult = initAccountDetails()
        if (accountResult.isSuccess && accountResult.getOrNull() != null) {
            val accountDetails: AccountDetails = accountResult.getOrNull()!!

            accountUid = accountDetails.accountUid
            categoryUid = accountDetails.categoryUid
            currency = accountDetails.currency

            _uiState.update {
                it.copy(
                    accountHolderName = accountDetails.accountHolderName,
                    accountCurrency = currency,
                    hasInitialised = true
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    hasInitialised = false,
                    error = RoundUpUiError.Initialisation
                )
            }
        }
    }

    fun refreshTransactionsAndRoundUp() {
        viewModelScope.launch {
            if (accountUid == null || categoryUid == null || currency == null) {
                return@launch
            }

            _uiState.update {
                it.copy(
                    isLoadingTransactionsFeed = true
                )
            }

            val fetchedTransactions = fetchTransactions(accountUid!!, categoryUid!!)
            cachedTransactions = fetchedTransactions

            val cutoff = roundUpCutoffTimestampStore.getLatestRoundUpCutOffTimestamp()
                ?: roundUpCutoffTimestampFallback
            val roundUp = calculateRoundUp(fetchedTransactions, cutoff)
            lastRoundUpTotal = roundUp

            _uiState.update {
                it.copy(
                    transactions = fetchedTransactions,
                    roundUpTotal = Money(
                        currency!!.currencyCode,
                        roundUp
                    ),
                    cutoffTimestamp = cutoff,
                    isLoadingTransactionsFeed = false
                )
            }
        }
    }

    fun loadSavingsGoals() {
        viewModelScope.launch {
            if (accountUid == null) {
                return@launch
            }

            _uiState.update { it.copy(isLoadingSavingsGoals = true) }

            val goals = getSavingsGoals(accountUid!!)

            _uiState.update {
                it.copy(savingsGoals = goals, isLoadingSavingsGoals = false)
            }
        }
    }

    // TODO: is this possibly duplicate of transferToGoal?
    fun createGoalAndTransfer(name: String, targetMinorUnits: Long) {
        viewModelScope.launch {
            if (accountUid == null || currency == null) {
                return@launch
            }

            val createAndTransferResult = transferToSavingsGoal.createAndTransferToNewSavingsGoal(
                accountUid!!,
                name,
                Money(
                    currency!!.currencyCode,
                    targetMinorUnits
                ),
                Money(
                    currency!!.currencyCode,
                    lastRoundUpTotal
                )
            )
            if (createAndTransferResult.isSuccess) {
                // Record last transaction rounded-up
                // ("first" is last in the chronological order
                // of the transactions)
                roundUpCutoffTimestampStore.saveLatestRoundUpCutoffTimestamp(
                    cachedTransactions.first().transactionTime
                )

                // Refresh transactions for UX
                refreshTransactionsAndRoundUp()
                _uiState.update { it.copy(showModal = false) }
            } else {
                _uiState.update { it.copy(error = RoundUpUiError.Transfer) }
            }
        }
    }

    fun transferToGoal(goalUid: String) {
        viewModelScope.launch {
            if (accountUid == null || currency == null) {
                return@launch
            }

            val transferResult = transferToSavingsGoal.transferToSavingsGoal(
                accountUid!!,
                goalUid,
                Money(
                    currency!!.currencyCode,
                    lastRoundUpTotal
                )
            )
            if (transferResult.isSuccess) {
                // Record last transaction rounded-up
                // ("first" is last in the chronological order
                // of the transactions)
                roundUpCutoffTimestampStore.saveLatestRoundUpCutoffTimestamp(
                    cachedTransactions.first().transactionTime
                )

                // Refresh transactions for UX
                refreshTransactionsAndRoundUp()
                _uiState.update { it.copy(showModal = false) }
            } else {
                _uiState.update { it.copy(error = RoundUpUiError.Transfer) }
            }
        }
    }

    fun showModal(show: Boolean) {
        _uiState.update { it.copy(showModal = show) }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }

}