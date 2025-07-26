package com.owensteel.starlingroundup.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.owensteel.starlingroundup.data.local.SecureTokenStore
import com.owensteel.starlingroundup.model.AccountHolderIndividualResponse
import com.owensteel.starlingroundup.model.AccountResponse
import com.owensteel.starlingroundup.model.GetSavingsGoalsResponse
import com.owensteel.starlingroundup.model.Money
import com.owensteel.starlingroundup.model.SavingsGoal
import com.owensteel.starlingroundup.model.Transaction
import com.owensteel.starlingroundup.model.TransactionFeedResponse
import com.owensteel.starlingroundup.model.TransferRequest
import com.owensteel.starlingroundup.model.TransferResponse
import com.owensteel.starlingroundup.network.StarlingService
import com.owensteel.starlingroundup.token.TokenManager
import com.owensteel.starlingroundup.util.MoneyUtils.roundUp
import com.owensteel.starlingroundup.util.SharedConstants.Transactions.TRANSACTION_DIRECTION_OUT
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Response
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject

/*

    ViewModel for doing the calculation work

 */

private const val GBP = "GBP"

private val roundUpCutoffTimestampFallback = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(
    Instant.EPOCH.atOffset(
        ZoneOffset.UTC
    )
)

@HiltViewModel
class RoundUpAndSaveViewModel @Inject constructor(
    application: Application,
    private val tokenManager: TokenManager
) : AndroidViewModel(application) {

    // Use StateFlow to maintain an observable mutable states for our
    // values that may change any time

    private val _hasInitialisedDataState = MutableStateFlow(HasInitialisedDataState())
    val hasInitialisedDataState: StateFlow<HasInitialisedDataState> = _hasInitialisedDataState

    private val _roundUpAmountState = MutableStateFlow(
        Money(GBP, 0L).toString()
    )
    val roundUpAmountState: StateFlow<String> = _roundUpAmountState

    private val _feedState = MutableStateFlow(FeedUiState())
    val feedState: StateFlow<FeedUiState> = _feedState

    private val _savingsGoalsModalUiState = MutableStateFlow(SavingsGoalsModalUiState())
    val savingsGoalsModalUiState: StateFlow<SavingsGoalsModalUiState> = _savingsGoalsModalUiState

    private val _accountHolderNameState = MutableStateFlow("")
    val accountHolderNameState: StateFlow<String> = _accountHolderNameState

    // Handle an error occurring during initialisation of
    // account data
    private fun handleInitialisationError(responseCode: Int) {
        // TODO: Handle specific errors
        _hasInitialisedDataState.value = _hasInitialisedDataState.value.copy(
            value = false,
            isLoading = false,
            hasError = true,
            errorCode = responseCode
        )
    }

    // 0. Fetch account details
    private var accountUid: String? = null
    private var categoryUid: String? = null
    private var accountCurrency: String? = null

    private fun initialiseAccountDetails(context: Context) {
        viewModelScope.launch {
            val accountResponse: Response<AccountResponse> = StarlingService.getAccountDetails(
                context, tokenManager
            )
            if (accountResponse.isSuccessful) {
                accountResponse.body()?.accounts?.firstOrNull()?.let { account ->
                    accountUid = account.accountUid
                    categoryUid = account.defaultCategory
                    accountCurrency = account.currency

                    // Get account holder details
                    fetchAccountHolderDetails(context)
                }
            } else {
                handleInitialisationError(accountResponse.code())
            }
        }
    }

    private fun fetchAccountHolderDetails(context: Context) {
        viewModelScope.launch {
            val accountHolderIndividualResponse: Response<AccountHolderIndividualResponse> =
                StarlingService.getAccountHolderIndividual(
                    context, tokenManager
                )
            if (accountHolderIndividualResponse.isSuccessful) {
                accountHolderIndividualResponse.body()?.let {
                    _accountHolderNameState.value = it.firstName
                }

                // Now we have user's complete details, display feature UI
                _hasInitialisedDataState.value = _hasInitialisedDataState.value.copy(
                    value = true,
                    isLoading = false,
                    hasError = false
                )

                // And now we have account details, fetch the transactions
                fetchWeeklyTransactions(context)
            } else {
                handleInitialisationError(accountHolderIndividualResponse.code())
            }
        }
    }

    // 1. Fetch this week's transactions
    private var transactionsCached: List<Transaction> = emptyList()
    private fun fetchWeeklyTransactions(context: Context) {
        if (accountUid == null || categoryUid == null) return

        viewModelScope.launch {
            _feedState.value = _feedState.value.copy(
                value = null,
                isLoading = true,
                hasError = false
            )

            val transactionFeedResponse: Response<TransactionFeedResponse> =
                StarlingService.getTransactionsForCurrentWeek(
                    context,
                    tokenManager,
                    accountUid!!,
                    categoryUid!!
                )
            if (transactionFeedResponse.isSuccessful) {
                val transactions = transactionFeedResponse.body()?.feedItems.orEmpty()
                transactionsCached = transactions

                // Round up amount
                calculateAndDisplayRoundUp(transactions)

                // Feed UI
                _feedState.value = _feedState.value.copy(
                    value = transactions,
                    latestRoundUpCutoffTimestamp = getLatestRoundUpCutoffTimestamp(),
                    isLoading = false,
                    hasError = false
                )
            } else {
                // Error loading transactions feed
                _feedState.value = _feedState.value.copy(
                    value = null,
                    isLoading = false,
                    hasError = true
                )
            }
        }
    }

    // 2. Calculate the round-up
    private var lastRoundUpTotal: Long = 0L
    private suspend fun calculateAndDisplayRoundUp(transactions: List<Transaction>) {
        if (accountCurrency == null) return

        val latestRoundUpCutoffTimestampInstant = Instant.parse(getLatestRoundUpCutoffTimestamp())

        val total = transactions
            .filter {
                // spending only
                it.direction == TRANSACTION_DIRECTION_OUT
                        // only count transactions after the
                        // last recorded round-up time
                        && Instant.parse(it.transactionTime) > latestRoundUpCutoffTimestampInstant
            }
            .map { it.amount.minorUnits }.sumOf {
                roundUp(it)
            }
        lastRoundUpTotal = total

        // Format and display total
        _roundUpAmountState.value = Money(
            accountCurrency!!,
            total
        ).toString()
    }

    // 3. Get savings goals
    private var savingsGoalsCached: List<SavingsGoal>? = null
    fun getAccountSavingsGoals() {
        if (accountUid == null) return

        viewModelScope.launch {
            // Reset to loading only
            _savingsGoalsModalUiState.value = _savingsGoalsModalUiState.value.copy(
                value = emptyList(),
                isLoading = true,
                hasLoadingError = false,
                hasTransferError = false
            )

            val getSavingsGoalsResponse: Response<GetSavingsGoalsResponse> =
                StarlingService.getSavingsGoals(
                    tokenManager,
                    accountUid!!
                )
            if (getSavingsGoalsResponse.isSuccessful) {
                val savingsGoals = getSavingsGoalsResponse.body()?.savingsGoalList.orEmpty()
                savingsGoalsCached = savingsGoals

                // Update UI
                _savingsGoalsModalUiState.value = _savingsGoalsModalUiState.value.copy(
                    value = savingsGoals,
                    isLoading = false,
                    hasLoadingError = false
                )
            } else {
                // Error loading transactions feed
                _savingsGoalsModalUiState.value = _savingsGoalsModalUiState.value.copy(
                    value = emptyList(),
                    isLoading = false,
                    hasLoadingError = true
                )
            }
        }
    }

    // 4. Perform the transfer to selected savings goal
    fun performTransferToSavingsGoal(
        savingsGoalUid: String,
        showTransferToSavingsSheet: MutableState<Boolean>
    ) {
        // Generate UID for transfer
        val transferUid = UUID.randomUUID().toString()

        viewModelScope.launch {
            // Show spinner in UI in case API hangs
            _savingsGoalsModalUiState.value = _savingsGoalsModalUiState.value.copy(
                isLoading = true,
                hasTransferError = false
            )

            val transferRequest = TransferRequest(
                amount = lastRoundUpTotal
            )
            val transferResponse: Response<TransferResponse> =
                StarlingService.transferToSavingsGoal(
                    tokenManager,
                    accountUid!!,
                    savingsGoalUid,
                    transferUid,
                    transferRequest,
                )
            if (transferResponse.isSuccessful) {
                // Record that the round-up for these
                // transactions has now been done
                recordLatestRoundUpCutoffTimestamp()
                // Hide modal
                showTransferToSavingsSheet.value = false
            } else {
                // Handle error
                _savingsGoalsModalUiState.value = _savingsGoalsModalUiState.value.copy(
                    isLoading = false,
                    hasTransferError = true
                )
            }
        }
    }

    // Prevent rounded-up transactions from being counted in a
    // future round-up calculation by recording a cut-off time

    private val tokenStore = SecureTokenStore(application.applicationContext)

    private suspend fun recordLatestRoundUpCutoffTimestamp() {
        // Use time and date of latest cached transaction (the
        // latest transaction included in the round-up) instead
        // of current timestamp in case somehow a transaction
        // occurs between these two times
        val cutoffTimestamp = transactionsCached.first().transactionTime
        tokenStore.saveLatestRoundUpCutoffTimestamp(cutoffTimestamp)
    }

    private suspend fun getLatestRoundUpCutoffTimestamp(): String {
        return tokenStore.getLatestRoundUpCutOffTimestamp() ?: roundUpCutoffTimestampFallback
    }

    // Run as soon as initialised
    init {
        // Prevent context leak by getting and
        // passing it just-in-time
        val context = application.applicationContext

        // Will call fetch methods sequentially
        viewModelScope.launch {
            initialiseAccountDetails(context)
        }
    }

}

data class SavingsGoalsModalUiState(
    val value: List<SavingsGoal> = emptyList(),
    val isLoading: Boolean = true,
    val hasLoadingError: Boolean = false,
    val hasTransferError: Boolean = false
)

data class FeedUiState(
    val value: List<Transaction>? = null,
    val latestRoundUpCutoffTimestamp: String = roundUpCutoffTimestampFallback,
    val isLoading: Boolean = true,
    val hasError: Boolean = false
)

data class HasInitialisedDataState(
    val value: Boolean = false,
    val isLoading: Boolean = true,
    val hasError: Boolean = false,
    val errorCode: Int? = null
)