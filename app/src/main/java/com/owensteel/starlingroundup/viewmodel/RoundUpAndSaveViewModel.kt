package com.owensteel.starlingroundup.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.owensteel.starlingroundup.data.local.RoundUpCutoffTimestampStore
import com.owensteel.starlingroundup.domain.model.AccountDetails
import com.owensteel.starlingroundup.model.AccountHolderIndividualResponse
import com.owensteel.starlingroundup.model.AccountResponse
import com.owensteel.starlingroundup.model.CreateSavingsGoalRequest
import com.owensteel.starlingroundup.model.CreateSavingsGoalResponse
import com.owensteel.starlingroundup.model.GetSavingsGoalsResponse
import com.owensteel.starlingroundup.model.Money
import com.owensteel.starlingroundup.model.SavingsGoal
import com.owensteel.starlingroundup.model.Transaction
import com.owensteel.starlingroundup.model.TransactionFeedResponse
import com.owensteel.starlingroundup.model.TransferRequest
import com.owensteel.starlingroundup.model.TransferResponse
import com.owensteel.starlingroundup.model.uistates.RoundUpUiError
import com.owensteel.starlingroundup.model.uistates.RoundUpUiState
import com.owensteel.starlingroundup.network.StarlingService
import com.owensteel.starlingroundup.token.TokenManager
import com.owensteel.starlingroundup.usecase.InitAccountDetailsUseCase
import com.owensteel.starlingroundup.util.MoneyUtils.roundUp
import com.owensteel.starlingroundup.util.SharedConstants.Transactions.TRANSACTION_DIRECTION_OUT
import com.owensteel.starlingroundup.util.SharedConstants.Transactions.TRANSACTION_SOURCE_INTERNAL_TRANSFER
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.Response
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Currency
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class RoundUpAndSaveViewModel @Inject constructor(
    private val initAccountDetails: InitAccountDetailsUseCase
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

        _uiState.update { it.copy(isLoading = true) }

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

}