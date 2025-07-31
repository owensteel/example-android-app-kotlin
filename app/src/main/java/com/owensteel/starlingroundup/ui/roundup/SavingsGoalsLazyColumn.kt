package com.owensteel.starlingroundup.ui.roundup

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.owensteel.starlingroundup.R
import com.owensteel.starlingroundup.model.SavingsGoal
import com.owensteel.starlingroundup.viewmodel.RoundUpAndSaveViewModel

@Composable
fun SavingsGoalsLazyColumn(
    savingsGoalsList: List<SavingsGoal>,
    viewModel: RoundUpAndSaveViewModel
) {
    when {
        savingsGoalsList.isEmpty() -> Text(
            stringResource(R.string.transfer_to_savings_modal_goals_empty),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .padding(0.dp, 15.dp)
                .fillMaxWidth(),
        )

        else -> LazyColumn(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Render the list of Savings Goals
            items(savingsGoalsList) { savingsGoal ->
                SavingsGoalRow(
                    savingsGoal,
                    viewModel
                )
            }
        }
    }
}