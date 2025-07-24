package com.owensteel.starlingroundup.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.owensteel.starlingroundup.model.AccountResponse
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

class MainViewModel(
    private val context: Context,
    private val tokenManager: TokenManager
) : ViewModel() {

    // Use StateFlow to maintain an observable mutable state for our
    // value that may change between appearances
    private val _roundUpAmountState = MutableStateFlow<String>("£0.00")
    val roundUpAmountState: StateFlow<String> = _roundUpAmountState

    private val _feedState = MutableStateFlow<TransactionFeedResponse?>(null)
    val feedState: StateFlow<TransactionFeedResponse?> = _feedState

    // 0. Fetch account details
    private var accountUid: String? = null
    private var categoryUid: String? = null

    private fun initialiseAccountDetails() {
        viewModelScope.launch {
            val accountResponse: Response<AccountResponse> = StarlingService.getAccountDetails(
                context, tokenManager
            )
            if (accountResponse.isSuccessful) {
                accountResponse.body()?.accounts?.firstOrNull()?.let { account ->
                    accountUid = account.accountUid
                    categoryUid = account.defaultCategory
                }
            }
        }
    }

    // 1. Fetch this week's transactions
    private fun fetchWeeklyTransactions() {
        if (accountUid == null || categoryUid == null) return

        viewModelScope.launch {
            val transactionFeedResponse: Response<TransactionFeedResponse> =
                StarlingService.getTransactionsForCurrentWeek(
                    context,
                    tokenManager,
                    accountUid!!,
                    categoryUid!!
                )
            if (transactionFeedResponse.isSuccessful) {
                _feedState.value = transactionFeedResponse.body()
            }
        }
    }

    // 2. Calculate the round-up

    // 3. Perform the transfer
    fun performTransfer() {
        viewModelScope.launch {
            // TODO: Trigger savings goal transfer
        }
    }

    // Run as soon as initialised
    init {
        initialiseAccountDetails()
        fetchWeeklyTransactions()
    }

}