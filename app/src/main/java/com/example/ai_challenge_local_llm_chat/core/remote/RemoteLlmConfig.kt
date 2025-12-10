package com.example.ai_challenge_local_llm_chat.core.remote

data class RemoteLlmConfig(
    val baseUrl: String,
    val chatPath: String,
    val apiKey: String,
    val model: String,
    val requestTimeoutMs: Long = 120_000L
) {
    fun resolveChatUrl(): String {
        if (baseUrl.isBlank()) return ""
        val normalizedBase = baseUrl.trim().trimEnd('/')
        val normalizedPath = chatPath.trim().ifEmpty { "/v1/chat/completions" }
        val sanitizedPath = if (normalizedPath.startsWith("/")) normalizedPath else "/$normalizedPath"
        return "$normalizedBase$sanitizedPath"
    }
}
