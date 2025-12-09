package com.example.ai_challenge_local_llm_chat.domain.repository

import com.example.ai_challenge_local_llm_chat.domain.model.ChatMessage
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun observeMessages(): Flow<List<ChatMessage>>
    suspend fun getMessagesSnapshot(): List<ChatMessage>
    suspend fun insertMessage(message: ChatMessage)
    suspend fun insertMessages(messages: List<ChatMessage>)
    suspend fun clear()
}
