package com.example.ai_challenge_local_llm_chat.presentation.chat.vps

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun VpsConversationListHost(
    dependencies: VpsConversationDependencies,
    onNewChat: () -> Unit,
    onSelectConversation: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val factory = remember(dependencies) { VpsConversationListViewModelFactory(dependencies) }
    val viewModel: VpsConversationListViewModel = viewModel(factory = factory, key = "vps_conversation_list_vm")
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    VpsConversationListScreen(
        state = state,
        onNewChat = onNewChat,
        onSelectConversation = onSelectConversation,
        onNavigateBack = onNavigateBack
    )
}
