package com.example.ai_challenge_local_llm_chat.data.remote

import android.util.Log
import com.example.ai_challenge_local_llm_chat.core.remote.RemoteLlmConfig
import com.example.ai_challenge_local_llm_chat.domain.model.ChatMessage
import com.example.ai_challenge_local_llm_chat.domain.model.MessageRole
import com.example.ai_challenge_local_llm_chat.domain.model.VpsModelInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

class VpsLlmClient(
    private val httpClient: OkHttpClient,
    private val config: RemoteLlmConfig
) {

    companion object {
        private const val TAG = "VpsLlmClient"
        private const val JSON_MEDIA_TYPE = "application/json; charset=utf-8"
    }

    suspend fun sendMessage(request: RemoteChatRequest): RemoteChatResponse =
        withContext(Dispatchers.IO) {
            val endpoint = config.resolveChatUrl()
            require(endpoint.isNotBlank()) { "VPS endpoint is not configured. Check local.properties." }

            val payload = JSONObject().apply {
                put("prompt", request.prompt)
                if (request.conversationId.isNullOrBlank()) {
                    put("conversation_id", JSONObject.NULL)
                } else {
                    put("conversation_id", request.conversationId)
                }
                request.model?.takeIf { it.isNotBlank() }?.let { put("model", it) }
                request.temperature?.let { put("temperature", it) }
                request.maxTokens?.let { put("max_tokens", it) }
            }
            val payloadString = payload.toString()

            val requestBuilder = Request.Builder()
                .url(endpoint)
                .header("Content-Type", "application/json")
                .post(payloadString.toRequestBody(JSON_MEDIA_TYPE.toMediaType()))

            if (config.apiKey.isNotBlank()) {
                requestBuilder.header("Authorization", "Bearer ")
            }

            Log.d(TAG, "POST  conversation= body=")
            val start = System.currentTimeMillis()
            val response = httpClient.newCall(requestBuilder.build()).execute()
            val body = response.body?.string().orEmpty()
            val elapsed = System.currentTimeMillis() - start
            Log.d(TAG, "VPS chat responded in  ms with code=")
            Log.d(TAG, "VPS chat raw response: ")

            if (!response.isSuccessful) {
                val errorMessage = parseErrorMessage(body)
                response.close()
                throw IllegalStateException("VPS chat error : ")
            }

            val json = JSONObject(body)
            val reply = json.optString("reply", json.optString("response"))
            val replyText = reply.ifBlank {
                response.close()
                throw IllegalStateException("Empty VPS reply")
            }
            val resolvedConversationId = json.optString("conversation_id")
            if (resolvedConversationId.isBlank()) {
                response.close()
                throw IllegalStateException("VPS reply does not contain conversation_id")
            }
            response.close()
            RemoteChatResponse(replyText, resolvedConversationId)
        }

    suspend fun fetchHistory(conversationId: String): List<ChatMessage> = withContext(Dispatchers.IO) {
        val endpoint = config.resolveHistoryUrl(conversationId)
        require(endpoint.isNotBlank()) { "VPS history endpoint is not configured." }

        val requestBuilder = Request.Builder()
            .url(endpoint)
            .get()

        if (config.apiKey.isNotBlank()) {
            requestBuilder.header("Authorization", "Bearer ")
        }

        Log.d(TAG, "GET ")
        val start = System.currentTimeMillis()
        val response = httpClient.newCall(requestBuilder.build()).execute()
        val body = response.body?.string().orEmpty()
        val elapsed = System.currentTimeMillis() - start
        Log.d(TAG, "History fetched in  ms with code=")
        Log.d(TAG, "History raw response: ")

        if (!response.isSuccessful) {
            val errorMessage = parseErrorMessage(body)
            response.close()
            throw IllegalStateException("VPS history error : ")
        }

        val messages = parseHistoryMessages(body)
        response.close()
        messages
    }

    suspend fun fetchModels(): List<VpsModelInfo> = withContext(Dispatchers.IO) {
        val endpoint = config.resolveModelsUrl()
        require(endpoint.isNotBlank()) { "VPS models endpoint is not configured." }
        val requestBuilder = Request.Builder()
            .url(endpoint)
            .get()

        if (config.apiKey.isNotBlank()) {
            requestBuilder.header("Authorization", "Bearer ")
        }

        Log.d(TAG, "GET ")
        val start = System.currentTimeMillis()
        val response = httpClient.newCall(requestBuilder.build()).execute()
        val body = response.body?.string().orEmpty()
        val elapsed = System.currentTimeMillis() - start
        Log.d(TAG, "Models fetched in  ms with code=")
        Log.d(TAG, "Models raw response: ")

        if (!response.isSuccessful) {
            val errorMessage = parseErrorMessage(body)
            response.close()
            throw IllegalStateException("VPS models error : ")
        }

        val array = runCatching { JSONArray(body) }.getOrElse {
            response.close()
            Log.w(TAG, "Cannot parse models JSON: ")
            return@withContext emptyList<VpsModelInfo>()
        }
        val models = mutableListOf<VpsModelInfo>()
        for (index in 0 until array.length()) {
            val item = array.optJSONObject(index) ?: continue
            val id = item.optString("id")
            if (id.isBlank()) continue
            val name = item.optString("name", id)
            val description = item.optString("description", "")
            models += VpsModelInfo(id = id, name = name, description = description)
        }
        response.close()
        models
    }

    private fun parseHistoryMessages(body: String): List<ChatMessage> {
        val json = runCatching { JSONObject(body) }.getOrElse {
            Log.w(TAG, "Cannot parse VPS history JSON: ")
            return emptyList()
        }
        val messages = json.optJSONArray("messages") ?: JSONArray()
        val baseTimestamp = System.currentTimeMillis()
        val result = mutableListOf<ChatMessage>()
        for (index in 0 until messages.length()) {
            val item = messages.optJSONObject(index) ?: continue
            val content = item.optString("content")
            if (content.isBlank()) continue
            val role = parseRole(item.optString("role"))
            result += ChatMessage(
                id = index.toLong(),
                role = role,
                content = content,
                timestamp = baseTimestamp + index
            )
        }
        return result
    }

    private fun parseRole(rawRole: String): MessageRole = when (rawRole.lowercase()) {
        "assistant" -> MessageRole.MODEL
        "system" -> MessageRole.SYSTEM
        else -> MessageRole.USER
    }

    private fun parseErrorMessage(body: String): String {
        if (body.isBlank()) return "empty body"
        return runCatching {
            val json = JSONObject(body)
            when {
                json.has("detail") -> json.optString("detail")
                json.has("error") -> json.optString("error")
                else -> body.take(200)
            }
        }.getOrElse { body.take(200) }
    }

    private fun String.forLog(maxLength: Int = 1000): String =
        if (length <= maxLength) this else take(maxLength) + "..."
}

data class RemoteChatRequest(
    val prompt: String,
    val conversationId: String?,
    val model: String?,
    val temperature: Float?,
    val maxTokens: Int?
)

data class RemoteChatResponse(
    val reply: String,
    val conversationId: String
)
