package com.example.ai_challenge_local_llm_chat.presentation.chat.vps

import com.example.ai_challenge_local_llm_chat.presentation.chat.ChatUiState

data class VpsChatUiState(
    val chatState: ChatUiState = ChatUiState(),
    val conversationId: String? = null,
    val isHistoryLoading: Boolean = false,
    val isSettingsVisible: Boolean = false,
    val settingsState: VpsSettingsUiState = VpsSettingsUiState()
)
