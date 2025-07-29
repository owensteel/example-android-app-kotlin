package com.owensteel.starlingroundup.usecase

import com.owensteel.starlingroundup.domain.repository.SavingsGoalRepository
import com.owensteel.starlingroundup.model.Money
import java.util.UUID
import javax.inject.Inject

class TransferToSavingsGoalUseCase @Inject constructor(
    private val savingsGoalRepository: SavingsGoalRepository
) {

    suspend fun transferToSavingsGoal(
        accountUid: String,
        savingsGoalUid: String,
        roundUpAmount: Money
    ): Result<Unit> {
        return try {
            val transferUid = UUID.randomUUID().toString()

            savingsGoalRepository.transferToGoal(
                savingsGoalUid,
                accountUid,
                transferUid,
                roundUpAmount
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createAndTransferToNewSavingsGoal(
        accountUid: String,
        savingsGoalName: String,
        savingsGoalTarget: Money,
        roundUpAmount: Money
    ): Result<Unit> {

        return try {
            val newSavingsGoalUid = savingsGoalRepository.createGoal(
                accountUid,
                savingsGoalName,
                savingsGoalTarget
            )

            // Return result of transfer
            if (newSavingsGoalUid != null) {
                transferToSavingsGoal(
                    accountUid,
                    newSavingsGoalUid,
                    roundUpAmount
                )
            } else {
                Result.failure(IllegalStateException("Could not create new Savings Goal"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}