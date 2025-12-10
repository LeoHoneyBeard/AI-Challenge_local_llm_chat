package com.example.ai_challenge_local_llm_chat.core.remote

import com.example.ai_challenge_local_llm_chat.domain.model.ChatMessage

interface RemoteLlmClient {
    suspend fun requestCompletion(messages: List<ChatMessage>): String
}
