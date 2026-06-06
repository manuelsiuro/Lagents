package com.msa.lagents.data.local

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import com.msa.lagents.data.local.agent.AgentDao
import com.msa.lagents.data.local.agent.AgentEntity
import com.msa.lagents.data.local.conversation.ConversationDao
import com.msa.lagents.data.local.conversation.ConversationEntity
import com.msa.lagents.data.local.conversation.MessageEntity
import com.msa.lagents.data.local.debug.DebugTraceDao
import com.msa.lagents.data.local.debug.DebugTraceEntity
import com.msa.lagents.data.local.knowledge.KnowledgeChunkEntity
import com.msa.lagents.data.local.knowledge.KnowledgeCollectionEntity
import com.msa.lagents.data.local.knowledge.KnowledgeDao
import com.msa.lagents.data.local.knowledge.KnowledgeDocumentEntity
import com.msa.lagents.data.local.voice.VoiceDao
import com.msa.lagents.data.local.voice.VoiceProfileEntity
import com.msa.lagents.data.local.workflow.WorkflowDao
import com.msa.lagents.data.local.workflow.WorkflowDefinitionEntity
import com.msa.lagents.data.local.workflow.WorkflowRunEntity
import com.msa.lagents.data.local.memory.MemoryDao
import com.msa.lagents.data.local.memory.MemoryEntity
import com.msa.lagents.data.local.prompt.PromptDao
import com.msa.lagents.data.local.prompt.PromptEntity
import com.msa.lagents.data.local.prompt.PromptVersionEntity
import com.msa.lagents.data.local.provider.ProviderConfigDao
import com.msa.lagents.data.local.provider.ProviderConfigEntity
import com.msa.lagents.data.local.skill.SkillDao
import com.msa.lagents.data.local.skill.SkillEntity
import com.msa.lagents.data.local.skill.SkillVersionEntity
import com.msa.lagents.data.local.tool.ToolConfigDao
import com.msa.lagents.data.local.tool.ToolConfigEntity

@Database(
    entities = [
        ConversationEntity::class,
        MessageEntity::class,
        AgentEntity::class,
        PromptEntity::class,
        PromptVersionEntity::class,
        SkillEntity::class,
        SkillVersionEntity::class,
        ToolConfigEntity::class,
        ProviderConfigEntity::class,
        MemoryEntity::class,
        DebugTraceEntity::class,
        KnowledgeCollectionEntity::class,
        KnowledgeDocumentEntity::class,
        KnowledgeChunkEntity::class,
        WorkflowDefinitionEntity::class,
        WorkflowRunEntity::class,
        VoiceProfileEntity::class,
    ],
    version = 6,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4),
        AutoMigration(from = 4, to = 5),
        AutoMigration(from = 5, to = 6),
    ],
)
abstract class LagentsDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
    abstract fun agentDao(): AgentDao
    abstract fun promptDao(): PromptDao
    abstract fun skillDao(): SkillDao
    abstract fun toolConfigDao(): ToolConfigDao
    abstract fun providerConfigDao(): ProviderConfigDao
    abstract fun memoryDao(): MemoryDao
    abstract fun debugTraceDao(): DebugTraceDao
    abstract fun knowledgeDao(): KnowledgeDao
    abstract fun workflowDao(): WorkflowDao
    abstract fun voiceDao(): VoiceDao
}
