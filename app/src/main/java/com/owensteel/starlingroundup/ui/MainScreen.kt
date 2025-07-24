package com.owensteel.starlingroundup.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.owensteel.starlingroundup.R
import com.owensteel.starlingroundup.model.CurrencyAmount
import com.owensteel.starlingroundup.model.Money
import com.owensteel.starlingroundup.model.Transaction
import com.owensteel.starlingroundup.ui.components.AppButton
import com.owensteel.starlingroundup.ui.theme.Typography
import com.owensteel.starlingroundup.util.MoneyUtils.roundUp
import com.owensteel.starlingroundup.util.SharedConstants.Transactions.TRANSACTION_DIRECTION_OUT
import com.owensteel.starlingroundup.viewmodel.FeedUiState
import com.owensteel.starlingroundup.viewmodel.MainViewModel

@Composable
fun MainScreen(viewModel: MainViewModel) {
    val hasInitialisedDataState by viewModel.hasInitialisedDataState.collectAsState()
    val amount by viewModel.roundUpAmountState.collectAsState()
    val accountHolderName by viewModel.accountHolderNameState.collectAsState()
    val feedState by viewModel.feedState.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxSize(),
            // Vertically arranging from top prevents elements
            // jumping about when one lower down changes size
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (hasInitialisedDataState) {
                MainFeature(
                    viewModel = viewModel,
                    amount = amount,
                    accountHolderName = accountHolderName
                )
                HorizontalDivider(
                    modifier = Modifier
                        .padding(0.dp, 15.dp)
                )
                TransactionsFeedFeature(
                    feedState = feedState
                )
            } else {
                // Loading spinner
                Column(
                    modifier = Modifier
                        .padding(0.dp)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
fun MainFeature(viewModel: MainViewModel, amount: String, accountHolderName: String) {
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

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = amount,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        AppButton(
            onClick = { viewModel.performTransfer() },
            text = stringResource(id = R.string.main_feature_button_round_up_and_save)
        )
    }
}

@Composable
fun TransactionsFeedFeature(feedState: FeedUiState) {
    Column(
        modifier = Modifier
            .padding(0.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        when {
            feedState.isLoading -> CircularProgressIndicator()
            feedState.hasError -> Text("Couldn't load the Transactions Feed.")
            else -> feedState.value?.let { TransactionsFeedLazyColumn(it) }
        }
    }
}

@Composable
fun TransactionsFeedLazyColumn(transactionsList: List<Transaction>) {
    LazyColumn {
        // Transaction list headers
        item {
            TransactionHeaderRow()
        }
        // Render the list of transactions
        items(transactionsList) { transaction ->
            TransactionRow(transaction)
        }
    }
}

val transactionsListRowColumnCommonPadding = 8.dp

@Composable
fun TransactionHeaderRow() {
    Row(
        modifier = Modifier
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
            textAlign = TextAlign.Start,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun TransactionRow(transaction: Transaction) {
    val transactionAmount: CurrencyAmount = transaction.amount

    Row(
        modifier = Modifier
            .padding(0.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Amount

        // Make amount negative or positive depending
        // on the transaction direction, for UX purposes
        val inOrOutMultiplier = if (transaction.direction == TRANSACTION_DIRECTION_OUT) -1 else 1
        Text(
            Money(
                transactionAmount.currency,
                transactionAmount.minorUnits * inOrOutMultiplier
            ).toString(),
            modifier = Modifier
                .weight(2f)
                .wrapContentHeight()
                .padding(transactionsListRowColumnCommonPadding),
            textAlign = TextAlign.Start,
            style = Typography.headlineSmall
        )

        // Round-up (only if transaction is for spending)
        if (transaction.direction == TRANSACTION_DIRECTION_OUT) {
            Text(
                Money(
                    transactionAmount.currency,
                    roundUp(transactionAmount.minorUnits)
                ).toString(),
                modifier = Modifier
                    .weight(1f)
                    .wrapContentHeight()
                    .padding(transactionsListRowColumnCommonPadding),
                textAlign = TextAlign.End,
                fontStyle = FontStyle.Italic
            )
        }
    }

}