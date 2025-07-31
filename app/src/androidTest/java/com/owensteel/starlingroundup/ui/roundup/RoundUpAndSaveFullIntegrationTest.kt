package com.owensteel.starlingroundup.ui.roundup

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.owensteel.starlingroundup.MainActivity
import com.owensteel.starlingroundup.R
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class RoundUpAndSaveFullIntegrationTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun clickingButton_opensModal() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        composeTestRule
            .onNodeWithText(context.getString(R.string.main_feature_button_round_up_and_save))
            .performClick()

        // Look for modal
        composeTestRule
            .onNodeWithText(context.getString(R.string.transfer_to_savings_modal_intro))
            .assertIsDisplayed()
    }

}