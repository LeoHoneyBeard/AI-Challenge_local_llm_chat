package com.example.ai_challenge_local_llm_chat.presentation.chat.vps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai_challenge_local_llm_chat.domain.model.VpsConversation
import com.example.ai_challenge_local_llm_chat.domain.usecase.ObserveVpsConversationsUseCase
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VpsConversationListViewModel(
    observeUseCase: ObserveVpsConversationsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(VpsConversationListUiState())
    val uiState: StateFlow<VpsConversationListUiState> = _uiState.asStateFlow()

    private val formatter = DateTimeFormatter.ofPattern("dd MMM HH:mm")
        .withLocale(Locale.getDefault())
        .withZone(ZoneId.systemDefault())

    init {
        viewModelScope.launch {
            observeUseCase().collect { conversations ->
                _uiState.value = VpsConversationListUiState(
                    conversations = conversations.map { it.toUiItem() }
                )
            }
        }
    }

    private fun VpsConversation.toUiItem(): VpsConversationItem = VpsConversationItem(
        conversationId = conversationId,
        subtitle = formatter.format(Instant.ofEpochMilli(updatedAt))
    )
}
