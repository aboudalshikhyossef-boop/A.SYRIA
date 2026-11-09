package com.example.adaptive

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
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

    @Test
    fun compactPhoneWindowUsesBottomNavigationClass() {
        val info = renderWindow(width = 390, height = 844)

        assertEquals(WindowWidthClass.COMPACT, info.widthClass)
        assertEquals(false, info.usesNavigationRail)
        assertEquals(WindowHeightClass.MEDIUM, info.heightClass)
    }

    @Test
    fun mediumWindowUsesNavigationRailClass() {
        val info = renderWindow(width = 700, height = 1_000)

        assertEquals(WindowWidthClass.MEDIUM, info.widthClass)
        assertEquals(true, info.usesNavigationRail)
        assertEquals(WindowHeightClass.EXPANDED, info.heightClass)
    }

    @Test
    fun expandedAndLargeWindowsKeepReadableContentBounds() {
        val tablet = renderWindow(width = 960, height = 700)
        val desktop = renderWindow(width = 1_300, height = 900)

        assertEquals(WindowWidthClass.EXPANDED, tablet.widthClass)
        assertEquals(960.dp, tablet.maxContentWidth)
        assertEquals(WindowWidthClass.LARGE, desktop.widthClass)
        assertEquals(1_120.dp, desktop.maxContentWidth)
    }

    private fun renderWindow(width: Int, height: Int): WindowAdaptiveLayoutInfo {
        var observedInfo: WindowAdaptiveLayoutInfo? = null

        composeRule.setContent {
            Box(modifier = Modifier.size(width.dp, height.dp)) {
                ProvideWindowAdaptiveLayout {
                    observedInfo = LocalWindowAdaptiveInfo.current
                    Text("Adaptive layout ready")
                }
            }
        }
        composeRule.waitForIdle()

        return requireNotNull(observedInfo)
    }
}
