package com.example

import androidx.biometric.BiometricManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BiometricCompatibilityTest {
    @Test
    fun android10UsesBiometricOnlyAuthenticatorSet() {
        assertEquals(BiometricManager.Authenticators.BIOMETRIC_STRONG, BiometricCompatibility.allowedAuthenticators(29))
        assertTrue(BiometricCompatibility.usesLegacyCredentialFallback(29))
    }

    @Test
    fun android9UsesBiometricOnlyAuthenticatorSet() {
        assertEquals(BiometricManager.Authenticators.BIOMETRIC_STRONG, BiometricCompatibility.allowedAuthenticators(28))
        assertTrue(BiometricCompatibility.usesLegacyCredentialFallback(28))
    }

    @Test
    fun android11AndNewerCanUseBiometricOrDeviceCredential() {
        assertEquals(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL,
            BiometricCompatibility.allowedAuthenticators(30)
        )
        assertFalse(BiometricCompatibility.usesLegacyCredentialFallback(30))
    }
}