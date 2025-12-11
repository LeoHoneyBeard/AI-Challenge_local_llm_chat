package com.example.ai_challenge_local_llm_chat.data.repository

import com.example.ai_challenge_local_llm_chat.data.local.db.VpsConversationDao
import com.example.ai_challenge_local_llm_chat.data.mapper.toDomain
import com.example.ai_challenge_local_llm_chat.data.mapper.toEntity
import com.example.ai_challenge_local_llm_chat.domain.model.VpsConversation
import com.example.ai_challenge_local_llm_chat.domain.repository.VpsConversationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class VpsConversationRepositoryImpl(
    private val dao: VpsConversationDao
) : VpsConversationRepository {

    override fun observeConversations(): Flow<List<VpsConversation>> =
        dao.observeConversations().map { items -> items.map { it.toDomain() } }

    override suspend fun saveConversation(conversationId: String) {
        val conversation = VpsConversation(
            conversationId = conversationId,
            updatedAt = System.currentTimeMillis()
        )
        dao.upsert(conversation.toEntity())
    }

    override suspend fun deleteConversation(conversationId: String) {
        dao.delete(conversationId)
    }

    override suspend fun clear() {
        dao.clear()
    }
}
