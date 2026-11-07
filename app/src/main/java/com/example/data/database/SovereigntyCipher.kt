package com.example.data.database

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

object SovereigntyCipher {
    private const val ALGORITHM = "AES"
    
    // A 128-bit master core symmetric key
    private val KEY_BYTES = byteArrayOf(
        0x53, 0x6f, 0x76, 0x65, 0x72, 0x65, 0x69, 0x67, // 'Sovereig'
        0x6e, 0x41, 0x72, 0x63, 0x68, 0x69, 0x76, 0x65  // 'nArchive'
    )
    private val secretKey = SecretKeySpec(KEY_BYTES, ALGORITHM)

    fun encrypt(value: String?): String {
        if (value.isNullOrEmpty()) return ""
        return try {
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val encrypted = cipher.doFinal(value.toByteArray(Charsets.UTF_8))
            Base64.encodeToString(encrypted, Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    fun decrypt(encryptedValue: String?): String {
        if (encryptedValue.isNullOrEmpty()) return ""
        return try {
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, secretKey)
            val decoded = Base64.decode(encryptedValue, Base64.NO_WRAP)
            val decrypted = cipher.doFinal(decoded)
            String(decrypted, Charsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }
}
