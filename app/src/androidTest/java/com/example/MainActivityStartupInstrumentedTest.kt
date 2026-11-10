package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Exercises the real launcher Activity, not an isolated Compose preview.
 * This catches startup crashes in ViewModel creation, WorkManager setup,
 * navigation, SplashScreen, and first-run onboarding on every CI API level.
 */
@RunWith(AndroidJUnit4::class)
class MainActivityStartupInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun launcherActivityReachesFirstRunWelcomeScreen() {
        composeRule.waitUntil(timeoutMillis = 20_000) {
            composeRule
                .onAllNodesWithText("منظومة رقمية متكاملة")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        composeRule.onNodeWithText("منظومة رقمية متكاملة").assertIsDisplayed()
    }
}
