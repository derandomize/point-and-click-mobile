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
        /** Открыта ли «сумка» почтальона (оверлей со списком писем). */
        val isBagOpen: Boolean = false,
        /** Одноразовое сообщение об итоге доставки письма. */
        val deliveryFeedback: String? = null,
    ) : GameUiState
}
