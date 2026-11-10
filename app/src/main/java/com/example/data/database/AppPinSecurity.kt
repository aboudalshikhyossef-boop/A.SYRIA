package com.example.data.database

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Local six-digit application PIN verifier.
 *
 * The PIN itself is never stored. A fresh 128-bit salt and PBKDF2-HMAC-SHA256
 * verifier are encrypted with a dedicated AES-256-GCM Android Keystore key.
 */
object AppPinSecurity {
    private const val VERSION = "1"
    private const val ITERATIONS = 210_000
    private const val SALT_BYTES = 16
    private const val HASH_BYTES = 32
    private const val KEY_BITS = HASH_BYTES * 8
    private const val KDF = "PBKDF2WithHmacSHA256"

    fun createEncryptedRecord(pin: String): String {
        require(isValidPin(pin)) { "App PIN must contain exactly six digits." }
        val salt = ByteArray(SALT_BYTES).also(SecureRandom()::nextBytes)
        val hash = derive(pin, salt, ITERATIONS)
        return try {
            val payload = listOf(
                VERSION,
                ITERATIONS.toString(),
                Base64.encodeToString(salt, Base64.NO_WRAP),
                Base64.encodeToString(hash, Base64.NO_WRAP)
            ).joinToString(":")
            SovereigntyCipher.encryptAppPinRecord(payload)
        } finally {
            hash.fill(0)
        }
    }

    fun verify(pin: String, encryptedRecord: String): Boolean {
        if (!isValidPin(pin) || encryptedRecord.isBlank()) return false
        var candidate = ByteArray(0)
        var expected = ByteArray(0)
        return try {
            val parts = SovereigntyCipher.decryptAppPinRecord(encryptedRecord).split(":")
            require(parts.size == 4 && parts[0] == VERSION) { "Invalid app PIN record." }
            val iterations = parts[1].toInt()
            require(iterations in 100_000..500_000) { "Invalid PIN KDF parameters." }
            val salt = Base64.decode(parts[2], Base64.NO_WRAP)
            expected = Base64.decode(parts[3], Base64.NO_WRAP)
            require(salt.size == SALT_BYTES && expected.size == HASH_BYTES) {
                "Invalid app PIN verifier."
            }
            candidate = derive(pin, salt, iterations)
            MessageDigest.isEqual(expected, candidate)
        } catch (_: Exception) {
            false
        } finally {
            candidate.fill(0)
            expected.fill(0)
        }
    }

    fun isValidPin(pin: String): Boolean =
        pin.length == 6 && pin.all { it in '0'..'9' }

    private fun derive(pin: String, salt: ByteArray, iterations: Int): ByteArray {
        val password = pin.toCharArray()
        val spec = PBEKeySpec(password, salt, iterations, KEY_BITS)
        return try {
            SecretKeyFactory.getInstance(KDF).generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
            password.fill('\u0000')
        }
    }
}
