package com.example

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.content.ContentValues
import android.os.Build
import android.provider.MediaStore
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.ui.brand.AyriaPanoramaBackdrop
import com.example.ui.screens.DashboardScreen
import com.example.ui.theme.VoidBlack
import com.example.ui.viewmodel.DashboardViewModel
import com.example.ui.viewmodel.PrayerViewModel
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Captures the unmodified phase-3 DashboardScreen at two explicit phone viewports.
 * These baseline images are kept in the GitHub Actions artifact for comparison
 * against the post-redesign captures in Phase 4.
 */
@RunWith(AndroidJUnit4::class)
class DashboardBeforeAfterScreenshotTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun captureDashboardBeforeOnTwoPhoneSizes() {
        assumeTrue("Dashboard comparison screenshots are captured on API 37.", Build.VERSION.SDK_INT >= 37)
        composeRule.mainClock.autoAdvance = false
        val viewport = mutableStateOf(
            DashboardViewport(
                widthDp = 320,
                heightDp = 560,
                tag = "dashboard-before-320x560"
            )
        )
        composeRule.setContent {
            val dashboardViewModel: DashboardViewModel = viewModel()
            val prayerViewModel: PrayerViewModel = viewModel()
            val currentViewport = viewport.value
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(VoidBlack),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = currentViewport.widthDp.dp, height = currentViewport.heightDp.dp)
                            .testTag(currentViewport.tag)
                    ) {
                        AyriaPanoramaBackdrop(pageIndex = 0, scrimAlpha = 0.18f)
                        DashboardScreen(
                            viewModel = dashboardViewModel,
                            prayerViewModel = prayerViewModel,
                            onNavigateToScanner = {},
                            onVaultLockRequest = { _, _, _ -> }
                        )
                    }
                }
            }
        }
        composeRule.mainClock.advanceTimeByFrame()
        composeRule.mainClock.advanceTimeBy(1_200)
        composeRule.waitForIdle()
        captureTaggedViewport(viewport.value.tag, "dashboard-before-320x560.png")

        viewport.value = DashboardViewport(
            widthDp = 360,
            heightDp = 640,
            tag = "dashboard-before-360x640"
        )
        composeRule.mainClock.advanceTimeByFrame()
        composeRule.mainClock.advanceTimeBy(1_200)
        composeRule.waitForIdle()
        captureTaggedViewport(viewport.value.tag, "dashboard-before-360x640.png")
    }

    private fun captureTaggedViewport(tag: String, fileName: String) {
        val bounds = composeRule.onNodeWithTag(tag, useUnmergedTree = true)
            .fetchSemanticsNode().boundsInRoot
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val host = composeRule.activity.findViewById<View>(android.R.id.content)
        val hostBitmap = Bitmap.createBitmap(host.width, host.height, Bitmap.Config.ARGB_8888)
        instrumentation.runOnMainSync { host.draw(Canvas(hostBitmap)) }

        val left = bounds.left.toInt().coerceAtLeast(0)
        val top = bounds.top.toInt().coerceAtLeast(0)
        val right = bounds.right.toInt().coerceAtMost(hostBitmap.width)
        val bottom = bounds.bottom.toInt().coerceAtMost(hostBitmap.height)
        assertTrue("Screenshot viewport has invalid bounds: $bounds", right > left && bottom > top)
        val bitmap = Bitmap.createBitmap(hostBitmap, left, top, right - left, bottom - top)

        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/A.SYRIA")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val resolver = instrumentation.targetContext.contentResolver
        val uri = checkNotNull(resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)) {
            "Could not create Dashboard screenshot $fileName."
        }
        try {
            val written = resolver.openOutputStream(uri)?.use { stream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            } ?: false
            assertTrue("Could not write Dashboard screenshot $fileName.", written)
            val published = ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) }
            assertTrue("Could not publish Dashboard screenshot $fileName.", resolver.update(uri, published, null, null) == 1)
        } catch (error: Throwable) {
            resolver.delete(uri, null, null)
            throw error
        }
    }
}

private data class DashboardViewport(
    val widthDp: Int,
    val heightDp: Int,
    val tag: String
)
