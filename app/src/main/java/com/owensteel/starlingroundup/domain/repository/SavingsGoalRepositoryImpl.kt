package com.owensteel.starlingroundup.domain.repository

import com.owensteel.starlingroundup.model.CreateSavingsGoalRequest
import com.owensteel.starlingroundup.model.CreateSavingsGoalResponse
import com.owensteel.starlingroundup.model.Money
import com.owensteel.starlingroundup.model.TransferRequest
import com.owensteel.starlingroundup.model.TransferResponse
import com.owensteel.starlingroundup.network.StarlingService
import com.owensteel.starlingroundup.token.TokenManager
import retrofit2.Response
import javax.inject.Inject

class SavingsGoalRepositoryImpl @Inject constructor(
    private val tokenManager: TokenManager
) : SavingsGoalRepository {

    override suspend fun transferToGoal(
        savingsGoalUid: String,
        accountUid: String,
        transferUid: String,
        roundUpAmount: Money
    ): TransferResponse? {
        return try {
            val transferRequest = TransferRequest(
                amount = roundUpAmount
            )
            val transferResponse: Response<TransferResponse> =
                StarlingService.transferToSavingsGoal(
                    tokenManager,
                    accountUid,
                    savingsGoalUid,
                    transferUid,
                    transferRequest,
                )
            if (transferResponse.isSuccessful) {
                transferResponse.body()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun createGoal(
        accountUid: String,
        savingsGoalName: String,
        savingsGoalTarget: Money
    ): String? {
        return try {
            val createSavingsGoalRequest = CreateSavingsGoalRequest(
                name = savingsGoalName,
                currency = savingsGoalTarget.currency,
                target = savingsGoalTarget,
                base64EncodedPhoto = null
            )
            val createSavingsGoalResponse: Response<CreateSavingsGoalResponse> =
                StarlingService.createSavingsGoal(
                    tokenManager,
                    accountUid,
                    createSavingsGoalRequest,
                )
            if (createSavingsGoalResponse.isSuccessful) {
                createSavingsGoalResponse.body()?.savingsGoalUid
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

}