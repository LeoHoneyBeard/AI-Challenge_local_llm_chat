package com.example.ai_challenge_local_llm_chat.presentation.chat.vps

import com.example.ai_challenge_local_llm_chat.data.local.preferences.VpsSettingsStorage
import com.example.ai_challenge_local_llm_chat.data.remote.VpsLlmClient
import com.example.ai_challenge_local_llm_chat.domain.usecase.ObserveVpsConversationsUseCase
import com.example.ai_challenge_local_llm_chat.domain.usecase.SaveVpsConversationUseCase

data class VpsConversationDependencies(
    val observeConversationsUseCase: ObserveVpsConversationsUseCase
)

data class VpsChatDependencies(
    val remoteClient: VpsLlmClient,
    val saveConversationUseCase: SaveVpsConversationUseCase,
    val settingsStorage: VpsSettingsStorage
)
