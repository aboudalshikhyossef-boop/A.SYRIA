package com.example.ui.design

import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class GlassLevel {
    Subtle,
    Standard,
    Elevated,
    Modal
}

private fun GlassLevel.fillAlpha(): Float = when (this) {
    GlassLevel.Subtle -> AyriaDesignTokens.Glass.subtleFillAlpha
    GlassLevel.Standard -> AyriaDesignTokens.Glass.standardFillAlpha
    GlassLevel.Elevated -> AyriaDesignTokens.Glass.elevatedFillAlpha
    GlassLevel.Modal -> AyriaDesignTokens.Glass.modalFillAlpha
}

private fun GlassLevel.fallbackAlpha(): Float = when (this) {
    GlassLevel.Subtle -> AyriaDesignTokens.Glass.fallbackSubtleAlpha
    GlassLevel.Standard -> AyriaDesignTokens.Glass.fallbackStandardAlpha
    GlassLevel.Elevated -> AyriaDesignTokens.Glass.fallbackElevatedAlpha
    GlassLevel.Modal -> AyriaDesignTokens.Glass.fallbackModalAlpha
}

private fun GlassLevel.elevation(): Dp = when (this) {
    GlassLevel.Subtle -> AyriaDesignTokens.Shadows.subtle
    GlassLevel.Standard -> AyriaDesignTokens.Shadows.card
    GlassLevel.Elevated -> AyriaDesignTokens.Shadows.card
    GlassLevel.Modal -> AyriaDesignTokens.Shadows.modal
}

