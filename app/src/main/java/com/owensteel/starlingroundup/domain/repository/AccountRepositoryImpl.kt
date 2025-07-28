package com.owensteel.starlingroundup.domain.repository

import com.owensteel.starlingroundup.model.Account
import com.owensteel.starlingroundup.model.AccountHolderIndividualResponse
import com.owensteel.starlingroundup.model.AccountResponse
import com.owensteel.starlingroundup.network.StarlingService
import com.owensteel.starlingroundup.token.TokenManager
import retrofit2.Response
import javax.inject.Inject

class AccountRepositoryImpl @Inject constructor() : AccountRepository {

    override suspend fun getPrimaryAccount(tokenManager: TokenManager): Account? {
        return try {
            val accountResponse: Response<AccountResponse> =
                StarlingService.getAccountDetails(tokenManager)
            if (accountResponse.isSuccessful) {
                accountResponse.body()?.accounts?.firstOrNull()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getAccountHolder(tokenManager: TokenManager): AccountHolderIndividualResponse? {
        return try {
            val accountHolderIndividualResponse: Response<AccountHolderIndividualResponse> =
                StarlingService.getAccountHolderIndividual(tokenManager)
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