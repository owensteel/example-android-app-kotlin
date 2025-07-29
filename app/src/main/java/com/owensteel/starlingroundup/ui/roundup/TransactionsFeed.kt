package com.owensteel.starlingroundup.ui.roundup

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.owensteel.starlingroundup.R
import com.owensteel.starlingroundup.model.Money
import com.owensteel.starlingroundup.model.Transaction
import com.owensteel.starlingroundup.ui.theme.TransactionInBgGreen
import com.owensteel.starlingroundup.util.DateTimeUtils
import com.owensteel.starlingroundup.util.MoneyUtils.roundUp
import com.owensteel.starlingroundup.util.SharedConstants.Transactions.TRANSACTION_DIRECTION_OUT
import com.owensteel.starlingroundup.util.SharedConstants.Transactions.TRANSACTION_SOURCE_INTERNAL_TRANSFER
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
                    TransactionRow(
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

            // Row allows us to display an icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .weight(1f)
            ) {
                // Display round-up sum
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
                        .wrapContentHeight()
                        .padding(transactionsListRowColumnCommonPadding),
                    textAlign = TextAlign.End,
                    fontStyle = FontStyle.Italic,
                    // Strikethrough to indicate to user that this
                    // transaction has already been rounded-up
                    textDecoration = if (!isInternalTransfer && hasAlreadyBeenRoundedUp)
                        TextDecoration.LineThrough else null
                )
                // Icon that follows the round-up sum
                if (!isInternalTransfer && hasAlreadyBeenRoundedUp) {
                    Spacer(modifier = Modifier.width(8.dp))
                    // Tick icon to show user this round-up has
                    // been transferred
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = Icons.Default.Check.name,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }

}