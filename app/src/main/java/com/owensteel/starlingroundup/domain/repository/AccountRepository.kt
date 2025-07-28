package com.owensteel.starlingroundup.domain.repository

import com.owensteel.starlingroundup.model.Account
import com.owensteel.starlingroundup.model.AccountHolderIndividualResponse
import com.owensteel.starlingroundup.token.TokenManager

interface AccountRepository {
    suspend fun getPrimaryAccount(tokenManager: TokenManager): Account?
    suspend fun getAccountHolder(tokenManager: TokenManager): AccountHolderIndividualResponse?
}