package com.example.ai_challenge_local_llm_chat.core.remote

data class RemoteLlmConfig(
    val baseUrl: String,
    val chatPath: String,
    val historyPath: String,
    val modelsPath: String,
    val apiKey: String,
    val model: String,
    val requestTimeoutMs: Long = 120_000L
) {
    fun resolveChatUrl(): String = buildUrl(chatPath.ifBlank { "/chat" })

    fun resolveHistoryUrl(conversationId: String): String {
        if (conversationId.isBlank()) return ""
        val baseHistoryPath = historyPath.ifBlank { "/history" }
        val sanitized = baseHistoryPath.trim().trimEnd('/')
        val prefixed = when {
            sanitized.isBlank() -> ""
            sanitized.startsWith("/") -> sanitized
            else -> "/$sanitized"
        }
        val pathWithId = if (prefixed.isBlank()) "/$conversationId" else "$prefixed/$conversationId"
        return buildUrl(pathWithId)
    }

    fun resolveModelsUrl(): String = buildUrl(modelsPath.ifBlank { "/models" })

    private fun buildUrl(path: String): String {
        if (baseUrl.isBlank()) return ""
        val normalizedBase = baseUrl.trim().trimEnd('/')
        val normalizedPath = path.trim()
        val sanitizedPath = when {
            normalizedPath.isBlank() -> ""
            normalizedPath.startsWith("/") -> normalizedPath
            else -> "/$normalizedPath"
        }
        return normalizedBase + sanitizedPath
    }
}
