package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inventor_ideas")
data class InventorIdea(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val encryptedTitle: String,
    val encryptedCategory: String,
    val encryptedOriginalIdea: String,
    val encryptedGeminiBlueprint: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun getDecryptedTitle(): String = SovereigntyCipher.decrypt(encryptedTitle)
    fun getDecryptedCategory(): String = SovereigntyCipher.decrypt(encryptedCategory)
    fun getDecryptedOriginalIdea(): String = SovereigntyCipher.decrypt(encryptedOriginalIdea)
    fun getDecryptedGeminiBlueprint(): String = SovereigntyCipher.decrypt(encryptedGeminiBlueprint)

    companion object {
        fun createEncrypted(
            id: Int = 0,
            title: String,
            category: String,
            originalIdea: String,
            geminiBlueprint: String,
            timestamp: Long = System.currentTimeMillis()
        ): InventorIdea {
            return InventorIdea(
                id = id,
                encryptedTitle = SovereigntyCipher.encrypt(title),
                encryptedCategory = SovereigntyCipher.encrypt(category),
                encryptedOriginalIdea = SovereigntyCipher.encrypt(originalIdea),
                encryptedGeminiBlueprint = SovereigntyCipher.encrypt(geminiBlueprint),
                timestamp = timestamp
            )
        }
    }
}
