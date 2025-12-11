package com.example.ai_challenge_local_llm_chat.presentation.chat.vps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai_challenge_local_llm_chat.data.local.preferences.VpsSettingsStorage
import com.example.ai_challenge_local_llm_chat.data.remote.RemoteChatRequest
import com.example.ai_challenge_local_llm_chat.data.remote.VpsLlmClient
import com.example.ai_challenge_local_llm_chat.domain.model.MessageRole
import com.example.ai_challenge_local_llm_chat.domain.model.VpsChatSettings
import com.example.ai_challenge_local_llm_chat.domain.usecase.SaveVpsConversationUseCase
import com.example.ai_challenge_local_llm_chat.presentation.chat.ChatMessageUi
import com.example.ai_challenge_local_llm_chat.presentation.chat.toUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class VpsChatViewModel(
    private val remoteClient: VpsLlmClient,
    private val saveConversationUseCase: SaveVpsConversationUseCase,
    private val settingsStorage: VpsSettingsStorage,
    private val initialConversationId: String?
) : ViewModel() {

    private val _uiState = MutableStateFlow(VpsChatUiState(conversationId = initialConversationId))
    val uiState: StateFlow<VpsChatUiState> = _uiState.asStateFlow()

    private var nextMessageId: Long = 0L
    private var appliedSettings: VpsChatSettings = settingsStorage.load()

    init {
        restoreSettingsUi()
        initialConversationId?.let { loadHistory(it) }
        fetchModels()
    }

    fun onInputChanged(input: String) {
        _uiState.update { state ->
            state.copy(chatState = state.chatState.copy(inputText = input))
        }
    }

    fun onSendMessage() {
        val currentState = _uiState.value
        val text = currentState.chatState.inputText.trim()
        if (text.isEmpty() || currentState.chatState.isGenerating || currentState.isHistoryLoading) return

        val userMessage = createUiMessage(MessageRole.USER, text)
        _uiState.update { state ->
            state.copy(
                chatState = state.chatState.copy(
                    messages = state.chatState.messages + userMessage,
                    inputText = "",
                    isGenerating = true,
                    streamingResponse = "",
                    errorMessage = null
                )
            )
        }

        viewModelScope.launch {
            try {
                val response = remoteClient.sendMessage(
                    RemoteChatRequest(
                        prompt = text,
                        conversationId = _uiState.value.conversationId,
                        model = appliedSettings.modelId,
                        temperature = appliedSettings.temperature,
                        maxTokens = appliedSettings.maxTokens
                    )
                )
                saveConversationUseCase(response.conversationId)
                appendAssistantReply(response.reply, response.conversationId)
            } catch (throwable: Throwable) {
                _uiState.update { state ->
                    state.copy(
                        chatState = state.chatState.copy(
                            isGenerating = false,
                            streamingResponse = null,
                            errorMessage = throwable.message ?: "Не удалось отправить запрос"
                        )
                    )
                }
            }
        }
    }

    fun onOpenSettings() {
        restoreSettingsUi()
        _uiState.update { it.copy(isSettingsVisible = true) }
    }

    fun onDismissSettings() {
        restoreSettingsUi()
        _uiState.update { it.copy(isSettingsVisible = false) }
    }

    fun onTemperatureChanged(value: Float) {
        val normalized = ((value * 10).roundToInt() / 10f).coerceIn(0.1f, 1f)
        _uiState.update { state ->
            state.copy(settingsState = state.settingsState.copy(temperature = normalized))
        }
    }

    fun onMaxTokensChanged(value: String) {
        val filtered = value.filter { it.isDigit() }
        _uiState.update { state ->
            state.copy(settingsState = state.settingsState.copy(maxTokensInput = filtered))
        }
    }

    fun onModelSelected(modelId: String) {
        _uiState.update { state ->
            state.copy(settingsState = state.settingsState.copy(selectedModelId = modelId))
        }
    }

    fun onApplySettings() {
        val settingsState = _uiState.value.settingsState
        val parsedMaxTokens = settingsState.maxTokensInput.toIntOrNull()?.takeIf { it > 0 }
        val newSettings = VpsChatSettings(
            modelId = settingsState.selectedModelId,
            temperature = settingsState.temperature,
            maxTokens = parsedMaxTokens
        )
        appliedSettings = newSettings
        settingsStorage.save(newSettings)
        restoreSettingsUi()
        _uiState.update { it.copy(isSettingsVisible = false) }
    }

    private fun fetchModels(force: Boolean = false) {
        if (!force && _uiState.value.settingsState.availableModels.isNotEmpty()) return
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(settingsState = state.settingsState.copy(isModelsLoading = true))
            }
            runCatching { remoteClient.fetchModels() }
                .onSuccess { models ->
                    val selectedId = when {
                        models.isEmpty() -> appliedSettings.modelId
                        models.any { it.id == appliedSettings.modelId } -> appliedSettings.modelId
                        else -> models.first().id.also {
                            appliedSettings = appliedSettings.copy(modelId = it)
                            settingsStorage.save(appliedSettings)
                        }
                    }
                    _uiState.update { state ->
                        state.copy(
                            settingsState = state.settingsState.copy(
                                availableModels = models,
                                selectedModelId = selectedId,
                                isModelsLoading = false
                            )
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update { state ->
                        state.copy(settingsState = state.settingsState.copy(isModelsLoading = false))
                    }
                    postError(throwable.message ?: "Не удалось загрузить модели")
                }
        }
    }

    private fun loadHistory(conversationId: String) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    conversationId = conversationId,
                    isHistoryLoading = true,
                    chatState = state.chatState.copy(errorMessage = null, streamingResponse = null)
                )
            }
            try {
                val messages = remoteClient.fetchHistory(conversationId)
                nextMessageId = (messages.maxOfOrNull { it.id } ?: -1L) + 1L
                _uiState.update { state ->
                    state.copy(
                        conversationId = conversationId,
                        isHistoryLoading = false,
                        chatState = state.chatState.copy(
                            messages = messages.map { it.toUiModel() },
                            errorMessage = null
                        )
                    )
                }
                saveConversationUseCase(conversationId)
            } catch (throwable: Throwable) {
                _uiState.update { state ->
                    state.copy(
                        isHistoryLoading = false,
                        chatState = state.chatState.copy(
                            messages = emptyList(),
                            errorMessage = throwable.message ?: "Не удалось загрузить историю"
                        )
                    )
                }
            }
        }
    }

    private fun appendAssistantReply(reply: String, conversationId: String) {
        val assistantMessage = createUiMessage(MessageRole.MODEL, reply)
        _uiState.update { state ->
            state.copy(
                conversationId = conversationId,
                chatState = state.chatState.copy(
                    isGenerating = false,
                    streamingResponse = null,
                    messages = state.chatState.messages + assistantMessage,
                    errorMessage = null
                )
            )
        }
    }

    private fun restoreSettingsUi() {
        val persisted = appliedSettings
        _uiState.update { state ->
            state.copy(
                settingsState = state.settingsState.copy(
                    temperature = persisted.temperature,
                    maxTokensInput = persisted.maxTokens?.toString() ?: "",
                    selectedModelId = persisted.modelId ?: state.settingsState.selectedModelId
                )
            )
        }
    }

    private fun postError(message: String) {
        _uiState.update { state ->
            state.copy(chatState = state.chatState.copy(errorMessage = message))
        }
    }

    private fun createUiMessage(role: MessageRole, text: String): ChatMessageUi = ChatMessageUi(
        id = nextMessageId++,
        role = role,
        text = text,
        timestamp = System.currentTimeMillis()
    )
}



