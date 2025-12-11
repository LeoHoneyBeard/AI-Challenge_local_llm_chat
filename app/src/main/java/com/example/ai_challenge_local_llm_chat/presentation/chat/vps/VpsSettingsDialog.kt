package com.example.ai_challenge_local_llm_chat.presentation.chat.vps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.util.Locale

@Composable
fun VpsSettingsDialog(
    state: VpsSettingsUiState,
    onDismiss: () -> Unit,
    onApply: () -> Unit,
    onModelSelected: (String) -> Unit,
    onTemperatureChanged: (Float) -> Unit,
    onMaxTokensChanged: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onApply, enabled = !state.isModelsLoading) {
                Text(text = "Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Отмена")
            }
        },
        title = { Text(text = "Настройки VPS чата") },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(text = "Модель", style = MaterialTheme.typography.titleMedium)
                when {
                    state.isModelsLoading -> {
                        CircularProgressIndicator()
                    }
                    state.availableModels.isEmpty() -> {
                        Text(
                            text = "Список моделей пуст. Проверьте сервер.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    else -> {
                        state.availableModels.forEach { model ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = model.id == state.selectedModelId,
                                    onClick = { onModelSelected(model.id) }
                                )
                                Column(modifier = Modifier.padding(start = 8.dp)) {
                                    Text(text = model.name, style = MaterialTheme.typography.bodyLarge)
                                    if (model.description.isNotBlank()) {
                                        Text(
                                            text = model.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val formattedTemperature = String.format(Locale.US, "%.1f", state.temperature)
                    Text(text = "Temperature: $formattedTemperature")
                    Slider(
                        value = state.temperature,
                        onValueChange = onTemperatureChanged,
                        valueRange = 0.1f..1f,
                        steps = 8
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Max tokens")
                    OutlinedTextField(
                        value = state.maxTokensInput,
                        onValueChange = onMaxTokensChanged,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        placeholder = { Text(text = "Не задано") }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    )
}



