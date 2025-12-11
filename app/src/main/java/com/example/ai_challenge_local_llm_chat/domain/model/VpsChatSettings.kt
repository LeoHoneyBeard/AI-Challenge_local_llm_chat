package com.example.ai_challenge_local_llm_chat.domain.model

data class VpsChatSettings(
    val modelId: String? = null,
    val temperature: Float = DEFAULT_TEMPERATURE,
    val maxTokens: Int? = null
) {
    companion object {
        const val DEFAULT_TEMPERATURE = 0.7f
    }
}
