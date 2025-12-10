package com.example.ai_challenge_local_llm_chat.data.repository

import com.example.ai_challenge_local_llm_chat.data.local.db.ChatMessageDao
import com.example.ai_challenge_local_llm_chat.data.mapper.toDomain
import com.example.ai_challenge_local_llm_chat.data.mapper.toEntity
import com.example.ai_challenge_local_llm_chat.domain.model.ChatMessage
import com.example.ai_challenge_local_llm_chat.domain.model.ChatSessionType
import com.example.ai_challenge_local_llm_chat.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ChatRepositoryImpl(
    private val dao: ChatMessageDao,
    private val sessionType: ChatSessionType
) : ChatRepository {

    private val sessionKey = sessionType.name

    override fun observeMessages(): Flow<List<ChatMessage>> =
        dao.observeMessages(sessionKey).map { entities -> entities.map { it.toDomain() } }

    override suspend fun getMessagesSnapshot(): List<ChatMessage> =
        dao.getMessages(sessionKey).map { it.toDomain() }

    override suspend fun insertMessage(message: ChatMessage) {
        dao.insert(message.toEntity(sessionKey))
    }

    override suspend fun insertMessages(messages: List<ChatMessage>) {
        dao.insert(messages.map { it.toEntity(sessionKey) })
    }

    override suspend fun clear() {
        dao.clear(sessionKey)
    }
}
