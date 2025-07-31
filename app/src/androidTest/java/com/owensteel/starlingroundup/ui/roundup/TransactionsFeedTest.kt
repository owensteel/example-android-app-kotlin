package com.owensteel.starlingroundup.ui.roundup

import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.test.platform.app.InstrumentationRegistry
import com.owensteel.starlingroundup.fakes.api.FakeStarlingServiceResponses.FakeAccount
import com.owensteel.starlingroundup.model.Money
import com.owensteel.starlingroundup.model.Transaction
import org.junit.Rule
import org.junit.Test
import java.time.Instant
import java.util.UUID
import com.owensteel.starlingroundup.R

@OptIn(ExperimentalFoundationApi::class)
class TransactionsFeedTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val now = Instant.now()
    private val justBeforeCutoff = now.minusSeconds(60).toString()
    private val justAfterCutoff = now.plusSeconds(60).toString()

    private fun createFakeTransaction(
        amountMinorUnits: Long = 1000L,
        timestamp: String = now.toString()
    ): Transaction {
        return Transaction(
            feedItemUid = UUID.randomUUID().toString(),
            amount = Money(
                FakeAccount.currency,
                amountMinorUnits
            ),
            direction = "OUT",
            transactionTime = timestamp,
            source = "MASTER_CARD",
            spendingCategory = UUID.randomUUID().toString()
        )
    }

    @Test
    fun showsEmptyMessage_whenListIsEmpty() {
        composeTestRule.setContent {
            TransactionsFeed(emptyList(), latestRoundUpCutoffTimestamp = now.toString())
        }

        composeTestRule
            .onNodeWithText(getString(R.string.transactions_list_empty))
            .assertIsDisplayed()
    }

    @Test
    fun showsHeaderAndTransactionRows_whenListHasItems() {
        val transaction1 = createFakeTransaction(timestamp = justBeforeCutoff)
        val transaction2 = createFakeTransaction(timestamp = justAfterCutoff)

        composeTestRule.setContent {
            TransactionsFeed(
                listOf(transaction1, transaction2),
                latestRoundUpCutoffTimestamp = now.toString()
            )
        }

        // Check that headers are displayed
        composeTestRule
            .onNodeWithText(getString(R.string.transactions_list_header_amount), substring = true)
            .assertIsDisplayed()

        // Check both transactions are rendered
        composeTestRule
            .onAllNodesWithText(transaction1.amount.toString(), substring = true)
            .assertCountEquals(2)
    }
}

fun getString(@StringRes resId: Int): String {
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    return context.getString(resId)
}

