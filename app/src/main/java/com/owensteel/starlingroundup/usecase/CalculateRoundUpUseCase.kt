package com.owensteel.starlingroundup.usecase

import com.owensteel.starlingroundup.model.Transaction
import com.owensteel.starlingroundup.util.MoneyUtils.roundUp
import com.owensteel.starlingroundup.util.SharedConstants.Transactions.TRANSACTION_DIRECTION_OUT
import com.owensteel.starlingroundup.util.SharedConstants.Transactions.TRANSACTION_SOURCE_INTERNAL_TRANSFER
import java.time.Instant
import javax.inject.Inject

class CalculateRoundUpUseCase @Inject constructor() {

    operator fun invoke(
        transactions: List<Transaction>,
        latestCutoffTimestamp: String
    ): Long {

        return calculateRoundUp(
            transactions,
            Instant.parse(latestCutoffTimestamp)
        )

    }

    private fun calculateRoundUp(
        transactions: List<Transaction>,
        latestCutoff: Instant
    ): Long {

        return transactions
            .filter {
                // Exclude transactions that have already been rounded-up
                Instant.parse(it.transactionTime) > latestCutoff
                        // Spending only
                        && it.direction == TRANSACTION_DIRECTION_OUT
                        // Exclude internal transfers
                        && it.source != TRANSACTION_SOURCE_INTERNAL_TRANSFER
            }
            .map { it.amount.minorUnits }.sumOf {
                roundUp(it)
            }

    }

}