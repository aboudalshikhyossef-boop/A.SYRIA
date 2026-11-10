package com.example

import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BiometricPromptCompatibilityInstrumentedTest {

    @Test
    fun promptConfigurationBuildsOnTheCurrentAndroidVersion() {
        val apiLevel = Build.VERSION.SDK_INT
        val authenticators = BiometricCompatibility.allowedAuthenticators(apiLevel)

        val builder = BiometricPrompt.PromptInfo.Builder()
            .setTitle("A.SYRIA")
            .setSubtitle("Compatibility check")
            .setAllowedAuthenticators(authenticators)

        if (BiometricCompatibility.usesLegacyCredentialFallback(apiLevel)) {
            builder.setNegativeButtonText("Use device PIN")
        }

        assertNotNull(builder.build())

        // This call must be safe on every API level supported by the app.
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        BiometricManager.from(context).canAuthenticate(authenticators)
    }
}