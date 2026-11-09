package com.example.ui.viewmodel

/**
 * Pure dashboard business rules shared by the ViewModel and navigation layer.
 *
 * Keeping these decisions outside Compose and Android lifecycle code makes
 * startup routing and score thresholds deterministic and unit-testable.
 */
internal object DashboardRoutes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val CALIBRATION = "calibration"
    const val DASHBOARD = "dashboard"
}

internal fun resolveDashboardStartDestination(
    isDataLoaded: Boolean,
    onboardingCompleted: Boolean,
    calibrationCompleted: Boolean
): String {
    if (!isDataLoaded) return DashboardRoutes.SPLASH

    return when {
        !onboardingCompleted -> DashboardRoutes.ONBOARDING
        !calibrationCompleted -> DashboardRoutes.CALIBRATION
        else -> DashboardRoutes.DASHBOARD
    }
}

internal fun cyberProgressForScore(score: Int): Float = when {
    score <= 50 -> (score.toFloat() / 50f).coerceIn(0f, 1f)
    score <= 150 -> ((score - 50).toFloat() / 100f).coerceIn(0f, 1f)
    else -> 1f
}

internal fun cyberRankForScore(score: Int, isArabic: Boolean): String {
    val rank = when {
        score <= 50 -> 0
        score <= 150 -> 1
        else -> 2
    }

    return if (isArabic) {
        when (rank) {
            0 -> "شيفرة مبتدئة"
            1 -> "حارس السينتينل"
            else -> "شبح سيادي"
        }
    } else {
        when (rank) {
            0 -> "Novice Cipher"
            1 -> "Sentinel Guard"
            else -> "Sovereign Ghost"
        }
    }
}
