package com.owensteel.starlingroundup.ui.roundup

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
import com.owensteel.starlingroundup.model.uistates.RoundUpUiError
import com.owensteel.starlingroundup.model.uistates.RoundUpUiState
import com.owensteel.starlingroundup.ui.components.AppButton
import com.owensteel.starlingroundup.ui.dialogs.NewSavingsGoalDialog
import com.owensteel.starlingroundup.ui.theme.AccessibleGrey
import com.owensteel.starlingroundup.viewmodel.RoundUpAndSaveViewModel

/*

    Modal sheet that takes a round-up amount and allows
    user to choose what Savings Goal to transfer to (and
    create one to transfer to if necessary)

 */

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TransferToSavingsGoalModal(
    uiState: RoundUpUiState,
    amount: String,
    viewModel: RoundUpAndSaveViewModel
) {
    val showCreateAndTransferToNewSavingsGoalDialog = remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = {
            viewModel.showModal(false)
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
            if (uiState.error is RoundUpUiError.Transfer) {
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
                uiState.isLoadingSavingsGoals -> CircularProgressIndicator(
                    modifier = Modifier
                        .padding(0.dp, 15.dp)
                )

                (uiState.error is RoundUpUiError.Network) -> Text(
                    stringResource(R.string.transfer_to_savings_modal_goals_load_error),
                    modifier = Modifier
                        .padding(0.dp, 15.dp)
                )

                else -> SavingsGoalsLazyColumn(
                    uiState.savingsGoals,
                    viewModel
                )
            }
        }
    }

    if (showCreateAndTransferToNewSavingsGoalDialog.value && uiState.accountCurrency != null) {
        NewSavingsGoalDialog(
            onDismiss = {
                showCreateAndTransferToNewSavingsGoalDialog.value = false
            },
            onConfirm = { goalName: String, goalTarget: Long ->
                showCreateAndTransferToNewSavingsGoalDialog.value = false
                // Request create and transfer
                viewModel.createGoalAndTransfer(
                    goalName, goalTarget
                )
            },
            accountCurrency = uiState.accountCurrency
        )
    }
}