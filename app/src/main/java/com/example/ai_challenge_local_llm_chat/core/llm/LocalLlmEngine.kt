package com.example.ai_challenge_local_llm_chat.core.llm

import com.example.ai_challenge_local_llm_chat.domain.model.ChatMessage
import kotlinx.coroutines.flow.Flow

interface LocalLlmEngine {
    suspend fun warmUp()
    fun streamResponse(messages: List<ChatMessage>): Flow<String>
    suspend fun shutdown()
}
