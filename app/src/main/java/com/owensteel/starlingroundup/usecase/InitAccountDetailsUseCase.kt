package com.owensteel.starlingroundup.usecase

import com.owensteel.starlingroundup.domain.model.AccountDetails
import com.owensteel.starlingroundup.domain.repository.AccountRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.util.Currency
import javax.inject.Inject

class InitAccountDetailsUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
    private val dispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(): Result<AccountDetails> = withContext(dispatcher) {
        try {
            val account = accountRepository.getPrimaryAccount()
            val holder = accountRepository.getAccountHolder()

            if (account == null || holder == null) {
                return@withContext Result.failure(IllegalStateException("Missing account or holder"))
            }

            return@withContext Result.success(
                AccountDetails(
                    accountUid = account.accountUid,
                    categoryUid = account.defaultCategory,
                    currency = Currency.getInstance(account.currency),
                    accountHolderName = holder.firstName
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}