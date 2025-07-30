package com.owensteel.starlingroundup.domain.repository

import com.owensteel.starlingroundup.model.Account
import com.owensteel.starlingroundup.model.AccountHolderIndividualResponse
import com.owensteel.starlingroundup.model.AccountResponse
import com.owensteel.starlingroundup.network.StarlingService
import retrofit2.Response
import javax.inject.Inject

class AccountRepositoryImpl @Inject constructor(
    private val starlingService: StarlingService
) : AccountRepository {

    override suspend fun getPrimaryAccount(): Account? {
        return try {
            val accountResponse: Response<AccountResponse> =
                starlingService.getAccountDetails()
            if (accountResponse.isSuccessful) {
                accountResponse.body()?.accounts?.firstOrNull()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getAccountHolder(): AccountHolderIndividualResponse? {
        return try {
            val accountHolderIndividualResponse: Response<AccountHolderIndividualResponse> =
                starlingService.getAccountHolderIndividual()
            if (accountHolderIndividualResponse.isSuccessful) {
                accountHolderIndividualResponse.body()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

}