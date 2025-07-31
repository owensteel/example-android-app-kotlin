package com.owensteel.starlingroundup.ui.dialogs

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Currency
import com.owensteel.starlingroundup.R
import com.owensteel.starlingroundup.fakes.api.FakeStarlingServiceResponses.FakeAccount

@OptIn(ExperimentalComposeUiApi::class)
@RunWith(AndroidJUnit4::class)
class NewSavingsGoalDialogTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testCurrency = Currency.getInstance(FakeAccount.currency)

    @Test
    fun testValidInput_callsOnConfirm() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        var confirmedName: String? = null

        composeTestRule.setContent {
            NewSavingsGoalDialog(
                onDismiss = {},
                onConfirm = { name, target ->
                    confirmedName = name
                },
                accountCurrency = testCurrency
            )
        }

        composeTestRule
            .onNodeWithText(context.getString(R.string.create_savings_goal_dialog_goal_name_input_label))
            .performTextInput("Holiday Fund")

        composeTestRule
            .onNodeWithText(context.getString(R.string.create_savings_goal_dialog_goal_finish_button))
            .performClick()

        assertEquals("Holiday Fund", confirmedName)
    }

    @Test
    fun testBlankGoalName_showsError() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        composeTestRule.setContent {
            NewSavingsGoalDialog(
                onDismiss = {},
                onConfirm = { _, _ -> },
                accountCurrency = testCurrency
            )
        }

        composeTestRule
            .onNodeWithText(context.getString(R.string.create_savings_goal_dialog_goal_target_input_label))
            .performTextInput("100")

        composeTestRule
            .onNodeWithText(context.getString(R.string.create_savings_goal_dialog_goal_finish_button))
            .performClick()

        composeTestRule
            .onNodeWithText(context.getString(R.string.create_savings_goal_dialog_error_invalid_name))
            .assertExists()
    }
}
