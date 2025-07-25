package com.owensteel.starlingroundup.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.owensteel.starlingroundup.R
import com.owensteel.starlingroundup.model.CurrencyAmount
import com.owensteel.starlingroundup.model.Money
import com.owensteel.starlingroundup.model.Transaction
import com.owensteel.starlingroundup.ui.components.AppButton
import com.owensteel.starlingroundup.ui.theme.TransactionInBgGreen
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
            feedState.isLoading -> CircularProgressIndicator()
            feedState.hasError -> Text("Couldn't load the Transactions Feed.")
            else -> feedState.value?.let { TransactionsFeedLazyColumn(it) }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class) // allows use of stickyHeader
@Composable
fun TransactionsFeedLazyColumn(transactionsList: List<Transaction>) {
    LazyColumn {
        // Transaction list headers
        stickyHeader {
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

        // Round-up (only if transaction is for spending)
        if (transaction.direction == TRANSACTION_DIRECTION_OUT) {
            Text(
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
                fontStyle = FontStyle.Italic
            )
        }
    }

}