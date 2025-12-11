package com.example.ai_challenge_local_llm_chat.di

import android.content.Context
import androidx.room.Room
import com.example.ai_challenge_local_llm_chat.BuildConfig
import com.example.ai_challenge_local_llm_chat.core.llm.LlamaCppEngine
import com.example.ai_challenge_local_llm_chat.core.llm.LocalLlmEngine
import com.example.ai_challenge_local_llm_chat.core.remote.RemoteLlmConfig
import com.example.ai_challenge_local_llm_chat.data.local.db.ChatDatabase
import com.example.ai_challenge_local_llm_chat.data.local.preferences.VpsSettingsStorage
import com.example.ai_challenge_local_llm_chat.data.remote.VpsLlmClient
import com.example.ai_challenge_local_llm_chat.data.repository.ChatRepositoryImpl
import com.example.ai_challenge_local_llm_chat.data.repository.VpsConversationRepositoryImpl
import com.example.ai_challenge_local_llm_chat.domain.model.ChatSessionType
import com.example.ai_challenge_local_llm_chat.domain.repository.ChatRepository
import com.example.ai_challenge_local_llm_chat.domain.repository.VpsConversationRepository
import com.example.ai_challenge_local_llm_chat.domain.usecase.ClearChatHistoryUseCase
import com.example.ai_challenge_local_llm_chat.domain.usecase.EnsureModelReadyUseCase
import com.example.ai_challenge_local_llm_chat.domain.usecase.GenerateModelResponseUseCase
import com.example.ai_challenge_local_llm_chat.domain.usecase.ObserveChatMessagesUseCase
import com.example.ai_challenge_local_llm_chat.domain.usecase.ObserveVpsConversationsUseCase
import com.example.ai_challenge_local_llm_chat.domain.usecase.SaveVpsConversationUseCase
import com.example.ai_challenge_local_llm_chat.domain.usecase.ShutdownLlmUseCase
import com.example.ai_challenge_local_llm_chat.presentation.chat.ChatFeatureDependencies
import com.example.ai_challenge_local_llm_chat.presentation.chat.vps.VpsChatDependencies
import com.example.ai_challenge_local_llm_chat.presentation.chat.vps.VpsConversationDependencies
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class AppModule(context: Context) {
    private val appContext = context.applicationContext

    private val database: ChatDatabase = Room.databaseBuilder(
        appContext,
        ChatDatabase::class.java,
        "chat-messages.db"
    ).fallbackToDestructiveMigration().build()

    private val localRepository: ChatRepository =
        ChatRepositoryImpl(database.chatMessageDao(), ChatSessionType.LOCAL)
    private val vpsConversationRepository: VpsConversationRepository =
        VpsConversationRepositoryImpl(database.vpsConversationDao())
    private val vpsSettingsStorage = VpsSettingsStorage(appContext)
    private val llmEngine: LocalLlmEngine = LlamaCppEngine(appContext)

    private val ensureLocalModelReadyUseCase = EnsureModelReadyUseCase(llmEngine)
    private val shutdownLocalLlmUseCase = ShutdownLlmUseCase(llmEngine)

    private val remoteConfig = RemoteLlmConfig(
        baseUrl = BuildConfig.VPS_BASE_URL,
        chatPath = BuildConfig.VPS_CHAT_PATH,
        historyPath = BuildConfig.VPS_HISTORY_PATH,
        modelsPath = BuildConfig.VPS_MODELS_PATH,
        apiKey = BuildConfig.VPS_API_KEY,
        model = BuildConfig.VPS_MODEL
    )
    private val remoteHttpClient: OkHttpClient = OkHttpClient.Builder()
        .callTimeout(remoteConfig.requestTimeoutMs, TimeUnit.MILLISECONDS)
        .connectTimeout(remoteConfig.requestTimeoutMs, TimeUnit.MILLISECONDS)
        .readTimeout(remoteConfig.requestTimeoutMs, TimeUnit.MILLISECONDS)
        .writeTimeout(remoteConfig.requestTimeoutMs, TimeUnit.MILLISECONDS)
        .build()
    private val remoteClient = VpsLlmClient(remoteHttpClient, remoteConfig)

    val localChatDependencies = ChatFeatureDependencies(
        observeChatMessagesUseCase = ObserveChatMessagesUseCase(localRepository),
        responseGenerator = GenerateModelResponseUseCase(localRepository, llmEngine),
        clearChatHistoryUseCase = ClearChatHistoryUseCase(localRepository),
        ensureModelReady = { ensureLocalModelReadyUseCase() },
        shutdownLlm = { shutdownLocalLlmUseCase() }
    )

    val vpsConversationDependencies = VpsConversationDependencies(
        observeConversationsUseCase = ObserveVpsConversationsUseCase(vpsConversationRepository)
    )

    val vpsChatDependencies = VpsChatDependencies(
        remoteClient = remoteClient,
        saveConversationUseCase = SaveVpsConversationUseCase(vpsConversationRepository),
        settingsStorage = vpsSettingsStorage
    )
}
