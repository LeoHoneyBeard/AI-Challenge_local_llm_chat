package com.example.ai_challenge_local_llm_chat.presentation.chat.vps

data class VpsConversationListUiState(
    val conversations: List<VpsConversationItem> = emptyList()
)

data class VpsConversationItem(
    val conversationId: String,
    val subtitle: String
)
