package com.example.data.database

import kotlinx.coroutines.flow.Flow

class InventorIdeaRepository(private val inventorIdeaDao: InventorIdeaDao) {
    
    val allIdeas: Flow<List<InventorIdea>> = inventorIdeaDao.getAllIdeas()

    suspend fun insertIdea(idea: InventorIdea) {
        inventorIdeaDao.insertIdea(idea)
    }

    suspend fun deleteIdeaById(id: Int) {
        inventorIdeaDao.deleteIdeaById(id)
    }

    suspend fun clearAll() {
        inventorIdeaDao.clearAll()
    }
}
