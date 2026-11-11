package com.example

import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.ui.design.AyriaDesignSystemPreviewContent
import java.io.File
import java.io.FileOutputStream
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AyriaDesignPreviewScreenshotTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun captureDesignSystemPreview() {
        // The panorama has an intentional infinite shimmer; freeze the test clock for a deterministic capture.
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            AyriaDesignSystemPreviewContent()
        }
        composeRule.waitForIdle()

        val bitmap = composeRule.onRoot().captureToImage().asAndroidBitmap()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val directory = checkNotNull(context.getExternalFilesDir(null))
        val output = File(directory, "design-system-preview.png")

        FileOutputStream(output).use { stream ->
            assertTrue(
                "Preview screenshot could not be written",
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            )
        }

        assertTrue("Preview screenshot is empty", output.exists() && output.length() > 0L)
    }
}
