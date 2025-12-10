package com.example.ai_challenge_local_llm_chat.domain.usecase

import kotlinx.coroutines.flow.Flow

sealed interface ChatResponseEvent {
    data object Started : ChatResponseEvent
    data class Chunk(val content: String) : ChatResponseEvent
    data class Success(val fullContent: String) : ChatResponseEvent
}

fun interface ChatResponseGenerator {
    operator fun invoke(userInput: String): Flow<ChatResponseEvent>
}
