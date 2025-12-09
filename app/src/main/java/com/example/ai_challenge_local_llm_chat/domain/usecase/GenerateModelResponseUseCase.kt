package com.example.ai_challenge_local_llm_chat.domain.usecase

import com.example.ai_challenge_local_llm_chat.core.llm.LocalLlmEngine
import com.example.ai_challenge_local_llm_chat.domain.model.ChatMessage
import com.example.ai_challenge_local_llm_chat.domain.model.MessageRole
import com.example.ai_challenge_local_llm_chat.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow

class GenerateModelResponseUseCase(
    private val repository: ChatRepository,
    private val engine: LocalLlmEngine,
    private val fallbackReply: String = "Мне не удалось получить ответ. Попробуйте еще раз."
) {
    sealed interface Event {
        data object Started : Event
        data class Chunk(val content: String) : Event
        data class Success(val fullContent: String) : Event
    }

    operator fun invoke(userInput: String): Flow<Event> = flow {
        val sanitizedInput = userInput.trim()
        if (sanitizedInput.isEmpty()) return@flow

        val timestamp = System.currentTimeMillis()
        repository.insertMessage(
            ChatMessage(
                role = MessageRole.USER,
                content = sanitizedInput,
                timestamp = timestamp
            )
        )

        emit(Event.Started)

        val history = repository.getMessagesSnapshot()
        val responseBuilder = StringBuilder()

        engine.streamResponse(history).collect { chunk ->
            if (chunk.isNotEmpty()) {
                responseBuilder.append(chunk)
                emit(Event.Chunk(responseBuilder.toString()))
            }
        }

        val finalReply = responseBuilder.toString().ifBlank { fallbackReply }
        repository.insertMessage(
            ChatMessage(
                role = MessageRole.MODEL,
                content = finalReply,
                timestamp = System.currentTimeMillis()
            )
        )

        emit(Event.Success(finalReply))
    }
}
