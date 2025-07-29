package com.owensteel.starlingroundup.usecase

import com.owensteel.starlingroundup.domain.repository.SavingsGoalRepository
import com.owensteel.starlingroundup.model.SavingsGoal
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetSavingsGoalsUseCase @Inject constructor(
    private val savingsGoalRepository: SavingsGoalRepository,
    private val dispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(accountUid: String): List<SavingsGoal> = withContext(dispatcher) {
        val savingsGoalList = savingsGoalRepository.getAccountSavingsGoals(accountUid)
            ?: return@withContext emptyList<SavingsGoal>()

        return@withContext savingsGoalList
    }

}