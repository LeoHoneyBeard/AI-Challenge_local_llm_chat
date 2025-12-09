package com.example.ai_challenge_local_llm_chat.core.llm

object LlmConfig {
    const val MODEL_FILE_NAME = "qwen2.5-coder-0.5b-instruct-q4_0.gguf"
    const val MODEL_ASSET_PATH = "models/$MODEL_FILE_NAME"
    const val SYSTEM_PROMPT = "You are a friendly assistant. Respond briefly, stay on topic, and keep using the conversation history for context."
}
