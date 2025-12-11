package com.example.ai_challenge_local_llm_chat.domain.repository

import com.example.ai_challenge_local_llm_chat.domain.model.VpsConversation
import kotlinx.coroutines.flow.Flow

interface VpsConversationRepository {
    fun observeConversations(): Flow<List<VpsConversation>>
    suspend fun saveConversation(conversationId: String)
    suspend fun deleteConversation(conversationId: String)
    suspend fun clear()
}
