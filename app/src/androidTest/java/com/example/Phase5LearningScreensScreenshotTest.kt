package com.example

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onAllNodes
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.fetchSemanticsNode
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.ui.screens.AcademyScreen
import com.example.ui.screens.CyberGlossaryScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.ResourcesScreen
import com.example.ui.viewmodel.DashboardViewModel
import com.example.ui.viewmodel.PrayerViewModel
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.math.roundToInt

@RunWith(AndroidJUnit4::class)
class Phase5LearningScreensScreenshotTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun capturePhase5ScreensAtTwoViewportSizes() {
        assumeTrue("Phase 5 comparison captures are generated on API 37.", Build.VERSION.SDK_INT >= 37)
        val viewModel = DashboardViewModel(composeRule.activity.application)
        val prayerViewModel = PrayerViewModel(composeRule.activity.application)
        val activeScreen = mutableStateOf("academy")
        val viewport = mutableStateOf(Phase5Viewport(360, 800))
        composeRule.mainClock.autoAdvance = false

        composeRule.setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f)) {
                Box(Modifier.fillMaxSize().background(Color(0xFF050B16))) {
                    Box(
                        Modifier
                            .requiredSize(viewport.value.width.dp, viewport.value.height.dp)
                            .testTag("phase5-viewport")
                    ) {
                        when (activeScreen.value) {
                            "academy" -> AcademyScreen(
                                viewModel = viewModel,
                                onNavigateToGlossary = { activeScreen.value = "glossary" },
                                onNavigateToChecklist = {}
                            )
                            "library" -> LibraryScreen(viewModel, prayerViewModel) {}
                            "glossary" -> CyberGlossaryScreen(viewModel) {}
                            "resources" -> ResourcesScreen(viewModel) {}
                        }
                    }
                }
            }
        }

        val screens = listOf("academy", "library", "glossary", "resources")
        val sizes = listOf(Phase5Viewport(360, 800), Phase5Viewport(600, 900))
        for (size in sizes) {
            viewport.value = size
            for (screen in screens) {
                activeScreen.value = screen
                composeRule.mainClock.advanceTimeBy(2200)
                composeRule.waitForIdle()
                if (screen == "library") {
                    composeRule.waitUntil(10_000) {
                        composeRule.onAllNodes(hasText("شرح الصدر وتيسير الأمر"))
                            .fetchSemanticsNodes().isNotEmpty()
                    }
                }
                val name = "phase5-before-$screen-${size.width}x${size.height}.png"
                captureComposeViewport(composeRule, name)
            }
        }
    }
}

private data class Phase5Viewport(val width: Int, val height: Int)

private fun captureComposeViewport(rule: ComposeContentTestRule, fileName: String) {
    val nodeBounds = rule.onNodeWithTag("phase5-viewport")
        .fetchSemanticsNode().boundsInRoot
    val instrumentation = InstrumentationRegistry.getInstrumentation()
    val host = rule.activity.findViewById<View>(android.R.id.content)
    val full = Bitmap.createBitmap(host.width, host.height, Bitmap.Config.ARGB_8888)
    instrumentation.runOnMainSync { host.draw(Canvas(full)) }
    val left = nodeBounds.left.roundToInt().coerceIn(0, full.width - 1)
    val top = nodeBounds.top.roundToInt().coerceIn(0, full.height - 1)
    val width = nodeBounds.width.roundToInt().coerceAtLeast(1).coerceAtMost(full.width - left)
    val height = nodeBounds.height.roundToInt().coerceAtLeast(1).coerceAtMost(full.height - top)
    val bitmap = Bitmap.createBitmap(full, left, top, width, height)

    val values = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
        put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/A.SYRIA")
        put(MediaStore.Images.Media.IS_PENDING, 1)
    }
    val resolver = instrumentation.targetContext.contentResolver
    val uri = checkNotNull(resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)) {
        "Could not create $fileName."
    }
    try {
        val written = resolver.openOutputStream(uri)?.use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        } ?: false
        assertTrue("Could not write $fileName.", written)
        val published = ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) }
        assertTrue("Could not publish $fileName.", resolver.update(uri, published, null, null) == 1)
    } catch (error: Throwable) {
        resolver.delete(uri, null, null)
        throw error
    }
}
