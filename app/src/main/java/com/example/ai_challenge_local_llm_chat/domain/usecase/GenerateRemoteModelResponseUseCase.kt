package com.example.ai_challenge_local_llm_chat.domain.usecase

import com.example.ai_challenge_local_llm_chat.core.remote.RemoteLlmClient
import com.example.ai_challenge_local_llm_chat.domain.model.ChatMessage
import com.example.ai_challenge_local_llm_chat.domain.model.MessageRole
import com.example.ai_challenge_local_llm_chat.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GenerateRemoteModelResponseUseCase(
    private val repository: ChatRepository,
    private val remoteClient: RemoteLlmClient,
    private val fallbackReply: String = "Удалённая модель не ответила. Попробуйте позже."
) : ChatResponseGenerator {

    override operator fun invoke(userInput: String): Flow<ChatResponseEvent> = flow {
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

        emit(ChatResponseEvent.Started)

        val history = repository.getMessagesSnapshot()
        val reply = remoteClient.requestCompletion(history)

        emit(ChatResponseEvent.Chunk(reply))

        val finalReply = reply.ifBlank { fallbackReply }
        repository.insertMessage(
            ChatMessage(
                role = MessageRole.MODEL,
                content = finalReply,
                timestamp = System.currentTimeMillis()
            )
        )

        emit(ChatResponseEvent.Success(finalReply))
    }
}
