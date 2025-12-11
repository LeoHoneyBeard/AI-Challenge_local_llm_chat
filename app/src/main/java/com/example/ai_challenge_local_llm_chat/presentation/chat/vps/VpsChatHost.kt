package com.example.ai_challenge_local_llm_chat.presentation.chat.vps

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ai_challenge_local_llm_chat.R
import com.example.ai_challenge_local_llm_chat.presentation.chat.ChatScreen

@Composable
fun VpsChatHost(
    conversationId: String?,
    dependencies: VpsChatDependencies,
    onNavigateBack: () -> Unit
) {
    val factory = remember(conversationId, dependencies) { VpsChatViewModelFactory(dependencies, conversationId) }
    val viewModel: VpsChatViewModel = viewModel(
        factory = factory,
        key = "vps_chat_"
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val title = stringResource(id = R.string.vps_chat_title)
    val emptyStateMessage = stringResource(id = R.string.vps_chat_empty_state)

    ChatScreen(
        state = state.chatState,
        onInputChanged = viewModel::onInputChanged,
        onSendMessage = viewModel::onSendMessage,
        title = title,
        emptyStateMessage = emptyStateMessage,
        onNavigateBack = onNavigateBack,
        onOpenSettings = viewModel::onOpenSettings,
        isHistoryLoading = state.isHistoryLoading
    )

    if (state.isSettingsVisible) {
        VpsSettingsDialog(
            state = state.settingsState,
            onDismiss = viewModel::onDismissSettings,
            onApply = viewModel::onApplySettings,
            onModelSelected = viewModel::onModelSelected,
            onTemperatureChanged = viewModel::onTemperatureChanged,
            onMaxTokensChanged = viewModel::onMaxTokensChanged
        )
    }
}

