package com.owensteel.starlingroundup.ui

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.owensteel.starlingroundup.R
import com.owensteel.starlingroundup.model.Money
import com.owensteel.starlingroundup.model.SavingsGoal
import com.owensteel.starlingroundup.model.Transaction
import com.owensteel.starlingroundup.ui.components.AppButton
import com.owensteel.starlingroundup.ui.components.AppSecondaryButton
import com.owensteel.starlingroundup.ui.components.CurrencyTextField
import com.owensteel.starlingroundup.ui.theme.AccessibleGrey
import com.owensteel.starlingroundup.ui.theme.TransactionInBgGreen
import com.owensteel.starlingroundup.util.MoneyUtils.roundUp
import com.owensteel.starlingroundup.util.SharedConstants.Transactions.TRANSACTION_DIRECTION_OUT
import com.owensteel.starlingroundup.util.SharedConstants.Transactions.TRANSACTION_SOURCE_INTERNAL_TRANSFER
import com.owensteel.starlingroundup.viewmodel.FeedUiState
import com.owensteel.starlingroundup.viewmodel.RoundUpAndSaveViewModel
import com.owensteel.starlingroundup.viewmodel.SavingsGoalsModalUiState
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoundUpAndSaveScreen(
    viewModel: RoundUpAndSaveViewModel = hiltViewModel()
) {
    val hasInitialisedDataState by viewModel.hasInitialisedDataState.collectAsState()
    val amount by viewModel.roundUpAmountState.collectAsState()
    val accountHolderName by viewModel.accountHolderNameState.collectAsState()
    val feedState by viewModel.feedState.collectAsState()
    val savingsGoalsModalUiState by viewModel.savingsGoalsModalUiState.collectAsState()

    // Transfer to Savings Goal modal
    val showTransferToSavingsSheet = remember { mutableStateOf(false) }

    // Pull to refresh states
    val isRefreshing = remember { mutableStateOf(false) }
    val pullToRefreshState = rememberPullToRefreshState()
    val coroutineScope = rememberCoroutineScope()
    val onRefresh: () -> Unit = {
        isRefreshing.value = true
        coroutineScope.launch {
            viewModel.refreshRoundUpTotalAndTransactionsFeed()
            isRefreshing.value = false
        }
    }
    val scaleFraction = {
        if (isRefreshing.value) 1f
        else LinearOutSlowInEasing.transform(pullToRefreshState.distanceFraction).coerceIn(0f, 1f)
    }

    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        // Main body
        Column(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxSize()
                .pullToRefresh(
                    isRefreshing = isRefreshing.value,
                    state = pullToRefreshState,
                    onRefresh = onRefresh
                ),
            // Vertically arranging from top prevents elements
            // jumping about when one lower down changes size
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (showTransferToSavingsSheet.value) {
                TransferSavingsModalSheet(
                    showTransferToSavingsSheet,
                    savingsGoalsModalUiState,
                    amount,
                    viewModel
                )
            }

            if (hasInitialisedDataState.value) {
                MainFeature(
                    viewModel = viewModel,
                    amount = amount,
                    accountHolderName = accountHolderName,
                    showTransferToSavingsSheet = showTransferToSavingsSheet
                )
                HorizontalDivider(
                    modifier = Modifier
                        .padding(0.dp, 15.dp)
                )
                TransactionsFeedFeature(
                    feedState = feedState
                )
            } else {
                // Placeholder container
                Column(
                    modifier = Modifier
                        .padding(0.dp)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (hasInitialisedDataState.hasError) {
                        // Error message
                        Text(
                            stringResource(
                                R.string.error_could_not_fetch_data,
                                hasInitialisedDataState.errorCode ?: ""
                            )
                        )
                    } else {
                        // Loading spinner
                        CircularProgressIndicator()
                    }
                }
            }
        }

        // Pull to refresh indicator
        // Must be at end of Composable for it to overlay
        // all elements
        Box(
            Modifier
                .graphicsLayer {
                    scaleX = scaleFraction()
                    scaleY = scaleFraction()
                }
        ) {
            PullToRefreshDefaults.Indicator(
                state = pullToRefreshState,
                isRefreshing = isRefreshing.value,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@Composable
fun MainFeature(
    viewModel: RoundUpAndSaveViewModel,
    amount: String,
    accountHolderName: String,
    showTransferToSavingsSheet: MutableState<Boolean>
) {
    Column(
        modifier = Modifier
            .padding(0.dp)
            .height(300.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.main_feature_greeting, accountHolderName),
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = stringResource(id = R.string.main_feature_intro, accountHolderName),
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = amount,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        AppButton(
            onClick = {
                // Show modal first
                showTransferToSavingsSheet.value = true
                // Request fetching of savings goal list
                viewModel.getAccountSavingsGoals()
            },
            text = stringResource(id = R.string.main_feature_button_round_up_and_save)
        )
    }
}

@Composable
fun TransactionsFeedFeature(
    feedState: FeedUiState
) {
    Column(
        modifier = Modifier
            .padding(0.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = stringResource(R.string.transactions_this_week_title),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                // Only pad bottom, as the top is
                // padded by the divider
                .padding(0.dp, 0.dp, 0.dp, 15.dp)
                .fillMaxWidth(),
        )
        when {
            feedState.isLoading -> CircularProgressIndicator(
                modifier = Modifier
                    .padding(15.dp)
            )

            feedState.hasError -> Text(stringResource(R.string.transactions_list_load_error))
            else -> feedState.value?.let {
                TransactionsFeedLazyColumn(
                    it,
                    feedState.latestRoundUpCutoffTimestamp
                )
            }
        }
    }
}

@OptIn(
    ExperimentalFoundationApi::class
) // allows use of stickyHeader
@Composable
fun TransactionsFeedLazyColumn(
    transactionsList: List<Transaction>,
    latestRoundUpCutoffTimestamp: String
) {
    // Transactions scroller
    val latestRoundUpCutoffTimestampAsInstant = Instant.parse(latestRoundUpCutoffTimestamp)
    LazyColumn {
        // Transaction list headers
        stickyHeader {
            TransactionHeaderRow()
        }
        // Render the list of transactions
        items(transactionsList) { transaction ->
            TransactionRow(
                transaction,
                (Instant.parse(transaction.transactionTime) > latestRoundUpCutoffTimestampAsInstant)
            )
        }
    }
}

val transactionsListRowColumnCommonPadding = 8.dp

@Composable
fun TransactionHeaderRow() {
    Row(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(0.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            stringResource(R.string.transactions_list_header_amount),
            modifier = Modifier
                .weight(2f)
                .wrapContentHeight()
                .padding(transactionsListRowColumnCommonPadding),
            textAlign = TextAlign.Start,
            fontWeight = FontWeight.Bold
        )
        Text(
            stringResource(R.string.transactions_list_header_roundup),
            modifier = Modifier
                .weight(1f)
                .wrapContentHeight()
                .padding(transactionsListRowColumnCommonPadding),
            textAlign = TextAlign.End,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun TransactionRow(
    transaction: Transaction,
    hasAlreadyBeenRoundedUp: Boolean
) {
    val transactionAmount: Money = transaction.amount

    Row(
        modifier = Modifier
            .padding(0.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Amount symbol

        // Make amount negative or positive depending
        // on the transaction direction, for UX purposes
        val inOrOutSymbol =
            if (transaction.direction == TRANSACTION_DIRECTION_OUT)
                stringResource(R.string.transaction_outgoing_symbol)
            else stringResource(R.string.transaction_ingoing_symbol)
        // Symbol has its own column so numbers are aligned
        Text(
            inOrOutSymbol,
            modifier = Modifier
                .width(30.dp)
                .wrapContentHeight()
                .padding(transactionsListRowColumnCommonPadding),
            textAlign = TextAlign.Center,
            style = TextStyle(
                fontSize = 24.sp
            )
        )

        // Amount

        // Change highlight colour depending on transaction
        // direction
        val amountBgColour =
            if (transaction.direction == TRANSACTION_DIRECTION_OUT) Color.Transparent else TransactionInBgGreen
        Text(
            Money(
                transactionAmount.currency,
                transactionAmount.minorUnits
            ).toString(),
            modifier = Modifier
                .weight(2f)
                .wrapContentHeight()
                .padding(transactionsListRowColumnCommonPadding),
            textAlign = TextAlign.Start,
            style = TextStyle(
                background = amountBgColour,
                fontSize = 24.sp
            )
        )

        // Potential round-up sum (only if transaction is not
        // an internal transfer, and is spending)
        if (transaction.direction == TRANSACTION_DIRECTION_OUT) {
            val isInternalTransfer = transaction.source == TRANSACTION_SOURCE_INTERNAL_TRANSFER
            Text(
                // Explain to user this transaction is internal, not a spend
                if (isInternalTransfer)
                    stringResource(R.string.transactions_list_label_not_counted)
                // Display potential round-up sum
                else
                    Money(
                        transactionAmount.currency,
                        roundUp(transactionAmount.minorUnits)
                    ).toString(),
                modifier = Modifier
                    .alpha(0.5f) // accessibility-friendly form of grey
                    .weight(1f)
                    .wrapContentHeight()
                    .padding(transactionsListRowColumnCommonPadding),
                textAlign = TextAlign.End,
                fontStyle = FontStyle.Italic,
                // Strikethrough to indicate to user that this
                // transaction has already been rounded-up
                textDecoration = if (!isInternalTransfer && !hasAlreadyBeenRoundedUp)
                    TextDecoration.LineThrough else null
            )
        }
    }

}

/*

    Transfer savings modal sheet

 */

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TransferSavingsModalSheet(
    showTransferToSavingsSheet: MutableState<Boolean>,
    savingsGoalsModalUiState: SavingsGoalsModalUiState,
    amount: String,
    viewModel: RoundUpAndSaveViewModel
) {
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

    if (showCreateAndTransferToNewSavingsGoalDialog.value) {
        CreateAndTransferToNewSavingsGoalDialog(
            onDismiss = {
                showCreateAndTransferToNewSavingsGoalDialog.value = false
            },
            onConfirm = { goalName: String, goalTarget: Long ->
                showCreateAndTransferToNewSavingsGoalDialog.value = false
                // Request create and transfer
                viewModel.createAndTransferToNewSavingsGoal(
                    goalName, goalTarget, showTransferToSavingsSheet
                )
            }
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

        // Show target and progress, if any
        if (savingsGoal.target != null) {
            Text(
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

}

/*

    Create Savings Goal dialog

 */

@Composable
fun CreateAndTransferToNewSavingsGoalDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Long) -> Unit
) {
    val goalNameValue = remember { mutableStateOf("") }
    val goalTargetValue = remember { mutableStateOf("0.00") }

    val dialogErrorMessageStringResourceId = remember { mutableStateOf<Int?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            AppButton(
                onClick = {
                    val goalTargetNumberDouble: Double? = goalTargetValue.value.toDoubleOrNull()

                    if (goalTargetNumberDouble == null) {
                        // Invalid goal target
                        dialogErrorMessageStringResourceId.value =
                            R.string.create_savings_goal_dialog_error_invalid_target
                    } else {
                        if (goalNameValue.value.isNotBlank()) {
                            val goalTargetNumberLong: Long = (goalTargetNumberDouble * 100).toLong()
                            onConfirm(goalNameValue.value, goalTargetNumberLong)
                        } else {
                            // Invalid goal name
                            dialogErrorMessageStringResourceId.value =
                                R.string.create_savings_goal_dialog_error_invalid_name
                        }
                    }
                },
                text = stringResource(R.string.create_savings_goal_dialog_goal_finish_button)
            )
        },
        dismissButton = {
            AppSecondaryButton(
                onClick = onDismiss,
                text = stringResource(R.string.dialog_button_cancel),
            )
        },
        title = {
            Text(stringResource(R.string.create_savings_goal_dialog_title))
        },
        text = {
            Column {
                OutlinedTextField(
                    value = goalNameValue.value,
                    onValueChange = { goalNameValue.value = it },
                    label = { Text(stringResource(R.string.create_savings_goal_dialog_goal_name_input_label)) },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                CurrencyTextField(
                    valueState = goalTargetValue,
                    label = stringResource(R.string.create_savings_goal_dialog_goal_target_input_label)
                )
                // Inline error message for invalid input
                if (dialogErrorMessageStringResourceId.value != null) {
                    Text(
                        text = stringResource(dialogErrorMessageStringResourceId.value!!),
                        color = Color.Red
                    )
                }
            }
        }
    )
}