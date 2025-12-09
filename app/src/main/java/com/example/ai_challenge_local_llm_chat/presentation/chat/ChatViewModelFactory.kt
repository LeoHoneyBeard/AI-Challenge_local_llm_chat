package com.example.ai_challenge_local_llm_chat.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.ai_challenge_local_llm_chat.domain.usecase.ClearChatHistoryUseCase
import com.example.ai_challenge_local_llm_chat.domain.usecase.EnsureModelReadyUseCase
import com.example.ai_challenge_local_llm_chat.domain.usecase.GenerateModelResponseUseCase
import com.example.ai_challenge_local_llm_chat.domain.usecase.ObserveChatMessagesUseCase
import com.example.ai_challenge_local_llm_chat.domain.usecase.ShutdownLlmUseCase

class ChatViewModelFactory(
    private val observeChatMessagesUseCase: ObserveChatMessagesUseCase,
    private val generateModelResponseUseCase: GenerateModelResponseUseCase,
    private val clearChatHistoryUseCase: ClearChatHistoryUseCase,
    private val ensureModelReadyUseCase: EnsureModelReadyUseCase,
    private val shutdownLlmUseCase: ShutdownLlmUseCase
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            return ChatViewModel(
                observeChatMessagesUseCase,
                generateModelResponseUseCase,
                clearChatHistoryUseCase,
                ensureModelReadyUseCase,
                shutdownLlmUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
