package com.example.adaptive

import org.junit.Assert.assertEquals
import org.junit.Test

class WindowAdaptiveLayoutTest {

    @Test
    fun widthBreakpointsCoverPhoneTabletAndLargeDisplays() {
        assertEquals(WindowWidthClass.COMPACT, classifyWindowWidth(599))
        assertEquals(WindowWidthClass.MEDIUM, classifyWindowWidth(600))
        assertEquals(WindowWidthClass.MEDIUM, classifyWindowWidth(839))
        assertEquals(WindowWidthClass.EXPANDED, classifyWindowWidth(840))
        assertEquals(WindowWidthClass.EXPANDED, classifyWindowWidth(1_199))
        assertEquals(WindowWidthClass.LARGE, classifyWindowWidth(1_200))
        assertEquals(WindowWidthClass.LARGE, classifyWindowWidth(1_599))
        assertEquals(WindowWidthClass.EXTRA_LARGE, classifyWindowWidth(1_600))
    }

    @Test
    fun heightBreakpointsCoverShortAndTallWindows() {
        assertEquals(WindowHeightClass.COMPACT, classifyWindowHeight(479))
        assertEquals(WindowHeightClass.MEDIUM, classifyWindowHeight(480))
        assertEquals(WindowHeightClass.MEDIUM, classifyWindowHeight(899))
        assertEquals(WindowHeightClass.EXPANDED, classifyWindowHeight(900))
    }
}
