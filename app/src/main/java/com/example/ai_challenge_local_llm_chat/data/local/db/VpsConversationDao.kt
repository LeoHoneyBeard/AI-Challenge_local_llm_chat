package com.example.ai_challenge_local_llm_chat.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface VpsConversationDao {
    @Query("SELECT * FROM vps_conversations ORDER BY updated_at DESC")
    fun observeConversations(): Flow<List<VpsConversationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: VpsConversationEntity)

    @Query("DELETE FROM vps_conversations WHERE conversation_id = :conversationId")
    suspend fun delete(conversationId: String)

    @Query("DELETE FROM vps_conversations")
    suspend fun clear()
}
