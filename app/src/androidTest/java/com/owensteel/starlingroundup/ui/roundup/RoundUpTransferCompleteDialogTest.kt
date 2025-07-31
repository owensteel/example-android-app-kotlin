package com.owensteel.starlingroundup.ui.roundup

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.platform.app.InstrumentationRegistry
import com.owensteel.starlingroundup.R
import com.owensteel.starlingroundup.fakes.api.FakeStarlingServiceResponses
import com.owensteel.starlingroundup.model.Money
import com.owensteel.starlingroundup.model.uistates.RoundUpUiState
import org.junit.Rule
import org.junit.Test

class RoundUpTransferCompleteDialogTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testRoundUpTransferCompleteDialog_displaysCorrectly() {
        val fakeRoundUpTotal = Money(FakeStarlingServiceResponses.FakeAccount.currency, 1234)

        val testUiState = RoundUpUiState(
            roundUpTransferCompleteDialogRoundUpTotal = fakeRoundUpTotal,
            roundUpTransferCompleteDialogChosenSavingsGoalName = FakeStarlingServiceResponses.FakeSavingsGoal.name
        )

        composeTestRule.setContent {
            RoundUpTransferCompleteDialog(
                uiState = testUiState,
                onDismiss = {}
            )
        }

        // Icon
        composeTestRule.onNodeWithContentDescription(Icons.Default.Done.name)
            .assertExists()

        // Text (bold parts won't affect testability of the content)
        composeTestRule.onNode(hasText(fakeRoundUpTotal.toString(), true))
            .assertExists()
        composeTestRule.onNode(hasText(FakeStarlingServiceResponses.FakeSavingsGoal.name, true))
            .assertExists()

        val context = InstrumentationRegistry.getInstrumentation().targetContext

        // Button
        composeTestRule.onNodeWithText(context.getString(R.string.dialog_button_ok))
            .assertExists()
            .assertHasClickAction()
    }

}