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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.owensteel.starlingroundup.R
import com.owensteel.starlingroundup.model.Transaction
import com.owensteel.starlingroundup.ui.components.AppButton
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
            verticalArrangement = Arrangement.Center,
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
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun MainFeature(viewModel: MainViewModel, amount: String, accountHolderName: String) {
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

@Composable
fun TransactionsFeedFeature(feedState: FeedUiState) {
    when {
        feedState.isLoading -> CircularProgressIndicator()
        feedState.hasError -> Text("Couldn't load the Transactions Feed.")
        else -> feedState.value?.let { TransactionsFeedLazyColumn(it) }
    }
}

@Composable
fun TransactionsFeedLazyColumn(transactionsList: List<Transaction>) {
    LazyColumn {
        items(transactionsList) { transaction ->
            Text(transaction.amount.toString())
        }
    }
}