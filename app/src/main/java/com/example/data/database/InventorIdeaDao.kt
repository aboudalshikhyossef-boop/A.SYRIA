package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface InventorIdeaDao {
    @Query("SELECT * FROM inventor_ideas ORDER BY timestamp DESC")
    fun getAllIdeas(): Flow<List<InventorIdea>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIdea(idea: InventorIdea)

    @Query("DELETE FROM inventor_ideas WHERE id = :id")
    suspend fun deleteIdeaById(id: Int)

    @Query("DELETE FROM inventor_ideas")
    suspend fun clearAll()
}