/**
 * Base Frosted Glass primitive.
 *
 * backdropContent is an isolated visual copy/source for this surface. It is blurred
 * independently on API 31+, while the foreground content remains sharp. Callers
 * that do not supply a backdrop still receive the tinted glass treatment.
 */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = AyriaDesignTokens.Corners.card,
    level: GlassLevel = GlassLevel.Standard,
    backdropContent: (@Composable BoxScope.() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val platformBlurAvailable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val fill = if (platformBlurAvailable) {
        AyriaDesignTokens.Colors.pearl.copy(alpha = level.fillAlpha())
    } else {
        AyriaDesignTokens.Colors.panel.copy(alpha = level.fallbackAlpha())
    }

    Box(
        modifier = modifier
            .shadow(elevation = level.elevation(), shape = shape, clip = false)
            .clip(shape)
    ) {
        if (backdropContent != null) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .then(
                        if (platformBlurAvailable) {
                            Modifier.blur(AyriaDesignTokens.Glass.backdropBlurRadius)
                        } else {
                            Modifier
                        }
                    )
            ) {
                backdropContent()
            }
        }

        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            fill.copy(alpha = (fill.alpha + 0.035f).coerceAtMost(1f)),
                            fill,
                            if (platformBlurAvailable) {
                                AyriaDesignTokens.Colors.deep.copy(alpha = 0.18f)
                            } else {
                                AyriaDesignTokens.Colors.abyss.copy(alpha = 0.22f)
                            }
                        )
                    )
                )
        )

        content()

        Box(
            modifier = Modifier
                .matchParentSize()
                .border(
                    BorderStroke(
                        1.dp,
                        Brush.verticalGradient(
                            colors = listOf(
                                AyriaDesignTokens.Colors.pearl.copy(
                                    alpha = AyriaDesignTokens.Glass.borderTopAlpha
                                ),
                                AyriaDesignTokens.Colors.glacier.copy(alpha = 0.12f),
                                AyriaDesignTokens.Colors.pearl.copy(
                                    alpha = AyriaDesignTokens.Glass.borderBottomAlpha
                                )
                            )
                        )
                    ),
                    shape
                )
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(1.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            AyriaDesignTokens.Colors.pearl.copy(alpha = 0.24f),
                            AyriaDesignTokens.Colors.glacier.copy(alpha = 0.20f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    level: GlassLevel = GlassLevel.Standard,
    contentPadding: Dp = AyriaDesignTokens.Spacing.lg,
    backdropContent: (@Composable BoxScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    GlassSurface(
        modifier = modifier,
        shape = AyriaDesignTokens.Corners.card,
        level = level,
        backdropContent = backdropContent
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding),
            content = content
        )
    }
}

@Composable
fun GlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    level: GlassLevel = GlassLevel.Elevated,
    content: @Composable RowScope.() -> Unit
) {
    GlassSurface(
        modifier = modifier
            .heightIn(min = 48.dp)
            .clickable(enabled = enabled, onClick = onClick),
        shape = AyriaDesignTokens.Corners.control,
        level = if (enabled) level else GlassLevel.Subtle
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 48.dp)
                .padding(horizontal = AyriaDesignTokens.Spacing.md, vertical = AyriaDesignTokens.Spacing.xs),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

@Composable
fun GlassChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Box(
        modifier = modifier.heightIn(min = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        GlassSurface(
            modifier = Modifier
                .height(36.dp)
                .clip(AyriaDesignTokens.Corners.pill)
                .clickable(enabled = enabled, onClick = onClick),
            shape = AyriaDesignTokens.Corners.pill,
            level = if (selected && enabled) GlassLevel.Elevated else GlassLevel.Subtle
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (selected) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(AyriaDesignTokens.Colors.teal, CircleShape)
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    text = label,
                    color = if (enabled) {
                        if (selected) AyriaDesignTokens.Colors.pearl else AyriaDesignTokens.Colors.muted
                    } else {
                        AyriaDesignTokens.Colors.dim
                    },
                    fontSize = 12.sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = AyriaDesignTokens.Colors.pearl,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    color = AyriaDesignTokens.Colors.muted,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }

        if (!actionLabel.isNullOrBlank() && onAction != null) {
            TextButton(onClick = onAction) {
                Text(
                    text = actionLabel,
                    color = AyriaDesignTokens.Colors.teal,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 72.dp,
    strokeWidth: Dp = 6.dp,
    label: String? = null,
    valueLabel: String = "${(progress.coerceIn(0f, 1f) * 100).toInt()}%"
) {
    val safeProgress = progress.coerceIn(0f, 1f)
    Column(
        modifier = modifier.semantics {
            progressBarRangeInfo = ProgressBarRangeInfo(safeProgress, 0f..1f)
        },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier.size(size),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.foundation.Canvas(modifier = Modifier.matchParentSize()) {
                drawArc(
                    color = AyriaDesignTokens.Colors.line.copy(alpha = 0.80f),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                )
                drawArc(
                    color = AyriaDesignTokens.Colors.teal,
                    startAngle = -90f,
                    sweepAngle = 360f * safeProgress,
                    useCenter = false,
                    style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                )
            }
            Text(
                text = valueLabel,
                color = AyriaDesignTokens.Colors.pearl,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
        if (!label.isNullOrBlank()) {
            Text(
                text = label,
                color = AyriaDesignTokens.Colors.muted,
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * The visual sheet surface. A ModalBottomSheet host owns the modal window, gestures,
 * insets and scrim; this component owns the shared glass material and sheet content.
 */
@Composable
fun GlassSheet(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    backdropContent: (@Composable BoxScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    GlassSurface(
        modifier = modifier.fillMaxWidth(),
        shape = AyriaDesignTokens.Corners.sheet,
        level = GlassLevel.Modal,
        backdropContent = backdropContent
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = AyriaDesignTokens.Spacing.lg,
                    vertical = AyriaDesignTokens.Spacing.md
                )
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(width = 36.dp, height = 4.dp)
                    .background(
                        AyriaDesignTokens.Colors.pearl.copy(alpha = 0.38f),
                        CircleShape
                    )
            )
            Spacer(Modifier.height(AyriaDesignTokens.Spacing.md))
            Text(
                text = title,
                color = AyriaDesignTokens.Colors.pearl,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    color = AyriaDesignTokens.Colors.muted,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
            Spacer(Modifier.height(AyriaDesignTokens.Spacing.md))
            content()
        }
    }
}
