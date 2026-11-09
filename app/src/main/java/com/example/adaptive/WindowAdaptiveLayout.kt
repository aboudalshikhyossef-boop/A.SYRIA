package com.example.adaptive

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2

/**
 * App-level width breakpoints based on the available app window, not the device model.
 *
 * Values follow Android's recommended window size classes and are deliberately
 * kept independent from UIConfig, which stores user-selected visual preferences.
 */
enum class WindowWidthClass {
    COMPACT,
    MEDIUM,
    EXPANDED,
    LARGE,
    EXTRA_LARGE
}

enum class WindowHeightClass {
    COMPACT,
    MEDIUM,
    EXPANDED
}

data class WindowAdaptiveLayoutInfo(
    val widthClass: WindowWidthClass,
    val heightClass: WindowHeightClass
) {
    /** Use a side rail when there is enough horizontal space for persistent navigation. */
    val usesNavigationRail: Boolean
        get() = widthClass != WindowWidthClass.COMPACT

    /** Wide layouts can use horizontal arrangements; short windows should avoid tall stacks. */
    val prefersLandscapeLayout: Boolean
        get() = widthClass >= WindowWidthClass.EXPANDED ||
            heightClass == WindowHeightClass.COMPACT

    /** Prevents phone and tablet content from stretching to unreadable line lengths. */
    val maxContentWidth: Dp
        get() = when (widthClass) {
            WindowWidthClass.COMPACT -> 600.dp
            WindowWidthClass.MEDIUM -> 760.dp
            WindowWidthClass.EXPANDED -> 960.dp
            WindowWidthClass.LARGE -> 1_120.dp
            WindowWidthClass.EXTRA_LARGE -> 1_280.dp
        }

    val horizontalContentPadding: Dp
        get() = when (widthClass) {
            WindowWidthClass.COMPACT -> 16.dp
            WindowWidthClass.MEDIUM -> 24.dp
            WindowWidthClass.EXPANDED,
            WindowWidthClass.LARGE,
            WindowWidthClass.EXTRA_LARGE -> 32.dp
        }
}

val LocalWindowAdaptiveInfo = staticCompositionLocalOf {
    WindowAdaptiveLayoutInfo(
        widthClass = WindowWidthClass.COMPACT,
        heightClass = WindowHeightClass.MEDIUM
    )
}

/**
 * Provides adaptive information for the current app window.
 *
 * Recalculates automatically when the app window changes due to rotation,
 * resizing, multi-window mode, or folding/unfolding a device.
 */
@Composable
fun ProvideWindowAdaptiveLayout(content: @Composable () -> Unit) {
    val windowSizeClass = currentWindowAdaptiveInfoV2().windowSizeClass
    val layoutInfo = remember(windowSizeClass) {
        WindowAdaptiveLayoutInfo(
            widthClass = classifyWindowWidth(windowSizeClass.minWidthDp),
            heightClass = classifyWindowHeight(windowSizeClass.minHeightDp)
        )
    }

    CompositionLocalProvider(
        LocalWindowAdaptiveInfo provides layoutInfo,
        content = content
    )
}

internal fun classifyWindowWidth(widthDp: Int): WindowWidthClass = when {
    widthDp >= 1_600 -> WindowWidthClass.EXTRA_LARGE
    widthDp >= 1_200 -> WindowWidthClass.LARGE
    widthDp >= 840 -> WindowWidthClass.EXPANDED
    widthDp >= 600 -> WindowWidthClass.MEDIUM
    else -> WindowWidthClass.COMPACT
}

internal fun classifyWindowHeight(heightDp: Int): WindowHeightClass = when {
    heightDp >= 900 -> WindowHeightClass.EXPANDED
    heightDp >= 480 -> WindowHeightClass.MEDIUM
    else -> WindowHeightClass.COMPACT
}
