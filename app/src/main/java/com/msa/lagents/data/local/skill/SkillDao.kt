package com.msa.lagents.data.local.skill

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface SkillDao {
    @Query("SELECT * FROM skills WHERE archivedAtMillis IS NULL ORDER BY updatedAtMillis DESC")
    fun observeActiveSkills(): Flow<List<SkillEntity>>

    @Query("SELECT * FROM skills WHERE id = :id")
    fun observeSkill(id: String): Flow<SkillEntity?>

    @Query("SELECT * FROM skill_versions WHERE skillId = :skillId ORDER BY version DESC")
    fun observeSkillVersions(skillId: String): Flow<List<SkillVersionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSkill(skill: SkillEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSkillVersion(version: SkillVersionEntity)

    @Transaction
    suspend fun upsertSkillWithVersion(
        skill: SkillEntity,
        version: SkillVersionEntity,
    ) {
        upsertSkill(skill)
        upsertSkillVersion(version)
    }

    @Query("UPDATE skills SET archivedAtMillis = :archivedAtMillis, updatedAtMillis = :archivedAtMillis WHERE id = :id")
    suspend fun archiveSkill(id: String, archivedAtMillis: Long)

    @Query("UPDATE skills SET archivedAtMillis = NULL, updatedAtMillis = :updatedAtMillis WHERE id = :id")
    suspend fun restoreSkill(id: String, updatedAtMillis: Long)

    @Query("DELETE FROM skill_versions WHERE skillId = :skillId")
    suspend fun deleteSkillVersions(skillId: String)

    @Query("DELETE FROM skills WHERE id = :id")
    suspend fun deleteSkill(id: String)

    @Transaction
    suspend fun deleteSkillWithVersions(skillId: String) {
        deleteSkillVersions(skillId)
        deleteSkill(skillId)
    }
}
