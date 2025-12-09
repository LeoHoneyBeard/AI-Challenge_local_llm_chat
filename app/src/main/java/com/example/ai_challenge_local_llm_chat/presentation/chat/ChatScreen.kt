package com.example.ai_challenge_local_llm_chat.presentation.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ai_challenge_local_llm_chat.di.AppModule
import com.example.ai_challenge_local_llm_chat.domain.model.MessageRole

@Composable
fun ChatApp(appModule: AppModule) {
    val factory = remember(appModule) {
        ChatViewModelFactory(
            appModule.observeChatMessagesUseCase,
            appModule.generateModelResponseUseCase,
            appModule.clearChatHistoryUseCase,
            appModule.ensureModelReadyUseCase,
            appModule.shutdownLlmUseCase
        )
    }
    val chatViewModel: ChatViewModel = viewModel(factory = factory)
    val uiState by chatViewModel.uiState.collectAsStateWithLifecycle()

    ChatScreen(
        state = uiState,
        onInputChanged = chatViewModel::onInputChanged,
        onSendMessage = chatViewModel::onSendMessage,
        onClearHistory = chatViewModel::onClearChat
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    state: ChatUiState,
    onInputChanged: (String) -> Unit,
    onSendMessage: () -> Unit,
    onClearHistory: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val lazyListState = rememberLazyListState()

    val streamingMessage = state.streamingResponse?.takeIf { it.isNotEmpty() }?.let {
        ChatMessageUi(
            id = Long.MAX_VALUE,
            role = MessageRole.MODEL,
            text = it,
            timestamp = System.currentTimeMillis(),
            isStreaming = true
        )
    }

    val messages = remember(state.messages, streamingMessage) {
        if (streamingMessage != null) state.messages + streamingMessage else state.messages
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            lazyListState.animateScrollToItem(messages.lastIndex)
        }
    }

    LaunchedEffect(state.errorMessage) {
        val message = state.errorMessage
        if (!message.isNullOrBlank()) {
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = "Local LLM Chat") },
                actions = {
                    if (state.isGenerating) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .heightIn(max = 24.dp),
                            strokeWidth = 2.dp
                        )
                    }
                    IconButton(onClick = onClearHistory, enabled = !state.isGenerating) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Очистить историю"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (messages.isEmpty() && !state.isGenerating) {
                EmptyStateMessage(modifier = Modifier.weight(1f))
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    state = lazyListState,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
                ) {
                    items(items = messages, key = { it.id }) { message ->
                        MessageBubble(message = message)
                    }
                }
            }

            ChatInput(
                text = state.inputText,
                onValueChange = onInputChanged,
                onSend = onSendMessage,
                isSendingAllowed = !state.isGenerating
            )
        }
    }
}

@Composable
private fun EmptyStateMessage(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Начните диалог с локальной моделью",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(32.dp)
        )
    }
}

@Composable
private fun MessageBubble(message: ChatMessageUi) {
    val isUser = message.role == MessageRole.USER
    val bubbleColor = if (isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .background(
                    color = bubbleColor,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(12.dp)
        ) {
            Text(
                text = if (isUser) "Вы" else "Модель",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = message.text,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
                modifier = Modifier.padding(top = 4.dp)
            )
            if (message.isStreaming) {
                Text(
                    text = "Генерация...",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun ChatInput(
    text: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    isSendingAllowed: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = onValueChange,
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 56.dp),
            placeholder = { Text(text = "Напишите сообщение...") },
            enabled = isSendingAllowed
        )
        IconButton(
            onClick = onSend,
            enabled = text.isNotBlank() && isSendingAllowed
        ) {
            Icon(imageVector = Icons.Default.Send, contentDescription = "Отправить")
        }
    }
}
