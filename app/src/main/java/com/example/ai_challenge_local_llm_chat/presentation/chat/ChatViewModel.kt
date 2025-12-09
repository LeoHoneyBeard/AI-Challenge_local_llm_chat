package com.example.ai_challenge_local_llm_chat.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai_challenge_local_llm_chat.domain.model.ChatMessage
import com.example.ai_challenge_local_llm_chat.domain.model.MessageRole
import com.example.ai_challenge_local_llm_chat.domain.usecase.ClearChatHistoryUseCase
import com.example.ai_challenge_local_llm_chat.domain.usecase.EnsureModelReadyUseCase
import com.example.ai_challenge_local_llm_chat.domain.usecase.GenerateModelResponseUseCase
import com.example.ai_challenge_local_llm_chat.domain.usecase.ObserveChatMessagesUseCase
import com.example.ai_challenge_local_llm_chat.domain.usecase.ShutdownLlmUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatViewModel(
    private val observeChatMessagesUseCase: ObserveChatMessagesUseCase,
    private val generateModelResponseUseCase: GenerateModelResponseUseCase,
    private val clearChatHistoryUseCase: ClearChatHistoryUseCase,
    private val ensureModelReadyUseCase: EnsureModelReadyUseCase,
    private val shutdownLlmUseCase: ShutdownLlmUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val cleanupScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

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
        viewModelScope.launch {
            runCatching { ensureModelReadyUseCase() }
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
            generateModelResponseUseCase(message)
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
                        GenerateModelResponseUseCase.Event.Started -> Unit
                        is GenerateModelResponseUseCase.Event.Chunk -> _uiState.update {
                            it.copy(streamingResponse = event.content)
                        }
                        is GenerateModelResponseUseCase.Event.Success -> _uiState.update {
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
        cleanupScope.launch {
            shutdownLlmUseCase()
        }.invokeOnCompletion {
            cleanupScope.cancel()
        }
    }

    private fun ChatMessage.toUiModel(): ChatMessageUi = ChatMessageUi(
        id = id,
        role = role,
        text = content,
        timestamp = timestamp
    )
}
