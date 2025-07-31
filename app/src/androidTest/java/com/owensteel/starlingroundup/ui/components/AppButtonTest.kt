package com.owensteel.starlingroundup.ui.components

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class AppButtonTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun appButton_displaysTextAndIsClickable() {
        var clicked = false

        composeTestRule.setContent {
            AppButton(
                text = "Test Button",
                onClick = { clicked = true }
            )
        }

        // Verify it's displayed
        composeTestRule.onNodeWithText("Test Button").assertIsDisplayed()

        // Click the button
        composeTestRule.onNodeWithText("Test Button").performClick()

        // Check if clicked
        assertTrue(clicked)
    }

    @Test
    fun appButton_disabledIsNotClickable() {
        var clicked = false

        composeTestRule.setContent {
            AppButton(
                text = "Disabled Button",
                enabled = false,
                onClick = { clicked = true }
            )
        }

        // Verify it's displayed but disabled
        composeTestRule.onNodeWithText("Disabled Button")
            .assertIsDisplayed()
            .assertIsNotEnabled()

        // Attempt click
        composeTestRule.onNodeWithText("Disabled Button").performClick()

        // Verify click didn't happen
        assertFalse(clicked)
    }

    @Test
    fun appSecondaryButton_behavesCorrectly() {
        var clicked = false

        composeTestRule.setContent {
            AppSecondaryButton(
                text = "Secondary",
                onClick = { clicked = true }
            )
        }

        composeTestRule.onNodeWithText("Secondary").assertIsDisplayed()
        composeTestRule.onNodeWithText("Secondary").performClick()

        assertTrue(clicked)
    }
}
