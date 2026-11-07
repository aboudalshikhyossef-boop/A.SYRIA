package com.example.data.database

  import android.content.Context
  import androidx.room.Database
  import androidx.room.Room
  import androidx.room.RoomDatabase
  import androidx.room.migration.Migration
  import androidx.sqlite.db.SupportSQLiteDatabase
  import com.example.data.chat.ChatMessage
  import com.example.data.chat.ChatMessageDao

  @Database(
      entities = [InventorIdea::class, ChatMessage::class],
      version = 2,
      exportSchema = false
  )
  abstract class AppDatabase : RoomDatabase() {

      abstract fun inventorIdeaDao(): InventorIdeaDao
      abstract fun chatMessageDao(): ChatMessageDao

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

          @Volatile
          private var INSTANCE: AppDatabase? = null

          fun getDatabase(context: Context): AppDatabase {
              return INSTANCE ?: synchronized(this) {
                  Room.databaseBuilder(
                      context.applicationContext,
                      AppDatabase::class.java,
                      "sovereign_archive_db"
                  )
                  .addMigrations(MIGRATION_1_2)
                  .build()
                  .also { INSTANCE = it }
              }
          }
      }
  }
  