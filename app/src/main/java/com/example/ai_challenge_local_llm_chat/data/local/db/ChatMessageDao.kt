package com.example.ai_challenge_local_llm_chat.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages WHERE chat_type = :chatType ORDER BY timestamp ASC")
    fun observeMessages(chatType: String): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE chat_type = :chatType ORDER BY timestamp ASC")
    suspend fun getMessages(chatType: String): List<ChatMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: ChatMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(messages: List<ChatMessageEntity>)

    @Query("DELETE FROM chat_messages WHERE chat_type = :chatType")
    suspend fun clear(chatType: String)
}
