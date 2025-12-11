package com.example.ai_challenge_local_llm_chat.presentation.chat

import com.example.ai_challenge_local_llm_chat.domain.model.ChatMessage

fun ChatMessage.toUiModel(): ChatMessageUi = ChatMessageUi(
    id = id,
    role = role,
    text = content,
    timestamp = timestamp
)
