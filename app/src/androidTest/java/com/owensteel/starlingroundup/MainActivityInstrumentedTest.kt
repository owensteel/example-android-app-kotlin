package com.owensteel.starlingroundup

import android.content.Context
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.owensteel.starlingroundup.util.DeviceSecurityCheck
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class MainActivityInstrumentedTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    // Default test
    @Test
    fun useAppContext() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.owensteel.starlingroundup", appContext.packageName)
    }

    @Test
    fun closesApp_whenDeviceIsCompromised() {
        // Simulate Frida detected
        DeviceSecurityCheck.classLoaderHook = { Any::class.java }

        val scenario = ActivityScenario.launch(MainActivity::class.java)
        scenario.onActivity {
            assertTrue(it.isFinishing)
        }

        // Cleanup
        DeviceSecurityCheck.classLoaderHook = { Class.forName(it) }
    }

    @Test
    fun showsRoundUpScreen_whenDeviceIsSecure() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        composeTestRule.onNodeWithText(
            context.getString(R.string.main_feature_intro)
        ).assertExists()
    }

}