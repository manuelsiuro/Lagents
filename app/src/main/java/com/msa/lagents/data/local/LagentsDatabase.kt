package com.msa.lagents.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.msa.lagents.data.local.conversation.ConversationDao
import com.msa.lagents.data.local.conversation.ConversationEntity
import com.msa.lagents.data.local.conversation.MessageEntity

@Database(
    entities = [
        ConversationEntity::class,
        MessageEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class LagentsDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
}
