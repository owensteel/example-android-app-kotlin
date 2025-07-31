package com.owensteel.starlingroundup.ui.roundup

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.platform.app.InstrumentationRegistry
import com.owensteel.starlingroundup.fakes.api.FakeStarlingServiceResponses.FakeSavingsGoal
import com.owensteel.starlingroundup.fakes.api.FakeStarlingServiceResponses.FakeSavingsGoal2
import com.owensteel.starlingroundup.viewmodel.RoundUpAndSaveViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import com.owensteel.starlingroundup.R
import org.mockito.Mockito.mock

@OptIn(ExperimentalCoroutinesApi::class)
class SavingsGoalsLazyColumnTest {

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var viewModel: RoundUpAndSaveViewModel

    @Before
    fun setup() {
        viewModel = mock()
    }

    @Test
    fun showsEmptyMessage_whenSavingsGoalsListIsEmpty() {
        composeRule.setContent {
            SavingsGoalsLazyColumn(
                savingsGoalsList = emptyList(),
                viewModel = viewModel
            )
        }

        val context = InstrumentationRegistry.getInstrumentation().targetContext

        // Use partial match on string
        composeRule.onNodeWithText(
            substring = true,
            text = context.getString(R.string.transfer_to_savings_modal_goals_empty)
        ).assertIsDisplayed()
    }

    @Test
    fun showsAllSavingsGoalsInList() {
        val goals = listOf(
            FakeSavingsGoal,
            FakeSavingsGoal2
        )

        composeRule.setContent {
            SavingsGoalsLazyColumn(
                savingsGoalsList = goals,
                viewModel = viewModel
            )
        }

        composeRule.onNodeWithText(FakeSavingsGoal.name).assertIsDisplayed()
        composeRule.onNodeWithText(FakeSavingsGoal2.name).assertIsDisplayed()
    }
}
