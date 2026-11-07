package com.example.data.chat

  import androidx.room.Dao
  import androidx.room.Insert
  import androidx.room.OnConflictStrategy
  import androidx.room.Query
  import kotlinx.coroutines.flow.Flow

  @Dao
  interface ChatMessageDao {

      @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
      fun observe(): Flow<List<ChatMessage>>

      @Insert(onConflict = OnConflictStrategy.REPLACE)
      suspend fun insert(message: ChatMessage): Long

      @Query("SELECT * FROM chat_messages ORDER BY timestamp DESC LIMIT :limit")
      suspend fun getRecent(limit: Int): List<ChatMessage>

      @Query("DELETE FROM chat_messages")
      suspend fun clear()
  }
  