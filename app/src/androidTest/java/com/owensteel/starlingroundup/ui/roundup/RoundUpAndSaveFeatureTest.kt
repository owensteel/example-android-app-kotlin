package com.owensteel.starlingroundup.ui.roundup

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.owensteel.starlingroundup.MainActivity
import com.owensteel.starlingroundup.R
import com.owensteel.starlingroundup.fakes.api.FakeStarlingServiceResponses.FakeAccount
import com.owensteel.starlingroundup.fakes.api.FakeStarlingServiceResponses.FakeAccountHolder
import com.owensteel.starlingroundup.fakes.api.FakeStarlingServiceResponses.FakeTransactionFeed
import com.owensteel.starlingroundup.model.Money
import com.owensteel.starlingroundup.usecase.CalculateRoundUpUseCase
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.Instant

@HiltAndroidTest
class RoundUpAndSaveFeatureTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private lateinit var calculateRoundUpUseCase: CalculateRoundUpUseCase

    @Before
    fun setUp() {
        hiltRule.inject()

        calculateRoundUpUseCase = CalculateRoundUpUseCase()
    }

    @Test
    fun displaysGreetingAndIntro_andAmount_andLastRoundUp_whenValidInput() {
        val cutoffTimestamp = Instant.EPOCH.toString()
        val roundUpAmountText = Money(
            FakeAccount.currency,
            calculateRoundUpUseCase(
                FakeTransactionFeed.feedItems,
                cutoffTimestamp
            )
        ).toString()
        val accountHolderName = FakeAccountHolder.firstName

        val context = InstrumentationRegistry.getInstrumentation().targetContext

        // Check greeting (e.g "Hello, Joe!")
        composeTestRule
            .onNodeWithText(
                context.getString(
                    R.string.main_feature_greeting,
                    accountHolderName
                )
            )
            .assertIsDisplayed()
        // Check intro
        composeTestRule
            .onNodeWithText(context.getString(R.string.main_feature_intro))
            .assertIsDisplayed()

        // Check amount is formatted and displayed correctly
        composeTestRule
            .onNodeWithText(roundUpAmountText)
            .assertIsDisplayed()

        // Check button
        composeTestRule
            .onNodeWithText(context.getString(R.string.main_feature_button_round_up_and_save))
            .assertIsEnabled()
    }

    @Test
    fun clickingButton_callsViewModelMethods() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        composeTestRule
            .onNodeWithText(context.getString(R.string.main_feature_button_round_up_and_save))
            .performClick()

        // Check for modal
        composeTestRule
            .onNodeWithText(context.getString(R.string.transfer_to_savings_modal_intro))
            .assertIsDisplayed()
    }
}
