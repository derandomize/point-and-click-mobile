package com.podzemnayapochta.presentation.location

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.podzemnayapochta.engine.SceneBuilder
import com.podzemnayapochta.presentation.game.GameUiState
import com.podzemnayapochta.presentation.game.GameViewModel

/**
 * Связывает [GameViewModel] с [LocationScreen]: строит сцену текущей локации
 * через [SceneBuilder]. Тап по выходу выполняет переход (use-case MoveTo)
 * и навигацию; тап по NPC пробрасывается наверх (диалог появится в PR диалогов).
 */
@Composable
fun LocationRoute(
    locationId: String,
    viewModel: GameViewModel,
    onNavigateToLocation: (String) -> Unit,
    onNpcTapped: (String) -> Unit = {},
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val sceneBuilder = remember { SceneBuilder() }

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
                    onNpcTapped = onNpcTapped,
                    onExitTapped = { targetId ->
                        viewModel.moveTo(targetId)
                        onNavigateToLocation(targetId)
                    },
                )
            }
        }
    }
}
