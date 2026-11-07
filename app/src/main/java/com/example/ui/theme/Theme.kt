package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.adaptive.UIConfig

/** Safely parse a hex string like "#RRGGBB" or "#AARRGGBB" into a Color. */
private fun parseHex(hex: String, fallback: Color): Color {
    if (hex.isBlank()) return fallback
    return try {
        val cleaned = hex.trimStart('#')
        val value = when (cleaned.length) {
            6 -> 0xFF000000.toLong() or cleaned.toLong(16)
            8 -> cleaned.toLong(16)
            else -> return fallback
        }
        Color(value.toInt())
    } catch (e: Exception) { fallback }
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    uiConfig: UIConfig = UIConfig(),
    content: @Composable () -> Unit,
) {
    val namedAccent: Color = when (uiConfig.accentColor) {
        "amber"  -> AmberZen
        "green"  -> Color(0xFF00FF88)
        "white"  -> Color(0xFFE0E0E0)
        "red"    -> Color(0xFFFF3D5A)
        "purple" -> Color(0xFFBB86FC)
        else     -> CyberCyan
    }

    val accentColor   = parseHex(uiConfig.primaryHex, namedAccent)
    val bgColor       = parseHex(uiConfig.backgroundHex, VoidBlack)

    val colorScheme = darkColorScheme(
        primary       = accentColor,
        secondary     = accentColor,
        tertiary      = accentColor,
        background    = bgColor,
        surface       = bgColor,
        onPrimary     = bgColor,
        onSecondary   = bgColor,
        onTertiary    = bgColor,
        onBackground  = accentColor,
        onSurface     = accentColor
    )

    val fw: FontWeight = when (uiConfig.fontWeight) {
        "light" -> FontWeight.Light
        "bold"  -> FontWeight.Bold
        else    -> FontWeight.Normal
    }

    val fs = uiConfig.fontSize
    val dynamicTypography = Typography(
        bodyLarge = TextStyle(
            fontFamily    = FontFamily.Default,
            fontWeight    = fw,
            fontSize      = fs.sp,
            lineHeight    = (fs * 1.5f).sp,
            letterSpacing = 0.5.sp
        ),
        bodyMedium = TextStyle(
            fontFamily    = FontFamily.Default,
            fontWeight    = fw,
            fontSize      = (fs * 0.875f).sp,
            lineHeight    = (fs * 1.35f).sp,
            letterSpacing = 0.25.sp
        ),
        bodySmall = TextStyle(
            fontFamily    = FontFamily.Default,
            fontWeight    = fw,
            fontSize      = (fs * 0.75f).sp,
            lineHeight    = (fs * 1.25f).sp,
            letterSpacing = 0.4.sp
        ),
        labelLarge = TextStyle(
            fontFamily    = FontFamily.Default,
            fontWeight    = FontWeight.Medium,
            fontSize      = (fs * 0.875f).sp,
            lineHeight    = (fs * 1.4f).sp,
            letterSpacing = 0.1.sp
        ),
        labelMedium = TextStyle(
            fontFamily    = FontFamily.Default,
            fontWeight    = FontWeight.Medium,
            fontSize      = (fs * 0.75f).sp,
            lineHeight    = (fs * 1.3f).sp,
            letterSpacing = 0.5.sp
        ),
        labelSmall = TextStyle(
            fontFamily    = FontFamily.Default,
            fontWeight    = FontWeight.Medium,
            fontSize      = (fs * 0.6875f).sp,
            lineHeight    = (fs * 1.2f).sp,
            letterSpacing = 0.5.sp
        ),
        titleLarge = TextStyle(
            fontFamily    = FontFamily.Default,
            fontWeight    = FontWeight.Bold,
            fontSize      = (fs * 1.57f).sp,
            lineHeight    = (fs * 2f).sp,
            letterSpacing = 0.sp
        ),
        titleMedium = TextStyle(
            fontFamily    = FontFamily.Default,
            fontWeight    = FontWeight.Bold,
            fontSize      = (fs * 1.14f).sp,
            lineHeight    = (fs * 1.7f).sp,
            letterSpacing = 0.15.sp
        )
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = dynamicTypography,
        content     = content
    )
}
