package com.example.ui.design

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import com.example.ui.brand.AyriaColors

/** Shared visual tokens for the A.SYRIA Polar Frosted Glass language. */
object AyriaDesignTokens {
    object Colors {
        val abyss: Color = AyriaColors.abyss
        val deep: Color = AyriaColors.deep
        val ocean: Color = AyriaColors.ocean
        val panel: Color = AyriaColors.panel
        val teal: Color = AyriaColors.teal
        val glacier: Color = AyriaColors.glacier
        val ion: Color = AyriaColors.ion
        val pearl: Color = AyriaColors.pearl
        val muted: Color = AyriaColors.muted
        val dim: Color = AyriaColors.dim
        val line: Color = AyriaColors.line
        val glass: Color = AyriaColors.glass
    }

    object Spacing {
        val xxs = 4.dp
        val xs = 8.dp
        val sm = 12.dp
        val md = 16.dp
        val lg = 24.dp
        val xl = 32.dp
    }

    object Corners {
        val card: Shape = RoundedCornerShape(24.dp)
        val control: Shape = RoundedCornerShape(16.dp)
        val pill: Shape = CircleShape
        val sheet: Shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        val icon: Shape = CircleShape
    }

    object Shadows {
        val subtle: Dp = 4.dp
        val card: Dp = 12.dp
        val modal: Dp = 20.dp
    }

    object Motion {
        const val standardMillis = 280
        const val emphasizedMillis = 360
        const val entranceMillis = 320
        const val pressedScale = 0.985f
    }

    object Glass {
        const val subtleFillAlpha = 0.07f
        const val standardFillAlpha = 0.09f
        const val elevatedFillAlpha = 0.11f
        const val modalFillAlpha = 0.12f
        const val borderTopAlpha = 0.28f
        const val borderBottomAlpha = 0.04f
        const val fallbackSubtleAlpha = 0.56f
        const val fallbackStandardAlpha = 0.62f
        const val fallbackElevatedAlpha = 0.68f
        const val fallbackModalAlpha = 0.72f
        val backdropBlurRadius = 18.dp
        val fallbackScrimAlpha = 0.58f
    }
}
