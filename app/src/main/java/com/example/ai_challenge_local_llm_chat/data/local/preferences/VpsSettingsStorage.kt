package com.example.ai_challenge_local_llm_chat.data.local.preferences

import android.content.Context
import com.example.ai_challenge_local_llm_chat.domain.model.VpsChatSettings

class VpsSettingsStorage(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun load(): VpsChatSettings {
        val temperature = prefs.getFloat(KEY_TEMPERATURE, VpsChatSettings.DEFAULT_TEMPERATURE)
        val maxTokensRaw = prefs.getInt(KEY_MAX_TOKENS, -1)
        val modelId = prefs.getString(KEY_MODEL_ID, null)
        return VpsChatSettings(
            modelId = modelId,
            temperature = temperature,
            maxTokens = maxTokensRaw.takeIf { it > 0 }
        )
    }

    fun save(settings: VpsChatSettings) {
        prefs.edit()
            .putString(KEY_MODEL_ID, settings.modelId)
            .putFloat(KEY_TEMPERATURE, settings.temperature)
            .putInt(KEY_MAX_TOKENS, settings.maxTokens ?: -1)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "vps_chat_settings"
        private const val KEY_MODEL_ID = "model_id"
        private const val KEY_TEMPERATURE = "temperature"
        private const val KEY_MAX_TOKENS = "max_tokens"
    }
}
