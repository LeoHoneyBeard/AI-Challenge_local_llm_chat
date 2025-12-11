package com.example.ai_challenge_local_llm_chat.presentation.chat.vps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class VpsChatViewModelFactory(
    private val dependencies: VpsChatDependencies,
    private val conversationId: String?
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VpsChatViewModel::class.java)) {
            return VpsChatViewModel(
                remoteClient = dependencies.remoteClient,
                saveConversationUseCase = dependencies.saveConversationUseCase,
                settingsStorage = dependencies.settingsStorage,
                initialConversationId = conversationId
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class ")
    }
}
