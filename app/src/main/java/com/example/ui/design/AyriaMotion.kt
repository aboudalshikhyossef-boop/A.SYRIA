package com.example.ui.design

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.using
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.LayoutDirection

/**
 * Material-style fade-through for peer destinations such as bottom navigation tabs.
 * A near-imperceptible scale keeps the handoff soft without moving the layout.
 */
@Composable
fun <T> AyriaFadeThrough(
    targetState: T,
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.TopStart,
    content: @Composable AnimatedContentScope.(targetState: T) -> Unit
) {
    AnimatedContent(
        targetState = targetState,
        modifier = modifier,
        transitionSpec = {
            (fadeIn(animationSpec = tween(220, delayMillis = 70)) +
                scaleIn(initialScale = 0.985f, animationSpec = tween(220, delayMillis = 70))) togetherWith
                (fadeOut(animationSpec = tween(130)) +
                    scaleOut(targetScale = 0.985f, animationSpec = tween(130)))
        }.using(
            SizeTransform(clip = false) { _, _ -> tween(durationMillis = 1) }
        ),
        contentAlignment = contentAlignment,
        label = "AyriaFadeThrough",
        content = content
    )
}

/** Forward navigation enters from the reading direction's trailing edge. */
fun ayriaForwardEnterTransition(layoutDirection: LayoutDirection): EnterTransition =
    slideInHorizontally(
        animationSpec = tween(360, easing = FastOutSlowInEasing),
        initialOffsetX = { width -> if (layoutDirection == LayoutDirection.Ltr) width else -width }
    ) + fadeIn(animationSpec = tween(220))

/** The previous destination leaves toward the reading direction's leading edge. */
fun ayriaForwardExitTransition(layoutDirection: LayoutDirection): ExitTransition =
    slideOutHorizontally(
        animationSpec = tween(320, easing = FastOutSlowInEasing),
        targetOffsetX = { width -> if (layoutDirection == LayoutDirection.Ltr) -width else width }
    ) + fadeOut(animationSpec = tween(170))

/** Pop navigation reverses the horizontal direction of the forward transition. */
fun ayriaPopEnterTransition(layoutDirection: LayoutDirection): EnterTransition =
    slideInHorizontally(
        animationSpec = tween(340, easing = FastOutSlowInEasing),
        initialOffsetX = { width -> if (layoutDirection == LayoutDirection.Ltr) -width else width }
    ) + fadeIn(animationSpec = tween(220))

fun ayriaPopExitTransition(layoutDirection: LayoutDirection): ExitTransition =
    slideOutHorizontally(
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        targetOffsetX = { width -> if (layoutDirection == LayoutDirection.Ltr) width else -width }
    ) + fadeOut(animationSpec = tween(160))
