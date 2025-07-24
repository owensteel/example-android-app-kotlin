package com.owensteel.starlingroundup.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
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
    application: Application,
    private val tokenManager: TokenManager
) : AndroidViewModel(application) {

    // Use StateFlow to maintain an observable mutable state for our
    // value that may change between appearances

    private val _roundUpAmountState = MutableStateFlow("£0.00")
    val roundUpAmountState: StateFlow<String> = _roundUpAmountState

    private val _feedState = MutableStateFlow<TransactionFeedResponse?>(null)
    val feedState: StateFlow<TransactionFeedResponse?> = _feedState

    private val _accountNameState = MutableStateFlow("")
    val accountNameState: StateFlow<String> = _accountNameState

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

                    // Display name
                    _accountNameState.value = account.name
                }
            }
        }
    }

    // 1. Fetch this week's transactions
    private fun fetchWeeklyTransactions(context: Context) {
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
        // Prevent context leak by getting and
        // passing it just-in-time
        val context = application.applicationContext

        // Call fetch methods sequentially
        viewModelScope.launch {
            initialiseAccountDetails(context)
            fetchWeeklyTransactions(context)
        }
    }

}