package com.example.ai_challenge_local_llm_chat.presentation.chat

import com.example.ai_challenge_local_llm_chat.domain.usecase.ChatResponseGenerator
import com.example.ai_challenge_local_llm_chat.domain.usecase.ClearChatHistoryUseCase
import com.example.ai_challenge_local_llm_chat.domain.usecase.ObserveChatMessagesUseCase

data class ChatFeatureDependencies(
    val observeChatMessagesUseCase: ObserveChatMessagesUseCase,
    val responseGenerator: ChatResponseGenerator,
    val clearChatHistoryUseCase: ClearChatHistoryUseCase,
    val ensureModelReady: (suspend () -> Unit)? = null,
    val shutdownLlm: (suspend () -> Unit)? = null
)
