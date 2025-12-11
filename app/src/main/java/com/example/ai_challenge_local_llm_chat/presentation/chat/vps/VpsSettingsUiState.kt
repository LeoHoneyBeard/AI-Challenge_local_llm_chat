package com.example.ai_challenge_local_llm_chat.presentation.chat.vps

import com.example.ai_challenge_local_llm_chat.domain.model.VpsChatSettings
import com.example.ai_challenge_local_llm_chat.domain.model.VpsModelInfo

data class VpsSettingsUiState(
    val availableModels: List<VpsModelInfo> = emptyList(),
    val selectedModelId: String? = null,
    val temperature: Float = VpsChatSettings.DEFAULT_TEMPERATURE,
    val maxTokensInput: String = "",
    val isModelsLoading: Boolean = false
)
