package com.example.ai_challenge_local_llm_chat.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai_challenge_local_llm_chat.domain.usecase.ChatResponseEvent
import com.example.ai_challenge_local_llm_chat.domain.usecase.ChatResponseGenerator
import com.example.ai_challenge_local_llm_chat.domain.usecase.ClearChatHistoryUseCase
import com.example.ai_challenge_local_llm_chat.domain.usecase.ObserveChatMessagesUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatViewModel(
    private val observeChatMessagesUseCase: ObserveChatMessagesUseCase,
    private val responseGenerator: ChatResponseGenerator,
    private val clearChatHistoryUseCase: ClearChatHistoryUseCase,
    private val ensureModelReadyAction: (suspend () -> Unit)?,
    private val shutdownLlmAction: (suspend () -> Unit)?
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val cleanupScope = shutdownLlmAction?.let { CoroutineScope(SupervisorJob() + Dispatchers.IO) }

    init {
        observeHistory()
        preloadModel()
    }

    private fun observeHistory() {
        viewModelScope.launch {
            observeChatMessagesUseCase()
                .collect { messages ->
                    _uiState.update { state ->
                        state.copy(messages = messages.map { it.toUiModel() })
                    }
                }
        }
    }

    private fun preloadModel() {
        val ensureAction = ensureModelReadyAction ?: return
        viewModelScope.launch {
            runCatching { ensureAction() }
                .onFailure { throwable ->
                    _uiState.update { state ->
                        state.copy(errorMessage = throwable.message)
                    }
                }
        }
    }

    fun onInputChanged(newValue: String) {
        _uiState.update { it.copy(inputText = newValue) }
    }

    fun onSendMessage() {
        val message = uiState.value.inputText.trim()
        if (message.isEmpty() || uiState.value.isGenerating) return

        viewModelScope.launch {
            responseGenerator(message)
                .onStart {
                    _uiState.update { it.copy(isGenerating = true, streamingResponse = "", inputText = "", errorMessage = null) }
                }
                .catch { throwable ->
                    _uiState.update {
                        it.copy(
                            isGenerating = false,
                            streamingResponse = null,
                            errorMessage = throwable.message ?: "Неизвестная ошибка"
                        )
                    }
                }
                .collect { event ->
                    when (event) {
                        ChatResponseEvent.Started -> Unit
                        is ChatResponseEvent.Chunk -> _uiState.update {
                            it.copy(streamingResponse = event.content)
                        }
                        is ChatResponseEvent.Success -> _uiState.update {
                            it.copy(
                                isGenerating = false,
                                streamingResponse = null,
                                errorMessage = null
                            )
                        }
                    }
                }
        }
    }

    fun onClearChat() {
        if (uiState.value.isGenerating) return
        viewModelScope.launch {
            clearChatHistoryUseCase()
            _uiState.update { it.copy(streamingResponse = null, errorMessage = null) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        val scope = cleanupScope ?: return
        val action = shutdownLlmAction ?: run {
            scope.cancel()
            return
        }
        scope.launch {
            action()
        }.invokeOnCompletion {
            scope.cancel()
        }
    }
}
