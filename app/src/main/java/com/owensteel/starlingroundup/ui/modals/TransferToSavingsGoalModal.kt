package com.owensteel.starlingroundup.ui.modals

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.owensteel.starlingroundup.R
import com.owensteel.starlingroundup.model.Money
import com.owensteel.starlingroundup.model.SavingsGoal
import com.owensteel.starlingroundup.ui.components.AppButton
import com.owensteel.starlingroundup.ui.components.transactionsListRowColumnCommonPadding
import com.owensteel.starlingroundup.ui.dialogs.CreateNewSavingsGoalDialog
import com.owensteel.starlingroundup.ui.theme.AccessibleGrey
import com.owensteel.starlingroundup.viewmodel.RoundUpAndSaveViewModel
import com.owensteel.starlingroundup.viewmodel.SavingsGoalsModalUiState

/*

    Modal sheet that takes a round-up amount and allows
    user to choose what Savings Goal to transfer to (and
    create one to transfer to if necessary)

 */

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TransferToSavingsGoalModal(
    showTransferToSavingsSheet: MutableState<Boolean>,
    savingsGoalsModalUiState: SavingsGoalsModalUiState,
    amount: String,
    viewModel: RoundUpAndSaveViewModel
) {
    val accountCurrency by viewModel.accountCurrencyState.collectAsState()
    val showCreateAndTransferToNewSavingsGoalDialog = remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = {
            showTransferToSavingsSheet.value = false
        }
    ) {
        Column(
            modifier = Modifier
                .padding(15.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = stringResource(R.string.transfer_to_savings_modal_header, amount),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(0.dp, 15.dp)
                    .fillMaxWidth(),
            )
            Text(
                text = stringResource(R.string.transfer_to_savings_modal_intro),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .padding(0.dp, 15.dp)
                    .fillMaxWidth(),
            )

            // Recoverable error gets inline error message
            if (savingsGoalsModalUiState.hasTransferError) {
                Text(
                    stringResource(R.string.transfer_to_savings_modal_goals_transfer_error),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .padding(0.dp, 15.dp),
                    color = Color.Red
                )
            }

            // Create Savings Goal
            AppButton(
                onClick = {
                    showCreateAndTransferToNewSavingsGoalDialog.value = true
                },
                text = stringResource(R.string.transfer_to_savings_modal_create_and_transfer)
            )

            // Savings Goals list
            when {
                savingsGoalsModalUiState.isLoading -> CircularProgressIndicator(
                    modifier = Modifier
                        .padding(0.dp, 15.dp)
                )

                savingsGoalsModalUiState.hasLoadingError -> Text(
                    stringResource(R.string.transfer_to_savings_modal_goals_load_error),
                    modifier = Modifier
                        .padding(0.dp, 15.dp)
                )

                else -> SavingsGoalsLazyColumn(
                    showTransferToSavingsSheet,
                    savingsGoalsModalUiState.value,
                    viewModel
                )
            }
        }
    }

    if (showCreateAndTransferToNewSavingsGoalDialog.value && accountCurrency != null) {
        CreateNewSavingsGoalDialog(
            onDismiss = {
                showCreateAndTransferToNewSavingsGoalDialog.value = false
            },
            onConfirm = { goalName: String, goalTarget: Long ->
                showCreateAndTransferToNewSavingsGoalDialog.value = false
                // Request create and transfer
                viewModel.createAndTransferToNewSavingsGoal(
                    goalName, goalTarget, showTransferToSavingsSheet
                )
            },
            accountCurrency = accountCurrency!!
        )
    }
}

@Composable
fun SavingsGoalsLazyColumn(
    showTransferToSavingsSheet: MutableState<Boolean>,
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
                    showTransferToSavingsSheet,
                    savingsGoal,
                    viewModel
                )
            }
        }
    }
}

@Composable
fun SavingsGoalRow(
    showTransferToSavingsSheet: MutableState<Boolean>,
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
                viewModel.performTransferToSavingsGoal(
                    savingsGoal.savingsGoalUid,
                    showTransferToSavingsSheet
                )
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