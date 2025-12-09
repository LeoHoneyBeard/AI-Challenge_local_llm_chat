package com.example.ai_challenge_local_llm_chat.domain.usecase

import com.example.ai_challenge_local_llm_chat.core.llm.LocalLlmEngine

class EnsureModelReadyUseCase(
    private val engine: LocalLlmEngine
) {
    suspend operator fun invoke() {
        engine.warmUp()
    }
}
