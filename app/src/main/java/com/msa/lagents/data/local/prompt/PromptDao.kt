package com.msa.lagents.data.local.prompt

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface PromptDao {
    @Query("SELECT * FROM prompts WHERE archivedAtMillis IS NULL ORDER BY updatedAtMillis DESC")
    fun observeActivePrompts(): Flow<List<PromptEntity>>

    @Query("SELECT * FROM prompts WHERE id = :id")
    fun observePrompt(id: String): Flow<PromptEntity?>

    @Query("SELECT * FROM prompt_versions WHERE promptId = :promptId ORDER BY version DESC")
    fun observePromptVersions(promptId: String): Flow<List<PromptVersionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPrompt(prompt: PromptEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPromptVersion(version: PromptVersionEntity)

    @Transaction
    suspend fun upsertPromptWithVersion(
        prompt: PromptEntity,
        version: PromptVersionEntity,
    ) {
        upsertPrompt(prompt)
        upsertPromptVersion(version)
    }

    @Query("UPDATE prompts SET archivedAtMillis = :archivedAtMillis, updatedAtMillis = :archivedAtMillis WHERE id = :id")
    suspend fun archivePrompt(id: String, archivedAtMillis: Long)

    @Query("UPDATE prompts SET archivedAtMillis = NULL, updatedAtMillis = :updatedAtMillis WHERE id = :id")
    suspend fun restorePrompt(id: String, updatedAtMillis: Long)

    @Query("DELETE FROM prompt_versions WHERE promptId = :promptId")
    suspend fun deletePromptVersions(promptId: String)

    @Query("DELETE FROM prompts WHERE id = :id")
    suspend fun deletePrompt(id: String)

    @Transaction
    suspend fun deletePromptWithVersions(promptId: String) {
        deletePromptVersions(promptId)
        deletePrompt(promptId)
    }
}
