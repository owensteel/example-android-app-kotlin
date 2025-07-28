package com.owensteel.starlingroundup.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.owensteel.starlingroundup.domain.model.AccountDetails
import com.owensteel.starlingroundup.model.Transaction
import com.owensteel.starlingroundup.model.uistates.RoundUpUiError
import com.owensteel.starlingroundup.model.uistates.RoundUpUiState
import com.owensteel.starlingroundup.usecase.FetchTransactionsUseCase
import com.owensteel.starlingroundup.usecase.InitAccountDetailsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Currency
import javax.inject.Inject

@HiltViewModel
class RoundUpAndSaveViewModel @Inject constructor(
    private val initAccountDetails: InitAccountDetailsUseCase,
    private val fetchTransactions: FetchTransactionsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoundUpUiState())
    val uiState: StateFlow<RoundUpUiState> = _uiState

    private var accountUid: String? = null
    private var categoryUid: String? = null
    private var currency: Currency? = null
    private var cachedTransactions: List<Transaction> = emptyList()

    init {
        viewModelScope.launch {
            initialise()
        }
    }

    private suspend fun initialise() {
        _uiState.update {
            it.copy(isLoading = true)
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
                    isLoading = false
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = RoundUpUiError.Initialisation
                )
            }
        }
    }

    fun refreshTransactionsAndRoundUp() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true
                )
            }

            val uid = accountUid ?: return@launch
            val catUid = categoryUid ?: return@launch

            val fetchedTransactions = fetchTransactions(uid, catUid)
            cachedTransactions = fetchedTransactions

            val cutoff = transferToSavingsGoal.getLatestCutoffTimestamp()
            val roundUp = calculateRoundUp(fetchedTransactions, cutoff)

            _uiState.update {
                it.copy(
                    transactions = fetchedTransactions,
                    roundUpTotal = roundUp,
                    cutoffTimestamp = cutoff,
                    isLoading = false
                )
            }
        }
    }

}