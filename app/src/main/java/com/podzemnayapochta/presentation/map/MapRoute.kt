package com.podzemnayapochta.presentation.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.podzemnayapochta.presentation.game.GameUiState
import com.podzemnayapochta.presentation.game.GameViewModel

/**
 * Связывает [GameViewModel] с [MapScreen]: подсвечивает текущую локацию,
 * а тап по локации открывает её экран (переход выполняет вызывающий слой).
 */
@Composable
fun MapRoute(
    viewModel: GameViewModel,
    onLocationSelected: (String) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

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

        is GameUiState.Ready ->
            MapScreen(
                locations = MapLocation.placeholderCity(),
                currentLocationId = s.gameState.currentLocationId,
                onLocationSelected = onLocationSelected,
            )
    }
}
