package com.example

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.content.ContentValues
import android.provider.MediaStore
import android.os.Environment
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.assertIsSelected
import com.example.AppBottomNavBar
import com.example.ui.brand.AyriaPanoramaBackdrop
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.ui.design.AyriaDesignSystemPreviewContent
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.Assume.assumeTrue
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AyriaDesignPreviewScreenshotTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun captureDesignSystemPreview() {
        assumeTrue("The captured Preview artifact is generated on API 37.", Build.VERSION.SDK_INT >= 37)
        // The panorama has an intentional infinite shimmer; freeze the test clock for a deterministic capture.
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            AyriaDesignSystemPreviewContent()
        }
        // The clock is frozen to stop the panorama shimmer, but Compose still needs
        // one frame to perform its first measure/layout/draw pass.
        composeRule.mainClock.advanceTimeByFrame()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("نظام التصميم").assertIsDisplayed()

        // Draw the actual Compose host view into a bitmap. This avoids a system screenshot
        // of the underlying window background and does not depend on PixelCopy.
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val composeHost = composeRule.activity.findViewById<View>(android.R.id.content)
        val width = composeHost.width
        val height = composeHost.height
        assertTrue("Compose host has no laid-out size ($width x $height)", width > 0 && height > 0)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        instrumentation.runOnMainSync {
            composeHost.draw(Canvas(bitmap))
        }
        val context = instrumentation.targetContext
        // MediaStore makes the file accessible to adb without depending on the app's private package directory.
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "design-system-preview.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(
                MediaStore.Images.Media.RELATIVE_PATH,
                Environment.DIRECTORY_PICTURES + "/A.SYRIA"
            )
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val resolver = context.contentResolver
        val uri = checkNotNull(
            resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        ) { "Could not create the Preview screenshot in MediaStore." }

        try {
            val wroteScreenshot = resolver.openOutputStream(uri)?.use { stream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            } ?: false
            assertTrue("Preview screenshot could not be written", wroteScreenshot)

            val published = ContentValues().apply {
                put(MediaStore.Images.Media.IS_PENDING, 0)
            }
            assertTrue(
                "Preview screenshot could not be published",
                resolver.update(uri, published, null, null) == 1
            )
        } catch (error: Throwable) {
            resolver.delete(uri, null, null)
            throw error
        }
    }

    @Test
    fun captureBottomNavigationStatesAndVerifyBackAndSavedSelection() {
        assumeTrue("Navigation emulator captures are generated on API 37.", Build.VERSION.SDK_INT >= 37)
        composeRule.mainClock.autoAdvance = false
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent { BottomNavigationAcceptanceHarness() }
        composeRule.mainClock.advanceTimeByFrame()
        composeRule.waitForIdle()

        val tabs = listOf(
            "home" to "الرئيسية",
            "academy" to "الأكاديمية",
            "chat" to "الدردشة",
            "library" to "المكتبة",
            "settings" to "الإعدادات"
        )
        tabs.forEach { (key, label) ->
            composeRule.onNodeWithContentDescription(label).performClick()
            composeRule.mainClock.advanceTimeBy(700)
            composeRule.waitForIdle()
            composeRule.onNodeWithContentDescription(label).assertIsSelected()
            captureComposeHostScreenshot("navigation-$key.png")
        }

        composeRule.onNodeWithContentDescription("فحص المصادر والحماية").performClick()
        composeRule.mainClock.advanceTimeBy(500)
        composeRule.waitForIdle()
        composeRule.onNodeWithText("فحص المصادر").assertIsDisplayed()
        captureComposeHostScreenshot("navigation-scanner.png")

        composeRule.onNodeWithText("إغلاق الفحص").performClick()
        composeRule.onNodeWithContentDescription("المكتبة").performClick()
        composeRule.mainClock.advanceTimeBy(700)
        composeRule.waitForIdle()
        // Simulate Android saved-instance-state restoration without replacing the
        // activity that owns the Compose test rule.
        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription("المكتبة").assertIsSelected()
        captureComposeHostScreenshot("navigation-state-restored.png")

        composeRule.activity.onBackPressedDispatcher.onBackPressed()
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription("الرئيسية").assertIsSelected()
        captureComposeHostScreenshot("navigation-back-home.png")
    }

    private fun captureComposeHostScreenshot(fileName: String) {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val composeHost = composeRule.activity.findViewById<View>(android.R.id.content)
        val width = composeHost.width
        val height = composeHost.height
        assertTrue("Compose host has no laid-out size ($width x $height)", width > 0 && height > 0)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        instrumentation.runOnMainSync { composeHost.draw(Canvas(bitmap)) }

        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/A.SYRIA")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val resolver = instrumentation.targetContext.contentResolver
        val uri = checkNotNull(
            resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        ) { "Could not create navigation screenshot $fileName." }

        try {
            val written = resolver.openOutputStream(uri)?.use { stream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            } ?: false
            assertTrue("Could not write navigation screenshot $fileName.", written)
            val published = ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) }
            assertTrue(
                "Could not publish navigation screenshot $fileName.",
                resolver.update(uri, published, null, null) == 1
            )
        } catch (error: Throwable) {
            resolver.delete(uri, null, null)
            throw error
        }
    }
}

@Composable
private fun BottomNavigationAcceptanceHarness() {
    var activeTab by rememberSaveable { mutableStateOf("home") }
    var scannerOpen by rememberSaveable { mutableStateOf(false) }
    BackHandler(enabled = scannerOpen || activeTab != "home") {
        if (scannerOpen) scannerOpen = false else activeTab = "home"
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(Modifier.fillMaxSize().background(Color(0xFF050A18))) {
            AyriaPanoramaBackdrop(pageIndex = 2, scrimAlpha = 0.18f)
            Column(
                modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth()
                    .statusBarsPadding().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("A.SYRIA", color = Color(0xFFB9E8FF), fontSize = 12.sp)
                Text(
                    if (scannerOpen) "فحص المصادر" else "شريط التنقل الزجاجي",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    if (scannerOpen) "SOURCE INSPECTOR" else "POLAR FROSTED GLASS",
                    color = Color(0xFF9DB5D4),
                    fontSize = 11.sp
                )
                if (scannerOpen) {
                    Text(
                        "إغلاق الفحص",
                        color = Color(0xFF16C8F5),
                        modifier = Modifier.clickable { scannerOpen = false }
                    )
                }
            }
            AppBottomNavBar(
                isArabic = true,
                activeTab = activeTab,
                onTabSelected = { activeTab = it },
                onOpenScanner = { scannerOpen = true },
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                    .navigationBarsPadding().padding(horizontal = 16.dp).padding(bottom = 16.dp)
            )
        }
    }
}
