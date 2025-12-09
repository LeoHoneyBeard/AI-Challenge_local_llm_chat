package com.example.ai_challenge_local_llm_chat.core.llm

import android.content.Context
import android.llama.cpp.LLamaAndroid
import com.example.ai_challenge_local_llm_chat.domain.model.ChatMessage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class LlamaCppEngine(
    context: Context,
    private val llamaAndroid: LLamaAndroid = LLamaAndroid.instance(),
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : LocalLlmEngine {
    private val modelFileManager = ModelFileManager(context.applicationContext, dispatcher)
    private val promptBuilder = PromptBuilder()
    private val loadMutex = Mutex()
    private val generationMutex = Mutex()
    @Volatile
    private var isLoaded = false

    override suspend fun warmUp() {
        loadMutex.withLock {
            if (!isLoaded) {
                val modelPath = modelFileManager.ensureModelOnDisk().absolutePath
                llamaAndroid.load(modelPath)
                isLoaded = true
            }
        }
    }

    override fun streamResponse(messages: List<ChatMessage>): Flow<String> = flow {
        warmUp()
        val prompt = promptBuilder.build(messages)
        generationMutex.withLock {
            llamaAndroid.send(prompt, formatChat = true).collect { token ->
                emit(token)
            }
        }
    }.flowOn(dispatcher)

    override suspend fun shutdown() {
        loadMutex.withLock {
            if (isLoaded) {
                llamaAndroid.unload()
                isLoaded = false
            }
        }
    }
}
