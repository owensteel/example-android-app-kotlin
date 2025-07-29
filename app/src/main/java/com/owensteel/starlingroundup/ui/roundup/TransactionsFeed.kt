package com.owensteel.starlingroundup.ui.roundup

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.owensteel.starlingroundup.R
import com.owensteel.starlingroundup.model.Transaction
import com.owensteel.starlingroundup.util.DateTimeUtils
import java.time.Instant
import java.time.ZoneId

/*

    Transactions feed

    Takes a list of transactions and displays
    the amount, direction, and round-up status,
    grouped by weekday

 */

@OptIn(
    ExperimentalFoundationApi::class
) // allows use of stickyHeader
@Composable
fun TransactionsFeed(
    transactionsList: List<Transaction>,
    latestRoundUpCutoffTimestamp: String
) {
    if (transactionsList.isEmpty()) {
        Text(
            text = stringResource(R.string.transactions_list_empty),
            modifier = Modifier
                .padding(15.dp)
        )
    } else {
        // We want to tell the user which transactions have been rounded-up already
        val latestRoundUpCutoffTimestampAsInstant = Instant.parse(latestRoundUpCutoffTimestamp)

        // Group transactions by weekday
        val transactionsGrouped = transactionsList.groupBy { transaction ->
            val transactionTimeAsInstantZoned =
                Instant.parse(transaction.transactionTime).atZone(ZoneId.systemDefault())
            val transactionDayOfWeek = transactionTimeAsInstantZoned.dayOfWeek
            val dayWithSuffix =
                DateTimeUtils.getDayWithSuffix(transactionTimeAsInstantZoned.dayOfMonth)

            // Lambda implicit return
            "$transactionDayOfWeek $dayWithSuffix"
        }

        // Transactions scroller
        LazyColumn {
            // Transaction list headers
            stickyHeader {
                TransactionHeaderRow()
            }

            // Render the list of transactions in groups
            transactionsGrouped.forEach { (headerLabel, transactions) ->
                // Weekday header
                item {
                    Row {
                        // Displays weekday and day of month
                        // e.g. "Monday 23rd"
                        Text(
                            headerLabel.uppercase(),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(15.dp),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                // Transactions for this weekday
                items(transactions) { transaction ->
                    val transactionTimeAsInstant = Instant.parse(transaction.transactionTime)
                    // Display transaction row
                    TransactionFeedRow(
                        transaction,
                        // If the transaction is before the latest round-up
                        // cutoff timestamp, assume it has already been part
                        // of a round-up total
                        (transactionTimeAsInstant <= latestRoundUpCutoffTimestampAsInstant)
                    )
                }
            }
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