package com.owensteel.starlingroundup.model.uistates

import com.owensteel.starlingroundup.model.Money
import com.owensteel.starlingroundup.model.SavingsGoal
import com.owensteel.starlingroundup.model.Transaction
import java.util.Currency

data class RoundUpUiState(
    val hasInitialised: Boolean = false,
    val roundUpTotal: Money = Money("GBP", 0),
    val transactions: List<Transaction> = emptyList(),
    val accountHolderName: String = "",
    val accountCurrency: Currency? = null,
    val savingsGoals: List<SavingsGoal> = emptyList(),
    val cutoffTimestamp: String = "",
    val isLoadingSavingsGoals: Boolean = false,
    val isLoadingTransactionsFeed: Boolean = false,
    val showModal: Boolean = false,
    val error: RoundUpUiError? = null
)