package com.example.data.database

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Device-bound authenticated encryption for the local vault.
 * New values use AES-256-GCM and Android Keystore-managed keys. The previous
 * AES transformation is retained only to migrate existing database records.
 */
object SovereigntyCipher {
    private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
    private const val VAULT_KEY_ALIAS = "asyria.vault.aes.gcm.v2"
    private const val API_KEY_ALIAS = "asyria.api-key.aes.gcm.v1"
    private const val VAULT_PREFIX = "vault:v2:"
    private const val SECRET_PREFIX = "secret:v1:"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val LEGACY_TRANSFORMATION = "AES"
    private const val IV_LENGTH_BYTES = 12
    private const val TAG_LENGTH_BITS = 128

    // Retained only to decrypt existing database rows during migration.
    private val LEGACY_KEY_BYTES = byteArrayOf(
        0x53, 0x6f, 0x76, 0x65, 0x72, 0x65, 0x69, 0x67,
        0x6e, 0x41, 0x72, 0x63, 0x68, 0x69, 0x76, 0x65
    )

    fun encrypt(value: String?): String {
        if (value.isNullOrEmpty()) return ""
        return encryptWithAlias(value, VAULT_KEY_ALIAS, VAULT_PREFIX)
    }

    fun decrypt(value: String?): String {
        if (value.isNullOrEmpty()) return ""
        return if (value.startsWith(VAULT_PREFIX)) {
            decryptWithAlias(value.removePrefix(VAULT_PREFIX), VAULT_KEY_ALIAS)
        } else {
            decryptLegacy(value)
        }
    }

    fun encryptSecret(value: String?): String {
        if (value.isNullOrEmpty()) return ""
        return encryptWithAlias(value, API_KEY_ALIAS, SECRET_PREFIX)
    }

    /** Supports one-time migration from a previously stored plaintext API key. */
    fun decryptSecret(value: String?): String {
        if (value.isNullOrEmpty()) return ""
        return if (value.startsWith(SECRET_PREFIX)) {
            decryptWithAlias(value.removePrefix(SECRET_PREFIX), API_KEY_ALIAS)
        } else {
            value
        }
    }

    fun isSecretEncrypted(value: String?): Boolean =
        value?.startsWith(SECRET_PREFIX) == true

    fun isVaultEncrypted(value: String?): Boolean =
        value?.startsWith(VAULT_PREFIX) == true

    fun reencryptLegacyVaultValue(value: String?): String {
        if (value.isNullOrEmpty() || isVaultEncrypted(value)) return value.orEmpty()
        return encrypt(decryptLegacy(value))
    }

    private fun encryptWithAlias(value: String, alias: String, prefix: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey(alias))
        val encrypted = cipher.doFinal(value.toByteArray(Charsets.UTF_8))
        return prefix + Base64.encodeToString(cipher.iv + encrypted, Base64.NO_WRAP)
    }

    private fun decryptWithAlias(payload: String, alias: String): String {
        val decoded = Base64.decode(payload, Base64.NO_WRAP)
        require(decoded.size > IV_LENGTH_BYTES) { "Encrypted value is truncated." }
        val iv = decoded.copyOfRange(0, IV_LENGTH_BYTES)
        val encrypted = decoded.copyOfRange(IV_LENGTH_BYTES, decoded.size)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(
            Cipher.DECRYPT_MODE,
            getOrCreateKey(alias),
            GCMParameterSpec(TAG_LENGTH_BITS, iv)
        )
        return String(cipher.doFinal(encrypted), Charsets.UTF_8)
    }

    private fun decryptLegacy(value: String): String {
        val cipher = Cipher.getInstance(LEGACY_TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(LEGACY_KEY_BYTES, "AES"))
        return String(cipher.doFinal(Base64.decode(value, Base64.NO_WRAP)), Charsets.UTF_8)
    }

    @Synchronized
    private fun getOrCreateKey(alias: String): SecretKey {
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
        (keyStore.getKey(alias, null) as? SecretKey)?.let { return it }

        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER)
        generator.init(
            KeyGenParameterSpec.Builder(
                alias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .setRandomizedEncryptionRequired(true)
                .build()
        )
        return generator.generateKey()
    }
}
