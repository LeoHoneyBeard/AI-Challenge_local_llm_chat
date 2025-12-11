package com.example.ai_challenge_local_llm_chat.presentation.chat.vps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class VpsConversationListViewModelFactory(
    private val dependencies: VpsConversationDependencies
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VpsConversationListViewModel::class.java)) {
            return VpsConversationListViewModel(dependencies.observeConversationsUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class ")
    }
}
