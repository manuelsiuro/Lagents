package com.msa.lagents.data.local.memory

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryDao {
    @Query("SELECT * FROM memories WHERE archivedAtMillis IS NULL ORDER BY updatedAtMillis DESC")
    fun observeActiveMemories(): Flow<List<MemoryEntity>>

    @Query("SELECT * FROM memories WHERE reviewStatus = :reviewStatus AND archivedAtMillis IS NULL ORDER BY updatedAtMillis DESC")
    fun observeMemoriesByReviewStatus(reviewStatus: String): Flow<List<MemoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMemory(memory: MemoryEntity)

    @Query("UPDATE memories SET reviewStatus = :reviewStatus, updatedAtMillis = :updatedAtMillis WHERE id = :id")
    suspend fun setReviewStatus(
        id: String,
        reviewStatus: String,
        updatedAtMillis: Long,
    )

    @Query("UPDATE memories SET archivedAtMillis = :archivedAtMillis, updatedAtMillis = :archivedAtMillis WHERE id = :id")
    suspend fun archiveMemory(id: String, archivedAtMillis: Long)

    @Query("DELETE FROM memories WHERE id = :id")
    suspend fun deleteMemory(id: String)
}
