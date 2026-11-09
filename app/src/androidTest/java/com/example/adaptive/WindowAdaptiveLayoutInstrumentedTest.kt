package com.example.adaptive

import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WindowAdaptiveLayoutInstrumentedTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun adaptiveProviderExposesWindowClassesAtRuntime() {
        var observedInfo: WindowAdaptiveLayoutInfo? = null

        composeRule.setContent {
            ProvideWindowAdaptiveLayout {
                observedInfo = LocalWindowAdaptiveInfo.current
                Text("Adaptive layout ready")
            }
        }

        composeRule.onNodeWithText("Adaptive layout ready").assertIsDisplayed()
        composeRule.runOnIdle {
            assertNotNull(observedInfo)
            assertNotNull(observedInfo?.widthClass)
            assertNotNull(observedInfo?.heightClass)
        }
    }
}
