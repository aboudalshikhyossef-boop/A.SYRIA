package com.example.ui.design

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import androidx.compose.ui.tooling.preview.Preview
import com.example.ui.brand.AyriaArabicFont
import com.example.ui.brand.AyriaPanoramaBackdrop

@Composable
internal fun AyriaDesignSystemPreviewContent() {
    androidx.compose.runtime.CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Rtl
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AyriaDesignTokens.Colors.abyss)
        ) {
            AyriaPanoramaBackdrop(pageIndex = 2, scrimAlpha = 0.20f)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 28.dp),
                verticalArrangement = Arrangement.spacedBy(AyriaDesignTokens.Spacing.md)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "A.SYRIA",
                        color = AyriaDesignTokens.Colors.glacier,
                        fontFamily = AyriaArabicFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "DESIGN SYSTEM  /  01",
                        color = AyriaDesignTokens.Colors.muted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "نظام التصميم",
                        color = AyriaDesignTokens.Colors.pearl,
                        fontFamily = AyriaArabicFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp
                    )
                    Text(
                        text = "Polar Frosted Glass · المكوّنات الأساسية",
                        color = AyriaDesignTokens.Colors.muted,
                        fontSize = 12.sp
                    )
                }

                SectionHeader(
                    title = "السطح الأساسي",
                    subtitle = "GlassSurface · تعبئة شفافة وحد داخلي ولمعة علوية"
                )

                GlassSurface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(88.dp),
                    level = GlassLevel.Standard,
                    backdropContent = { PreviewBackdropPatch() }
                ) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(horizontal = 20.dp)
                    ) {
                        Text(
                            text = "طبقة زجاجية",
                            color = AyriaDesignTokens.Colors.pearl,
                            fontFamily = AyriaArabicFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Text(
                            text = "الخلفية مطموسة، والمحتوى الأمامي حاد",
                            color = AyriaDesignTokens.Colors.muted,
                            fontFamily = AyriaArabicFont,
                            fontSize = 12.sp
                        )
                    }
                }

                SectionHeader(
                    title = "البطاقات والأزرار",
                    subtitle = "مقياس حواف 24dp و16dp وأهداف لمس لا تقل عن 48dp"
                )

                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backdropContent = { PreviewBackdropPatch() }
                ) {
                    Text(
                        text = "مساحة زجاجية هادئة",
                        color = AyriaDesignTokens.Colors.pearl,
                        fontFamily = AyriaArabicFont,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "تدرج جليدي خفيف، حافة داخلية دقيقة، وظل ناعم.",
                        color = AyriaDesignTokens.Colors.muted,
                        fontFamily = AyriaArabicFont,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GlassButton(
                        onClick = {},
                        modifier = Modifier.weight(1f),
                        level = GlassLevel.Elevated
                    ) {
                        Text(
                            text = "إجراء أساسي",
                            color = AyriaDesignTokens.Colors.pearl,
                            fontFamily = AyriaArabicFont,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                    GlassButton(
                        onClick = {},
                        modifier = Modifier.weight(1f),
                        level = GlassLevel.Subtle
                    ) {
                        Text(
                            text = "ثانوي",
                            color = AyriaDesignTokens.Colors.muted,
                            fontFamily = AyriaArabicFont,
                            fontSize = 13.sp
                        )
                    }
                }

                SectionHeader(
                    title = "شرائح الحالات",
                    subtitle = "محدد · عادي · معطل"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GlassChip(label = "محدد", selected = true, onClick = {})
                    GlassChip(label = "الكل", selected = false, onClick = {})
                    GlassChip(label = "معطل", selected = false, enabled = false, onClick = {})
                }

                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = 16.dp,
                    level = GlassLevel.Elevated,
                    backdropContent = { PreviewBackdropPatch() }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "ProgressRing",
                                color = AyriaDesignTokens.Colors.pearl,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "مؤشر دائري مع دلالة تقدم",
                                color = AyriaDesignTokens.Colors.muted,
                                fontFamily = AyriaArabicFont,
                                fontSize = 12.sp
                            )
                        }
                        ProgressRing(progress = 0.72f, label = "الإنجاز")
                    }
                }

                GlassSheet(
                    title = "GlassSheet",
                    subtitle = "سطح نافذة سفلية مع مقبض علوي",
                    modifier = Modifier.fillMaxWidth(),
                    backdropContent = { PreviewBackdropPatch() }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GlassChip(label = "تأكيد", selected = true, onClick = {})
                        GlassChip(label = "إلغاء", selected = false, onClick = {})
                    }
                }

                Text(
                    text = "A.SYRIA  ·  POLAR FROSTED GLASS",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    color = AyriaDesignTokens.Colors.dim,
                    fontSize = 10.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun PreviewBackdropPatch() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    AyriaDesignTokens.Colors.ocean,
                    AyriaDesignTokens.Colors.deep,
                    AyriaDesignTokens.Colors.abyss
                ),
                start = Offset.Zero,
                end = Offset(size.width, size.height)
            )
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    AyriaDesignTokens.Colors.ion.copy(alpha = 0.46f),
                    AyriaDesignTokens.Colors.ion.copy(alpha = 0.08f),
                    androidx.compose.ui.graphics.Color.Transparent
                ),
                center = Offset(size.width * 0.78f, size.height * 0.18f),
                radius = size.minDimension * 0.90f
            ),
            radius = size.minDimension * 0.90f,
            center = Offset(size.width * 0.78f, size.height * 0.18f)
        )

        val shard = Path().apply {
            moveTo(size.width * 0.08f, size.height * 0.85f)
            lineTo(size.width * 0.40f, size.height * 0.14f)
            lineTo(size.width * 0.74f, size.height * 0.83f)
            close()
        }
        drawPath(
            path = shard,
            color = AyriaDesignTokens.Colors.glacier.copy(alpha = 0.16f)
        )
        drawPath(
            path = shard,
            color = AyriaDesignTokens.Colors.glacier.copy(alpha = 0.30f),
            style = Stroke(width = 1.dp.toPx())
        )
    }
}

@Preview(
    name = "A.SYRIA · Design System",
    showBackground = true,
    widthDp = 393,
    heightDp = 852
)
@Composable
fun AyriaDesignSystemPreview() {
    AyriaDesignSystemPreviewContent()
}
