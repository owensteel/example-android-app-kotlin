package com.owensteel.starlingroundup.model.uistates

import com.owensteel.starlingroundup.model.Money
import com.owensteel.starlingroundup.model.SavingsGoal
import com.owensteel.starlingroundup.model.Transaction

data class RoundUpUiState(
    val roundUpTotal: Money = Money("GBP", 0),
    val transactions: List<Transaction> = emptyList(),
    val accountHolderName: String = "",
    val savingsGoals: List<SavingsGoal> = emptyList(),
    val cutoffTimestamp: String = "",
    val isLoading: Boolean = false,
    val showModal: Boolean = false,
    val error: RoundUpUiError? = null
)