package com.owensteel.starlingroundup.domain.repository

import com.owensteel.starlingroundup.model.Money
import com.owensteel.starlingroundup.model.SavingsGoal
import com.owensteel.starlingroundup.model.TransferResponse

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

    suspend fun getAccountSavingsGoals(
        accountUid: String
    ): List<SavingsGoal>?

}