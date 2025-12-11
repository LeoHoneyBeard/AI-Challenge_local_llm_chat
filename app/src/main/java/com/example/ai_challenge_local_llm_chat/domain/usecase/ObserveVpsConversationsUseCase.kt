package com.example.ai_challenge_local_llm_chat.domain.usecase

import com.example.ai_challenge_local_llm_chat.domain.model.VpsConversation
import com.example.ai_challenge_local_llm_chat.domain.repository.VpsConversationRepository
import kotlinx.coroutines.flow.Flow

class ObserveVpsConversationsUseCase(
    private val repository: VpsConversationRepository
) {
    operator fun invoke(): Flow<List<VpsConversation>> = repository.observeConversations()
}
