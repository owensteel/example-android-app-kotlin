package com.owensteel.starlingroundup.model.uistates

import com.owensteel.starlingroundup.model.Money
import com.owensteel.starlingroundup.model.SavingsGoal
import com.owensteel.starlingroundup.model.Transaction
import java.util.Currency
import java.util.Locale

// Must be var in case user changes locale during runtime
private var defaultCurrencyCode = Currency.getInstance(Locale.getDefault()).currencyCode

data class RoundUpUiState(
    val hasInitialised: Boolean = false,
    val roundUpTotal: Money = Money(defaultCurrencyCode, 0L),
    val transactions: List<Transaction> = emptyList(),
    val accountHolderName: String = "",
    val accountCurrency: Currency? = null,
    val savingsGoals: List<SavingsGoal> = emptyList(),
    val cutoffTimestamp: String = "",
    val isLoadingSavingsGoals: Boolean = false,
    val isLoadingTransactionsFeed: Boolean = false,
    val showModal: Boolean = false,
    val showRoundUpTransferCompleteDialog: Boolean = false,
    val roundUpTransferCompleteDialogRoundUpTotal: Money = Money(defaultCurrencyCode, 0L),
    val roundUpTransferCompleteDialogChosenSavingsGoalName: String = "",
    val error: RoundUpUiError? = null
)