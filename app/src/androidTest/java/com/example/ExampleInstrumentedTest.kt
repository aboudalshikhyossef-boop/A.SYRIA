package com.example

import android.util.Base64
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.data.database.SovereigntyCipher
import java.io.ByteArrayOutputStream
import javax.crypto.Cipher
import javax.crypto.CipherOutputStream
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
        assertEquals("com.asyria.v4.preview", appContext.packageName)
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
    fun vaultFileStreamUsesAuthenticatedEncryption() {
        val original = ByteArray(256 * 1024) { index -> (index % 251).toByte() }
        val encryptor = SovereigntyCipher.newVaultFileEncryptCipher()
        val encryptedOutput = ByteArrayOutputStream()
        encryptedOutput.write(encryptor.iv)
        CipherOutputStream(encryptedOutput, encryptor).use { it.write(original) }

        val payload = encryptedOutput.toByteArray()
        val iv = payload.copyOfRange(0, 12)
        val ciphertext = payload.copyOfRange(12, payload.size)
        val decryptor = SovereigntyCipher.newVaultFileDecryptCipher(iv)
        val decryptedOutput = ByteArrayOutputStream()
        var offset = 0
        while (offset < ciphertext.size) {
            val count = minOf(4096, ciphertext.size - offset)
            decryptor.update(ciphertext, offset, count)?.let(decryptedOutput::write)
            offset += count
        }
        decryptor.doFinal().let(decryptedOutput::write)

        assertTrue(original.contentEquals(decryptedOutput.toByteArray()))
    }

    @Test
    fun vaultFileStreamRejectsTamperedCiphertext() {
        val encryptor = SovereigntyCipher.newVaultFileEncryptCipher()
        val encryptedOutput = ByteArrayOutputStream()
        encryptedOutput.write(encryptor.iv)
        CipherOutputStream(encryptedOutput, encryptor).use { it.write("private".toByteArray()) }

        val payload = encryptedOutput.toByteArray()
        val iv = payload.copyOfRange(0, 12)
        val ciphertext = payload.copyOfRange(12, payload.size)
        ciphertext[ciphertext.lastIndex] = (ciphertext.last().toInt() xor 0x01).toByte()

        var rejected = false
        try {
            val decryptor = SovereigntyCipher.newVaultFileDecryptCipher(iv)
            decryptor.update(ciphertext)
            decryptor.doFinal()
        } catch (_: Exception) {
            rejected = true
        }
        assertTrue("Modified ciphertext must fail GCM authentication", rejected)
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
