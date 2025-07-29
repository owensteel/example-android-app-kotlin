package com.owensteel.starlingroundup.domain.repository

import com.owensteel.starlingroundup.model.Account
import com.owensteel.starlingroundup.model.AccountHolderIndividualResponse

interface AccountRepository {
    suspend fun getPrimaryAccount(): Account?
    suspend fun getAccountHolder(): AccountHolderIndividualResponse?
}