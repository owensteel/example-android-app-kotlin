package com.owensteel.starlingroundup.domain.repository

import com.owensteel.starlingroundup.model.Money
import com.owensteel.starlingroundup.model.TransferResponse
import com.owensteel.starlingroundup.token.TokenManager

interface SavingsGoalRepository {

    suspend fun transferToGoal(
        savingsGoalUid: String,
        accountUid: String,
        transferUid: String,
        roundUpAmount: Money
    ): TransferResponse?

    suspend fun createGoal(
        accountUid: String,
        savingsGoalName: String,
        savingsGoalTarget: Money
    ): String?

}