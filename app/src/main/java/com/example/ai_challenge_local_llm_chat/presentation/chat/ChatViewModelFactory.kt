package com.example.ai_challenge_local_llm_chat.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
class ChatViewModelFactory(
    private val dependencies: ChatFeatureDependencies
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            return ChatViewModel(
                dependencies.observeChatMessagesUseCase,
                dependencies.responseGenerator,
                dependencies.clearChatHistoryUseCase,
                dependencies.ensureModelReady,
                dependencies.shutdownLlm
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
