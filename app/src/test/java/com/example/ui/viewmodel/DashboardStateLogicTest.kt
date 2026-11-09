package com.example.ui.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Test

class DashboardStateLogicTest {

    @Test
    fun startupRemainsOnSplashUntilAllRequiredSettingsAreLoaded() {
        assertEquals(
            DashboardRoutes.SPLASH,
            resolveDashboardStartDestination(
                isDataLoaded = false,
                onboardingCompleted = false,
                calibrationCompleted = false
            )
        )
        assertEquals(
            DashboardRoutes.SPLASH,
            resolveDashboardStartDestination(
                isDataLoaded = false,
                onboardingCompleted = true,
                calibrationCompleted = true
            )
        )
    }

    @Test
    fun startupRoutesToOnboardingBeforeCalibration() {
        assertEquals(
            DashboardRoutes.ONBOARDING,
            resolveDashboardStartDestination(
                isDataLoaded = true,
                onboardingCompleted = false,
                calibrationCompleted = false
            )
        )
    }

    @Test
    fun startupRoutesToCalibrationAfterOnboarding() {
        assertEquals(
            DashboardRoutes.CALIBRATION,
            resolveDashboardStartDestination(
                isDataLoaded = true,
                onboardingCompleted = true,
                calibrationCompleted = false
            )
        )
    }

    @Test
    fun startupRoutesToDashboardOnlyAfterBothStepsComplete() {
        assertEquals(
            DashboardRoutes.DASHBOARD,
            resolveDashboardStartDestination(
                isDataLoaded = true,
                onboardingCompleted = true,
                calibrationCompleted = true
            )
        )
    }

    @Test
    fun cyberProgressClampsLowerAndUpperBounds() {
        assertEquals(0f, cyberProgressForScore(-10), 0.0001f)
        assertEquals(0f, cyberProgressForScore(0), 0.0001f)
        assertEquals(1f, cyberProgressForScore(200), 0.0001f)
    }

    @Test
    fun cyberProgressUsesExpectedThresholds() {
        assertEquals(1f, cyberProgressForScore(50), 0.0001f)
        assertEquals(0.01f, cyberProgressForScore(51), 0.0001f)
        assertEquals(1f, cyberProgressForScore(150), 0.0001f)
        assertEquals(1f, cyberProgressForScore(151), 0.0001f)
    }

    @Test
    fun cyberRankUsesExpectedThresholdsInEnglishAndArabic() {
        assertEquals("Novice Cipher", cyberRankForScore(50, isArabic = false))
        assertEquals("Sentinel Guard", cyberRankForScore(51, isArabic = false))
        assertEquals("Sentinel Guard", cyberRankForScore(150, isArabic = false))
        assertEquals("Sovereign Ghost", cyberRankForScore(151, isArabic = false))

        assertEquals("شيفرة مبتدئة", cyberRankForScore(50, isArabic = true))
        assertEquals("حارس السينتينل", cyberRankForScore(51, isArabic = true))
        assertEquals("شبح سيادي", cyberRankForScore(151, isArabic = true))
    }
}
