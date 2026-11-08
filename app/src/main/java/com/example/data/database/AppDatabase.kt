package com.example.data.database

import android.content.ContentValues
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.chat.ChatMessage
import com.example.data.chat.ChatMessageDao

@Database(
    entities = [InventorIdea::class, ChatMessage::class, VaultItem::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun inventorIdeaDao(): InventorIdeaDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun vaultItemDao(): VaultItemDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS chat_messages (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "role TEXT NOT NULL, " +
                        "content TEXT NOT NULL, " +
                        "timestamp INTEGER NOT NULL, " +
                        "sessionId TEXT NOT NULL)"
                )
            }
        }

        /** Re-encrypt legacy vault fields. Any failure aborts the DB migration. */
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                data class LegacyIdea(
                    val id: Int,
                    val title: String,
                    val category: String,
                    val originalIdea: String,
                    val blueprint: String
                )

                val rows = mutableListOf<LegacyIdea>()
                database.query(
                    "SELECT id, encryptedTitle, encryptedCategory, " +
                        "encryptedOriginalIdea, encryptedGeminiBlueprint FROM inventor_ideas"
                ).use { cursor ->
                    while (cursor.moveToNext()) {
                        rows += LegacyIdea(
                            id = cursor.getInt(0),
                            title = cursor.getString(1),
                            category = cursor.getString(2),
                            originalIdea = cursor.getString(3),
                            blueprint = cursor.getString(4)
                        )
                    }
                }

                rows.forEach { row ->
                    val values = ContentValues().apply {
                        put("encryptedTitle", SovereigntyCipher.reencryptLegacyVaultValue(row.title))
                        put("encryptedCategory", SovereigntyCipher.reencryptLegacyVaultValue(row.category))
                        put("encryptedOriginalIdea", SovereigntyCipher.reencryptLegacyVaultValue(row.originalIdea))
                        put("encryptedGeminiBlueprint", SovereigntyCipher.reencryptLegacyVaultValue(row.blueprint))
                    }
                    check(
                        database.update("inventor_ideas", values, "id = ?", arrayOf(row.id.toString())) == 1
                    ) { "Failed to migrate encrypted vault record ${row.id}." }
                }
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS vault_items (" +
                        "id TEXT NOT NULL PRIMARY KEY, " +
                        "encryptedDisplayName TEXT NOT NULL, " +
                        "encryptedMimeType TEXT NOT NULL, " +
                        "storedFileName TEXT NOT NULL, " +
                        "sizeBytes INTEGER NOT NULL, " +
                        "createdAt INTEGER NOT NULL)"
                )
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sovereign_archive_db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
