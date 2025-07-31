package com.owensteel.starlingroundup.endtoend

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import com.owensteel.starlingroundup.MainActivity
import com.owensteel.starlingroundup.R
import com.owensteel.starlingroundup.fakes.api.FakeStarlingServiceResponses.FakeSavingsGoal
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class RoundUpFlowTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun userCanCompleteRoundUpAndTransferToExistingSavingsGoalFlow() {

        val context = InstrumentationRegistry.getInstrumentation().targetContext

        // Wait for transaction list header
        composeTestRule
            .onNodeWithText(context.getString(R.string.transactions_this_week_title))
            .assertIsDisplayed()

        // Tap Round Up button
        composeTestRule
            .onNodeWithText(context.getString(R.string.main_feature_button_round_up_and_save)) // adjust to match actual text used
            .assertIsDisplayed()
            .performClick()

        // Modal appears
        composeTestRule
            .onNodeWithText(context.getString(R.string.transfer_to_savings_modal_intro))
            .assertIsDisplayed()

        // Create & transfer to new Savings Goal modal
        composeTestRule
            .onNodeWithText(context.getString(R.string.transfer_to_savings_modal_intro))
            .assertIsDisplayed()

        // Click existing Goal
        composeTestRule
            .onNodeWithText(FakeSavingsGoal.name)
            .performClick()

        // Check for successful transfer dialog
        composeTestRule.waitUntil(timeoutMillis = 5000L) {
            composeTestRule.onAllNodesWithText(context.getString(R.string.roundup_transfer_complete_dialog_title))
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText(context.getString(R.string.roundup_transfer_complete_dialog_title))
            .assertExists()
    }

    @Test
    fun userCanCompleteRoundUpAndTransferToNewSavingsGoalFlow() {

        val newGoalName = "Holiday Fund"

        val context = InstrumentationRegistry.getInstrumentation().targetContext

        // Wait for transaction list header
        composeTestRule
            .onNodeWithText(context.getString(R.string.transactions_this_week_title))
            .assertIsDisplayed()

        // Tap Round Up button
        composeTestRule
            .onNodeWithText(context.getString(R.string.main_feature_button_round_up_and_save)) // adjust to match actual text used
            .assertIsDisplayed()
            .performClick()

        // Modal appears
        composeTestRule
            .onNodeWithText(context.getString(R.string.transfer_to_savings_modal_intro))
            .assertIsDisplayed()

        // Create & transfer to new Savings Goal modal
        composeTestRule
            .onNodeWithText(context.getString(R.string.transfer_to_savings_modal_intro))
            .assertIsDisplayed()

        // Click create and transfer
        composeTestRule
            .onNodeWithText(context.getString(R.string.transfer_to_savings_modal_create_and_transfer))
            .performClick()

        // Check for New Savings Goal dialog
        composeTestRule
            .onNodeWithText(context.getString(R.string.create_savings_goal_dialog_title))
            .assertIsDisplayed()

        // Input name for new Goal
        composeTestRule
            .onNodeWithText(context.getString(R.string.create_savings_goal_dialog_goal_name_input_label))
            .performTextInput(newGoalName)

        // Create Goal button
        composeTestRule
            .onNodeWithText(context.getString(R.string.create_savings_goal_dialog_goal_finish_button))
            .performClick()

        // Check for successful transfer dialog
        composeTestRule.waitUntil(timeoutMillis = 5000L) {
            composeTestRule.onAllNodesWithText(context.getString(R.string.roundup_transfer_complete_dialog_title))
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText(context.getString(R.string.roundup_transfer_complete_dialog_title))
            .assertExists()
    }

}