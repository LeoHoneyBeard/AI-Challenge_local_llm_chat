package com.example.ai_challenge_local_llm_chat.domain.usecase

import com.example.ai_challenge_local_llm_chat.domain.model.ChatMessage
import com.example.ai_challenge_local_llm_chat.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow

class ObserveChatMessagesUseCase(
    private val repository: ChatRepository
) {
    operator fun invoke(): Flow<List<ChatMessage>> = repository.observeMessages()
}
