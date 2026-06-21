package com.podzemnayapochta.presentation.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.podzemnayapochta.domain.model.GameState
import com.podzemnayapochta.domain.repository.ContentRepository
import com.podzemnayapochta.domain.repository.GameContent
import com.podzemnayapochta.domain.usecase.DeliverLetter
import com.podzemnayapochta.domain.usecase.DeliverResult
import com.podzemnayapochta.domain.usecase.MoveResult
import com.podzemnayapochta.domain.usecase.MoveTo
import com.podzemnayapochta.domain.usecase.QuestEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Центральный ViewModel игры: загружает контент, держит [GameState]
 * и обновляет его через use-case-ы (см. docs/architecture.md — поток управления:
 * Input → ViewModel → UseCase → GameState → State flow → UI).
 */
@HiltViewModel
class GameViewModel
    @Inject
    constructor(
        private val contentRepository: ContentRepository,
        private val moveTo: MoveTo,
        private val deliverLetter: DeliverLetter,
        private val questEngine: QuestEngine,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<GameUiState>(GameUiState.Loading)
        val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

        init {
            loadGame()
        }

        fun loadGame() {
            _uiState.value = GameUiState.Loading
            viewModelScope.launch {
                runCatching { contentRepository.loadContent() }
                    .onSuccess { content -> _uiState.value = GameUiState.Ready(content, initialState(content)) }
                    .onFailure { error ->
                        _uiState.value = GameUiState.Error(error.message ?: "Не удалось загрузить контент")
                    }
            }
        }

        /** Переход в локацию по тапу на карте/выходе. */
        fun moveTo(targetLocationId: String) =
            updateReady { content, state ->
                val from = content.location(state.currentLocationId) ?: return@updateReady state
                when (val result = moveTo(state, from, targetLocationId)) {
                    is MoveResult.Success -> result.state
                    else -> state
                }
            }

        /** Получить письмо (LOCKED → RECEIVED). */
        fun receiveLetter(letterId: String) = updateReady { _, state -> questEngine.receiveLetter(state, letterId) }

        /** Доставить письмо NPC. Возвращает результат для UI-обратной связи. */
        fun deliver(
            letterId: String,
            recipientNpcId: String,
        ): DeliverResult {
            val ready = _uiState.value as? GameUiState.Ready ?: return DeliverResult.LetterNotFound
            val result = deliverLetter(ready.gameState, letterId, recipientNpcId)
            if (result is DeliverResult.Success) {
                _uiState.update { GameUiState.Ready(ready.content, result.state) }
            }
            return result
        }

        private fun initialState(content: GameContent): GameState =
            GameState(
                currentLocationId = content.startLocationId,
                letters = content.letters.associateBy { it.id },
                unlockedLocationIds = setOf(content.startLocationId),
            ).let { base ->
                // Первое письмо сразу выдаётся игроку на старте.
                val firstLetterId = content.letters.firstOrNull()?.id
                if (firstLetterId != null) {
                    questEngine.receiveLetter(base, firstLetterId)
                } else {
                    base
                }
            }

        private inline fun updateReady(transform: (GameContent, GameState) -> GameState) {
            val ready = _uiState.value as? GameUiState.Ready ?: return
            val newState = transform(ready.content, ready.gameState)
            _uiState.update { GameUiState.Ready(ready.content, newState) }
        }
    }
