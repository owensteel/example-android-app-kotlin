package com.owensteel.starlingroundup

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.owensteel.starlingroundup.di.StarlingServiceModule
import com.owensteel.starlingroundup.fakes.security.FakeDeviceSecurityCheckModule
import com.owensteel.starlingroundup.util.DeviceSecurityCheck
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@UninstallModules(StarlingServiceModule::class)
@RunWith(AndroidJUnit4::class)
class MainActivityInstTest {

    private val deviceSecurityCheck = DeviceSecurityCheck()

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun checksDeviceSecurityAndFinishesIfCompromised() {
        // Simulate suspicious class
        // We're using a Fake Device Security Check
        // module so we can edit it before it is injected
        // into MainActivity, and thus simulate this
        FakeDeviceSecurityCheckModule.sharedDeviceSecurityCheck.classLoaderHook = { name ->
            if (name == "de.robv.android.xposed.XposedBridge") {
                String::class.java
            } else {
                throw ClassNotFoundException()
            }
        }
        // Make sure the DeviceSecurityCheck we're injecting has actually
        // spotted the suspicious class
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val result = FakeDeviceSecurityCheckModule.sharedDeviceSecurityCheck.isCompromised(context)
        assertTrue(
            "DeviceSecurityCheck returns positive result when suspicious class is simulated",
            result
        )

        // Now simulate a startup in these conditions
        val scenario = ActivityScenario.launch(MainActivity::class.java)

        // Should respond to simulated positive result and finish immediately
        assertTrue(
            "MainActivity should finish due to compromised device",
            scenario.state == Lifecycle.State.DESTROYED
        )

        // Cleanup
        deviceSecurityCheck.classLoaderHook = { Class.forName(it) }
    }

    @Test
    fun loadsMainScreenComposableIfDeviceSecurityCheckCleared() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val expectedText = context.getString(R.string.main_feature_intro)

        composeTestRule.onNodeWithText(expectedText)
            .assertIsDisplayed()
    }
}
