package com.owensteel.starlingroundup.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.owensteel.starlingroundup.model.AccountHolderIndividualResponse
import com.owensteel.starlingroundup.model.AccountResponse
import com.owensteel.starlingroundup.model.Money
import com.owensteel.starlingroundup.model.Transaction
import com.owensteel.starlingroundup.model.TransactionFeedResponse
import com.owensteel.starlingroundup.network.StarlingService
import com.owensteel.starlingroundup.token.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Response

/*

    ViewModel for doing the calculation work

 */

private const val GBP = "GBP"
private const val TRANSACTION_DIRECTION_OUT = "OUT"

class MainViewModel(
    application: Application,
    private val tokenManager: TokenManager
) : AndroidViewModel(application) {

    // Use StateFlow to maintain an observable mutable states for our
    // values that may change any time

    private val _hasInitialisedDataState = MutableStateFlow(false)
    val hasInitialisedDataState: StateFlow<Boolean> = _hasInitialisedDataState

    private val _roundUpAmountState = MutableStateFlow(
        Money(GBP, 0L).toString()
    )
    val roundUpAmountState: StateFlow<String> = _roundUpAmountState

    private val _feedState = MutableStateFlow(FeedUiState())
    val feedState: StateFlow<FeedUiState> = _feedState

    private val _accountHolderNameState = MutableStateFlow("")
    val accountHolderNameState: StateFlow<String> = _accountHolderNameState

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
                _hasInitialisedDataState.value = true

                // And now we have account details, fetch the transactions
                fetchWeeklyTransactions(context)
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
                    isLoading = false,
                    hasError = false
                )
            } else {
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
    private fun calculateAndDisplayRoundUp(transactions: List<Transaction>) {
        if (accountCurrency == null) return

        val total = transactions
            .filter { it.direction == TRANSACTION_DIRECTION_OUT } // spending only
            .map { it.amount.minorUnits }.sumOf {
                Money(accountCurrency!!, it).roundUp()
            }
        lastRoundUpTotal = total

        // Format and display total
        _roundUpAmountState.value = Money(
            accountCurrency!!,
            total
        ).toString()
    }

    // 3. Perform the transfer
    fun performTransfer() {
        viewModelScope.launch {
            // TODO: Trigger savings goal transfer
        }
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

data class FeedUiState(
    val value: List<Transaction>? = null,
    val isLoading: Boolean = true,
    val hasError: Boolean = false
)