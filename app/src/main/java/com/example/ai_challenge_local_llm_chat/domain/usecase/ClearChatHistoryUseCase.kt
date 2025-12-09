package com.example.ai_challenge_local_llm_chat.domain.usecase

import com.example.ai_challenge_local_llm_chat.domain.repository.ChatRepository

class ClearChatHistoryUseCase(
    private val repository: ChatRepository
) {
    suspend operator fun invoke() {
        repository.clear()
    }
}
