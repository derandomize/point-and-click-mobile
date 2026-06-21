package com.podzemnayapochta.presentation.game

import com.podzemnayapochta.domain.model.GameState
import com.podzemnayapochta.domain.repository.GameContent

/**
 * Состояние игрового экрана для UI (см. docs/architecture.md — поток управления).
 */
sealed interface GameUiState {
    data object Loading : GameUiState

    data class Error(
        val message: String,
    ) : GameUiState

    data class Ready(
        val content: GameContent,
        val gameState: GameState,
        val dialogue: DialogueUiState? = null,
    ) : GameUiState
}
