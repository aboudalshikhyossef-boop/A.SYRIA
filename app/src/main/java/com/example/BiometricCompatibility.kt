package com.example

import android.os.Build
import androidx.biometric.BiometricManager

/**
 * Selects authenticator combinations supported by the current Android version.
 * Android 9 and 10 do not support BIOMETRIC_STRONG | DEVICE_CREDENTIAL.
 */
internal object BiometricCompatibility {
    fun allowedAuthenticators(apiLevel: Int): Int =
        if (apiLevel >= Build.VERSION_CODES.R) {
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
        } else {
            BiometricManager.Authenticators.BIOMETRIC_STRONG
        }

    fun usesLegacyCredentialFallback(apiLevel: Int): Boolean =
        apiLevel < Build.VERSION_CODES.R
}