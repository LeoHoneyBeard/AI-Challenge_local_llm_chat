package com.example.ai_challenge_local_llm_chat.data.mapper

import com.example.ai_challenge_local_llm_chat.data.local.db.ChatMessageEntity
import com.example.ai_challenge_local_llm_chat.domain.model.ChatMessage
import com.example.ai_challenge_local_llm_chat.domain.model.MessageRole

fun ChatMessageEntity.toDomain(): ChatMessage = ChatMessage(
    id = id,
    role = MessageRole.valueOf(role),
    content = content,
    timestamp = timestamp
)

fun ChatMessage.toEntity(): ChatMessageEntity = ChatMessageEntity(
    id = id,
    role = role.name,
    content = content,
    timestamp = timestamp
)
