package com.podzemnayapochta.presentation.location

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.podzemnayapochta.engine.SceneBuilder
import com.podzemnayapochta.presentation.dialogue.DialogueOverlay
import com.podzemnayapochta.presentation.game.GameUiState
import com.podzemnayapochta.presentation.game.GameViewModel

/**
 * Связывает [GameViewModel] с [LocationScreen]: строит сцену текущей локации
 * через [SceneBuilder]. Тап по выходу выполняет переход (use-case MoveTo)
 * и навигацию; тап по NPC открывает диалог (оверлей поверх локации).
 * Доставка письма NPC сопровождается сообщением в Snackbar.
 */
@Composable
fun LocationRoute(
    locationId: String,
    viewModel: GameViewModel,
    onNavigateToLocation: (String) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val sceneBuilder = remember { SceneBuilder() }
    val snackbarHostState = remember { SnackbarHostState() }

    val feedback = (state as? GameUiState.Ready)?.deliveryFeedback
    LaunchedEffect(feedback) {
        if (feedback != null) {
            snackbarHostState.showSnackbar(feedback)
            viewModel.consumeDeliveryFeedback()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when (val s = state) {
                is GameUiState.Loading ->
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }

                is GameUiState.Error ->
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Ошибка: ${s.message}",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(24.dp),
                        )
                    }

                is GameUiState.Ready -> {
                    val scene = remember(s.content, locationId) { sceneBuilder.build(s.content, locationId) }
                    if (scene == null) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Локация не найдена: $locationId")
                        }
                    } else {
                        LocationScreen(
                            scene = scene,
                            onNpcTapped = viewModel::startDialogue,
                            onExitTapped = { targetId ->
                                viewModel.moveTo(targetId)
                                onNavigateToLocation(targetId)
                            },
                        )
                        s.dialogue?.let { dialogue ->
                            DialogueOverlay(
                                dialogue = dialogue,
                                onChoice = viewModel::chooseDialogueOption,
                                onDeliverLetter = viewModel::deliverToCurrentNpc,
                                onDismiss = viewModel::endDialogue,
                            )
                        }
                    }
                }
            }
        }
    }
}
