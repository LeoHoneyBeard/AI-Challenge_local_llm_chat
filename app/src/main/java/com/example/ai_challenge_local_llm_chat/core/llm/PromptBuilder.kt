package com.example.ai_challenge_local_llm_chat.core.llm

import com.example.ai_challenge_local_llm_chat.domain.model.ChatMessage
import com.example.ai_challenge_local_llm_chat.domain.model.MessageRole

class PromptBuilder {
    fun build(messages: List<ChatMessage>): String {
        val builder = StringBuilder()
        builder.append("<|system|>\n")
            .append(LlmConfig.SYSTEM_PROMPT)
            .append("\n<|end|>\n")

        messages.forEach { message ->
            when (message.role) {
                MessageRole.USER -> builder.append(formatSegment("user", message.content))
                MessageRole.MODEL -> builder.append(formatSegment("assistant", message.content))
                MessageRole.SYSTEM -> builder.append(formatSegment("system", message.content))
            }
        }

        builder.append("<|assistant|>\n")
        return builder.toString()
    }

    private fun formatSegment(role: String, content: String): String = buildString {
        append("<|$role|>\n")
        append(content.trim())
        append("\n<|end|>\n")
    }
}
