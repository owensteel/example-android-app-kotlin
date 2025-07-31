package com.owensteel.starlingroundup.ui.roundup

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.owensteel.starlingroundup.R
import com.owensteel.starlingroundup.model.Money
import com.owensteel.starlingroundup.model.SavingsGoal
import com.owensteel.starlingroundup.ui.theme.AccessibleGrey
import com.owensteel.starlingroundup.viewmodel.RoundUpAndSaveViewModel

@Composable
fun SavingsGoalRow(
    savingsGoal: SavingsGoal,
    viewModel: RoundUpAndSaveViewModel
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            // Outer padding
            .padding(0.dp, 15.dp)
            // Rounded border
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = 1.dp,
                color = AccessibleGrey,
                shape = RoundedCornerShape(12.dp)
            )
            // Inner padding
            .padding(15.dp)
            .clickable {
                // User selects this savings
                // goal to transfer to
                viewModel.transferToGoal(savingsGoal)
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Goal name
        Text(
            savingsGoal.name,
            modifier = Modifier
                .weight(2f)
                .wrapContentHeight()
                .padding(transactionsListRowColumnCommonPadding),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Start
        )

        // Show current total, and target and progress
        // if available
        Text(
            if (savingsGoal.target == null)
            // No target set, just show total
                Money(
                    savingsGoal.totalSaved.currency,
                    savingsGoal.totalSaved.minorUnits
                ).toString()
            else
            // Show total with target and current progress
                stringResource(
                    R.string.transfer_to_savings_modal_goal_amount_vs_target_and_percentage,
                    Money(
                        savingsGoal.totalSaved.currency,
                        savingsGoal.totalSaved.minorUnits
                    ).toString(),
                    Money(
                        savingsGoal.target.currency,
                        savingsGoal.target.minorUnits
                    ).toString(),
                    savingsGoal.savedPercentage.toString()
                ),
            modifier = Modifier
                .weight(2f)
                .wrapContentHeight()
                .padding(transactionsListRowColumnCommonPadding),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.End
        )
    }

}