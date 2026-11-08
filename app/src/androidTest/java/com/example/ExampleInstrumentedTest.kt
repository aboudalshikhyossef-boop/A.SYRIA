package com.example

import android.util.Base64
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.data.database.SovereigntyCipher
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    @Test
    fun useAppContext() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.asyria.v4", appContext.packageName)
    }

    @Test
    fun vaultUsesRandomizedAuthenticatedEncryption() {
        val plainText = "A.SYRIA private vault entry"
        val first = SovereigntyCipher.encrypt(plainText)
        val second = SovereigntyCipher.encrypt(plainText)

        assertTrue(first.startsWith("vault:v2:"))
        assertNotEquals(first, second)
        assertEquals(plainText, SovereigntyCipher.decrypt(first))
        assertEquals(plainText, SovereigntyCipher.decrypt(second))
    }

    @Test
    fun apiCredentialUsesItsSeparateKeystoreKey() {
        val secret = "test-api-key"
        val encrypted = SovereigntyCipher.encryptSecret(secret)

        assertTrue(encrypted.startsWith("secret:v1:"))
        assertNotEquals(secret, encrypted)
        assertEquals(secret, SovereigntyCipher.decryptSecret(encrypted))
        assertEquals(secret, SovereigntyCipher.decryptSecret(secret))
    }

    @Test
    fun legacyVaultCiphertextCanBeMigratedWithoutLosingPlaintext() {
        val legacyKey = byteArrayOf(
            0x53, 0x6f, 0x76, 0x65, 0x72, 0x65, 0x69, 0x67,
            0x6e, 0x41, 0x72, 0x63, 0x68, 0x69, 0x76, 0x65
        )
        val expected = "legacy vault record"
        val legacyCipher = Cipher.getInstance("AES").apply {
            init(Cipher.ENCRYPT_MODE, SecretKeySpec(legacyKey, "AES"))
        }
        val legacy = Base64.encodeToString(
            legacyCipher.doFinal(expected.toByteArray(Charsets.UTF_8)),
            Base64.NO_WRAP
        )

        val migrated = SovereigntyCipher.reencryptLegacyVaultValue(legacy)
        assertTrue(migrated.startsWith("vault:v2:"))
        assertEquals(expected, SovereigntyCipher.decrypt(migrated))
    }
}
