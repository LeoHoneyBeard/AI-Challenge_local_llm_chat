package com.example.ai_challenge_local_llm_chat.data.remote

import android.util.Log
import com.example.ai_challenge_local_llm_chat.core.remote.RemoteLlmClient
import com.example.ai_challenge_local_llm_chat.core.remote.RemoteLlmConfig
import com.example.ai_challenge_local_llm_chat.domain.model.ChatMessage
import com.example.ai_challenge_local_llm_chat.domain.model.MessageRole
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
) : RemoteLlmClient {

    companion object {
        private const val TAG = "VpsLlmClient"
    }

    override suspend fun requestCompletion(messages: List<ChatMessage>): String = withContext(Dispatchers.IO) {
        val endpoint = config.resolveChatUrl()
        require(endpoint.isNotBlank()) { "Не задан адрес VPS сервера. Заполните local.properties." }

        val payload = JSONObject().apply {
            put("model", config.model)
            put("prompt", buildPrompt(messages))
            put("messages", JSONArray().apply {
                messages.forEach { message ->
                    put(
                        JSONObject().apply {
                            put("role", mapRole(message.role))
                            put("content", message.content)
                        }
                    )
                }
            })
        }

        val requestBuilder = Request.Builder()
            .url(endpoint)
            .header("Content-Type", "application/json")
            .post(payload.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))

        if (config.apiKey.isNotBlank()) {
            requestBuilder.header("Authorization", "Bearer ${config.apiKey}")
        }

        Log.d(TAG, "Выполняем запрос к $endpoint, сообщений=${messages.size}, модель=${config.model}")
        val startTime = System.currentTimeMillis()
        val response = httpClient.newCall(requestBuilder.build()).execute()
        val body = response.body?.string()
        val elapsed = System.currentTimeMillis() - startTime
        Log.d(TAG, "Ответ VPS за ${elapsed} мс, код=${response.code}")
        if (!response.isSuccessful) {
            val errorMessage = body?.let { parseErrorMessage(it) } ?: "empty error body"
            Log.e(TAG, "Ошибка запроса: ${response.code} $errorMessage")
            response.close()
            throw IllegalStateException("Ошибка VPS: ${response.code} $errorMessage")
        }

        val content = body?.let { parseContent(it) }.orEmpty()
        response.close()
        if (content.isBlank()) {
            Log.w(TAG, "Пустой ответ от VPS")
            throw IllegalStateException("Пустой ответ VPS модели")
        }
        Log.d(TAG, "Успешный ответ длиной=${content.length}")
        content
    }

    private fun buildPrompt(messages: List<ChatMessage>): String = buildString {
        messages.forEach { message ->
            when (message.role) {
                MessageRole.USER -> append("User: ${message.content}\n")
                MessageRole.MODEL -> append("Assistant: ${message.content}\n")
                MessageRole.SYSTEM -> append("System: ${message.content}\n")
            }
        }
        append("Assistant:")
    }

    private fun mapRole(role: MessageRole): String = when (role) {
        MessageRole.USER -> "user"
        MessageRole.MODEL -> "assistant"
        MessageRole.SYSTEM -> "system"
    }

    private fun parseContent(body: String): String {
        Log.d(TAG, "Сырой ответ VPS: ${body.take(500)}")
        val json = runCatching { JSONObject(body) }.getOrElse {
            Log.w(TAG, "Не удалось распарсить ответ как JSON: ${it.message}")
            return body
        }

        val directReply = json.optString("reply")
        if (directReply.isNotBlank()) {
            Log.d(TAG, "Ответ из поля reply длиной=${directReply.length}")
            return directReply
        }

        val responseField = json.optString("response")
        if (responseField.isNotBlank()) {
            Log.d(TAG, "Ответ из поля response длиной=${responseField.length}")
            return responseField
        }

        val choices = json.optJSONArray("choices") ?: run {
            Log.w(TAG, "В ответе нет массива choices и полей reply/response")
            return ""
        }
        if (choices.length() == 0) return ""
        val firstChoice = choices.optJSONObject(0) ?: return ""
        val messageObj = firstChoice.optJSONObject("message")
        val directMessage = messageObj?.optString("content").orEmpty()
        if (directMessage.isNotBlank()) {
            Log.d(TAG, "Ответ из message.content длиной=${directMessage.length}")
            return directMessage
        }
        val textFallback = firstChoice.optString("text").orEmpty()
        if (textFallback.isNotBlank()) {
            Log.d(TAG, "Ответ из choice.text длиной=${textFallback.length}")
        } else {
            Log.w(TAG, "Не удалось найти content/text в ответе")
        }
        return textFallback
    }

    private fun parseErrorMessage(body: String): String {
        return runCatching {
            val json = JSONObject(body)
            when {
                json.has("error") -> {
                    val errorObj = json.getJSONObject("error")
                    errorObj.optString("message", errorObj.toString())
                }
                else -> json.toString()
            }
        }.getOrElse { body.take(200) }
    }
}
