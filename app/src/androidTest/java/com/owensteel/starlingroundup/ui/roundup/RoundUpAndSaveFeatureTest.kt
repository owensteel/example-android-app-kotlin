package com.owensteel.starlingroundup.ui.roundup

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.platform.app.InstrumentationRegistry
import com.owensteel.starlingroundup.R
import com.owensteel.starlingroundup.fakes.api.FakeStarlingServiceResponses.FakeAccount
import com.owensteel.starlingroundup.fakes.api.FakeStarlingServiceResponses.FakeAccountHolder
import com.owensteel.starlingroundup.fakes.api.FakeStarlingServiceResponses.FakeTransactionFeed
import com.owensteel.starlingroundup.model.Money
import com.owensteel.starlingroundup.model.uistates.RoundUpUiState
import com.owensteel.starlingroundup.usecase.CalculateRoundUpUseCase
import com.owensteel.starlingroundup.viewmodel.RoundUpAndSaveViewModel
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import java.time.Instant

@HiltAndroidTest
class RoundUpAndSaveFeatureTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    private lateinit var roundUpAndSaveViewModel: RoundUpAndSaveViewModel
    private lateinit var calculateRoundUpUseCase: CalculateRoundUpUseCase

    private var defaultCutoffTimestamp = Instant.EPOCH.toString()
    private lateinit var fakeRoundUpAmount: Money

    @Before
    fun setUp() {
        hiltRule.inject()

        roundUpAndSaveViewModel = mock()

        calculateRoundUpUseCase = CalculateRoundUpUseCase()

        fakeRoundUpAmount = Money(
            FakeAccount.currency,
            calculateRoundUpUseCase(
                FakeTransactionFeed.feedItems,
                defaultCutoffTimestamp
            )
        )
    }

    @Test
    fun displaysGreetingAndIntro_andAmount_andLastRoundUp_whenValidInput() {
        composeTestRule.setContent {
            RoundUpAndSaveFeature(
                viewModel = roundUpAndSaveViewModel,
                roundUpAmount = fakeRoundUpAmount,
                accountHolderName = FakeAccountHolder.firstName,
                // Default state
                uiState = RoundUpUiState()
            )
        }

        val context = InstrumentationRegistry.getInstrumentation().targetContext

        // Check greeting (e.g "Hello, Joe!")
        composeTestRule
            .onNodeWithText(
                context.getString(
                    R.string.main_feature_greeting,
                    FakeAccountHolder.firstName
                )
            )
            .assertIsDisplayed()
        // Check intro
        composeTestRule
            .onNodeWithText(context.getString(R.string.main_feature_intro))
            .assertIsDisplayed()

        // Check amount is formatted and displayed correctly
        composeTestRule
            .onNodeWithText(fakeRoundUpAmount.toString())
            .assertIsDisplayed()

        // Check button
        composeTestRule
            .onNodeWithText(context.getString(R.string.main_feature_button_round_up_and_save))
            .assertIsEnabled()
    }

    @Test
    fun disablesButton_whenNoRoundUpAmount() {
        val roundUpAmount = Money(FakeAccount.currency, 0L)
        val uiState = RoundUpUiState(
            transactions = emptyList()
        )

        composeTestRule.setContent {
            RoundUpAndSaveFeature(
                viewModel = roundUpAndSaveViewModel,
                roundUpAmount = roundUpAmount,
                accountHolderName = FakeAccountHolder.firstName,
                uiState = uiState
            )
        }

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        composeTestRule
            .onNodeWithText(context.getString(R.string.main_feature_button_round_up_and_save))
            .assertIsNotEnabled()
    }
}
