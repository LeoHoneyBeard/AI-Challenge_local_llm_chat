package com.example.ai_challenge_local_llm_chat.core.llm

import android.content.Context
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

class ModelFileManager(
    private val context: Context,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend fun ensureModelOnDisk(): File = withContext(dispatcher) {
        val modelsDir = File(context.filesDir, "models").apply { if (!exists()) mkdirs() }
        val destination = File(modelsDir, LlmConfig.MODEL_FILE_NAME)
        if (!destination.exists()) {
            try {
                context.assets.open(LlmConfig.MODEL_ASSET_PATH).use { input ->
                    destination.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            } catch (ioException: IOException) {
                if (destination.exists()) {
                    destination.delete()
                }
                throw MissingModelFileException(
                    "Модель не найдена. Положите ${LlmConfig.MODEL_FILE_NAME} в app/src/main/assets/models.",
                    ioException
                )
            }
        }
        destination
    }
}
