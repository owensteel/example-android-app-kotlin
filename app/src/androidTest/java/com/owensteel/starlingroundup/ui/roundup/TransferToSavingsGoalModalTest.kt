package com.owensteel.starlingroundup.ui.roundup

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.owensteel.starlingroundup.R
import com.owensteel.starlingroundup.fakes.api.FakeStarlingServiceResponses.FakeAccount
import com.owensteel.starlingroundup.fakes.api.FakeStarlingServiceResponses.FakeSavingsGoal
import com.owensteel.starlingroundup.model.Money
import com.owensteel.starlingroundup.model.SavingsGoal
import com.owensteel.starlingroundup.model.uistates.RoundUpUiState
import com.owensteel.starlingroundup.viewmodel.RoundUpAndSaveViewModel
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import java.util.Currency

@OptIn(ExperimentalMaterial3Api::class)
class TransferToSavingsGoalModalTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val fakeViewModel = mock<RoundUpAndSaveViewModel>()

    private val testUiState = RoundUpUiState(
        error = null,
        isLoadingSavingsGoals = false,
        savingsGoals = listOf(FakeSavingsGoal),
        accountCurrency = Currency.getInstance(FakeAccount.currency)
    )

    private val fakeTransferAmount = Money(
        FakeAccount.currency,
        1000L
    )

    @Test
    fun showsSavingsGoalName() {
        composeTestRule.setContent {
            TransferToSavingsGoalModal(
                uiState = testUiState,
                amount = fakeTransferAmount.toString(),
                viewModel = fakeViewModel
            )
        }

        val context = InstrumentationRegistry.getInstrumentation().targetContext

        // Assert the title
        composeTestRule
            .onNodeWithText(
                context.getString(
                    R.string.transfer_to_savings_modal_header,
                    fakeTransferAmount.toString()
                )
            )
            .assertIsDisplayed()

        // Assert the goal name
        composeTestRule
            .onNodeWithText(FakeSavingsGoal.name)
            .assertIsDisplayed()
    }

    @Test
    fun clickingCreateGoalOpensDialog() {
        composeTestRule.setContent {
            TransferToSavingsGoalModal(
                uiState = testUiState,
                amount = fakeTransferAmount.toString(),
                viewModel = fakeViewModel
            )
        }

        val context = InstrumentationRegistry.getInstrumentation().targetContext

        composeTestRule.onNodeWithText(context.getString(R.string.transfer_to_savings_modal_create_and_transfer))
            .performClick()

        // Look for Savings Goal creation dialog header
        composeTestRule
            .onNodeWithText(context.getString(R.string.create_savings_goal_dialog_title))
            .assertExists()
    }
}
