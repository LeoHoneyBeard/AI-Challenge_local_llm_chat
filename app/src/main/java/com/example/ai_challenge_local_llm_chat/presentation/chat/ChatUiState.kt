package com.example.ai_challenge_local_llm_chat.presentation.chat

data class ChatUiState(
    val messages: List<ChatMessageUi> = emptyList(),
    val inputText: String = "",
    val isGenerating: Boolean = false,
    val streamingResponse: String? = null,
    val errorMessage: String? = null
)
