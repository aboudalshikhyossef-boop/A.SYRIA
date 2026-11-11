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
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
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
}
