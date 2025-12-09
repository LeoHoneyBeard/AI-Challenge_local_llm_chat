package com.example.ai_challenge_local_llm_chat.di

import android.content.Context
import androidx.room.Room
import com.example.ai_challenge_local_llm_chat.core.llm.LlamaCppEngine
import com.example.ai_challenge_local_llm_chat.core.llm.LocalLlmEngine
import com.example.ai_challenge_local_llm_chat.data.local.db.ChatDatabase
import com.example.ai_challenge_local_llm_chat.data.repository.ChatRepositoryImpl
import com.example.ai_challenge_local_llm_chat.domain.repository.ChatRepository
import com.example.ai_challenge_local_llm_chat.domain.usecase.ClearChatHistoryUseCase
import com.example.ai_challenge_local_llm_chat.domain.usecase.EnsureModelReadyUseCase
import com.example.ai_challenge_local_llm_chat.domain.usecase.GenerateModelResponseUseCase
import com.example.ai_challenge_local_llm_chat.domain.usecase.ObserveChatMessagesUseCase
import com.example.ai_challenge_local_llm_chat.domain.usecase.ShutdownLlmUseCase

class AppModule(context: Context) {
    private val appContext = context.applicationContext

    private val database: ChatDatabase = Room.databaseBuilder(
        appContext,
        ChatDatabase::class.java,
        "chat-messages.db"
    ).fallbackToDestructiveMigration().build()

    private val repository: ChatRepository = ChatRepositoryImpl(database.chatMessageDao())
    private val llmEngine: LocalLlmEngine = LlamaCppEngine(appContext)

    val observeChatMessagesUseCase = ObserveChatMessagesUseCase(repository)
    val clearChatHistoryUseCase = ClearChatHistoryUseCase(repository)
    val generateModelResponseUseCase = GenerateModelResponseUseCase(repository, llmEngine)
    val ensureModelReadyUseCase = EnsureModelReadyUseCase(llmEngine)
    val shutdownLlmUseCase = ShutdownLlmUseCase(llmEngine)
}
