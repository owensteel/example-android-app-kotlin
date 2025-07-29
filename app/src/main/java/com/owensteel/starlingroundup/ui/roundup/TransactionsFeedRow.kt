package com.owensteel.starlingroundup.ui.roundup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.owensteel.starlingroundup.R
import com.owensteel.starlingroundup.model.Money
import com.owensteel.starlingroundup.model.Transaction
import com.owensteel.starlingroundup.ui.theme.TransactionInBgGreen
import com.owensteel.starlingroundup.util.MoneyUtils.roundUp
import com.owensteel.starlingroundup.util.SharedConstants.Transactions.TRANSACTION_DIRECTION_OUT
import com.owensteel.starlingroundup.util.SharedConstants.Transactions.TRANSACTION_SOURCE_INTERNAL_TRANSFER

/*

    The row display a transaction in a Transactions Feed

 */

@Composable
fun TransactionFeedRow(
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