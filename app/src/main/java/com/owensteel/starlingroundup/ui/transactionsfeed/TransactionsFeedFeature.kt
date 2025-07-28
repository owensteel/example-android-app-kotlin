package com.owensteel.starlingroundup.ui.transactionsfeed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.owensteel.starlingroundup.R
import com.owensteel.starlingroundup.viewmodel.TransactionsFeedUiState

/*

    Handles the multiple states of the Transactions Feed
    Displays the feed, with a title, when ready

 */

@Composable
fun TransactionsFeedFeature(
    transactionsFeedUiState: TransactionsFeedUiState
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
            transactionsFeedUiState.isLoading -> CircularProgressIndicator(
                modifier = Modifier
                    .padding(15.dp)
            )

            transactionsFeedUiState.hasError -> Text(stringResource(R.string.transactions_list_load_error))

            else -> transactionsFeedUiState.value?.let {
                TransactionsFeed(
                    it,
                    transactionsFeedUiState.latestRoundUpCutoffTimestamp
                )
            }
        }
    }
}