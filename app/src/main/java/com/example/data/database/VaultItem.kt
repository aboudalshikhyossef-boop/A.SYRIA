package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Metadata for an encrypted file stored only inside the app's private files directory. */
@Entity(tableName = "vault_items")
data class VaultItem(
    @PrimaryKey val id: String,
    val encryptedDisplayName: String,
    val encryptedMimeType: String,
    /** Random opaque filename; never derived from the original filename. */
    val storedFileName: String,
    val sizeBytes: Long,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun getDisplayName(): String = SovereigntyCipher.decrypt(encryptedDisplayName)
    fun getMimeType(): String = SovereigntyCipher.decrypt(encryptedMimeType)
}
