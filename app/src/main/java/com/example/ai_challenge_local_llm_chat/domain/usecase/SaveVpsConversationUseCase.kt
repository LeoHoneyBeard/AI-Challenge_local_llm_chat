package com.example.ai_challenge_local_llm_chat.domain.usecase

import com.example.ai_challenge_local_llm_chat.domain.repository.VpsConversationRepository

class SaveVpsConversationUseCase(
    private val repository: VpsConversationRepository
) {
    suspend operator fun invoke(conversationId: String) {
        repository.saveConversation(conversationId)
    }
}
