package com.example.ai_challenge_local_llm_chat.domain.model

data class ChatMessage(
    val id: Long = 0L,
    val role: MessageRole,
    val content: String,
    val timestamp: Long
)
