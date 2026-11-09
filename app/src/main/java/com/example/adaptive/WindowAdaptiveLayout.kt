package com.example.adaptive

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * App-level layout classes derived from the available Compose window constraints.
 *
 * Measuring the app window in dp (rather than physical pixels or device names)
 * keeps layouts correct across densities, split-screen, rotation, and foldables.
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
    val heightClass: WindowHeightClass,
    val isLandscapeWindow: Boolean = false
) {
    /** Use a side rail when there is enough horizontal space for persistent navigation. */
    val usesNavigationRail: Boolean
        get() = widthClass != WindowWidthClass.COMPACT

    /** Reflects the actual window aspect ratio, not the device orientation setting. */
    val prefersLandscapeLayout: Boolean
        get() = isLandscapeWindow

    /** Prevents content from stretching to unreadable line lengths on wide displays. */
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
 * Provides responsive information from the current app window constraints.
 *
 * BoxWithConstraints is remeasured when the app window changes, including rotation,
 * resizing in multi-window mode, and folding/unfolding. It also works on older
 * Android versions without relying on API-level-specific display metrics.
 */
@Composable
fun ProvideWindowAdaptiveLayout(content: @Composable () -> Unit) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val widthDp = maxWidth.value.roundToInt()
        val heightDp = maxHeight.value.roundToInt()
        val layoutInfo = remember(widthDp, heightDp) {
            WindowAdaptiveLayoutInfo(
                widthClass = classifyWindowWidth(widthDp),
                heightClass = classifyWindowHeight(heightDp),
                isLandscapeWindow = prefersLandscapeWindow(widthDp, heightDp)
            )
        }

        CompositionLocalProvider(
            LocalWindowAdaptiveInfo provides layoutInfo,
            content = content
        )
    }
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

internal fun prefersLandscapeWindow(widthDp: Int, heightDp: Int): Boolean =
    widthDp > heightDp


/**
 * Centers secondary destinations and prevents oversized line lengths on tablets.
 * The child still receives the full available height and scrolls within its own screen.
 */
@Composable
fun AdaptiveContentFrame(content: @Composable () -> Unit) {
    val layoutInfo = LocalWindowAdaptiveInfo.current
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = layoutInfo.maxContentWidth)
        ) {
            content()
        }
    }
}
