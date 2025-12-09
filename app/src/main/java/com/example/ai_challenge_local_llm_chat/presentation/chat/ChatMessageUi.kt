package com.example.ai_challenge_local_llm_chat.presentation.chat

import com.example.ai_challenge_local_llm_chat.domain.model.MessageRole

data class ChatMessageUi(
    val id: Long,
    val role: MessageRole,
    val text: String,
    val timestamp: Long,
    val isStreaming: Boolean = false
)
