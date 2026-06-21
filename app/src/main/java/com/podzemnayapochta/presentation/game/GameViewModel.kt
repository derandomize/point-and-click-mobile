package com.podzemnayapochta.presentation.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.podzemnayapochta.domain.model.DialogueChoice
import com.podzemnayapochta.domain.model.GameState
import com.podzemnayapochta.domain.model.Letter
import com.podzemnayapochta.domain.model.LetterStatus
import com.podzemnayapochta.domain.repository.ContentRepository
import com.podzemnayapochta.domain.repository.GameContent
import com.podzemnayapochta.domain.usecase.DeliverLetter
import com.podzemnayapochta.domain.usecase.DeliverResult
import com.podzemnayapochta.domain.usecase.DialogueEngine
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
        private val dialogueEngine: DialogueEngine,
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
                    // Выходя на маршрут, полученные письма становятся «в пути».
                    is MoveResult.Success -> questEngine.markCarriedInTransit(result.state)
                    else -> state
                }
            }

        /** Доставить письмо NPC. Возвращает результат для UI-обратной связи. */
        fun deliver(
            letterId: String,
            recipientNpcId: String,
        ): DeliverResult {
            val ready = _uiState.value as? GameUiState.Ready ?: return DeliverResult.LetterNotFound
            val result = deliverLetter(ready.gameState, letterId, recipientNpcId)
            if (result is DeliverResult.Success) {
                _uiState.update { ready.copy(gameState = result.state) }
            }
            return result
        }

        /** Начать диалог с NPC (тап по персонажу в локации). */
        fun startDialogue(npcId: String) {
            val ready = _uiState.value as? GameUiState.Ready ?: return
            val npc = ready.content.npc(npcId) ?: return
            val rootId = npc.dialogueRootId ?: return
            val node = ready.content.dialogue(rootId) ?: return
            _uiState.update {
                ready.copy(
                    dialogue =
                        DialogueUiState(
                            npcId = npcId,
                            node = node,
                            speakerName = npc.name,
                            availableChoices = dialogueEngine.availableChoices(node.choices, ready.gameState),
                            deliverableLetter = deliverableLetterFor(ready.gameState, npcId),
                        ),
                )
            }
        }

        /** Выбрать вариант ответа: применить эффекты и перейти к следующему узлу или завершить. */
        fun chooseDialogueOption(choice: DialogueChoice) {
            val ready = _uiState.value as? GameUiState.Ready ?: return
            val dialogue = ready.dialogue ?: return

            val newState = dialogueEngine.applyChoice(ready.gameState, choice)
            val nextNode = choice.targetNodeId?.let { ready.content.dialogue(it) }

            if (nextNode == null) {
                _uiState.update { ready.copy(gameState = newState, dialogue = null) }
                return
            }
            _uiState.update {
                ready.copy(
                    gameState = newState,
                    dialogue =
                        dialogue.copy(
                            node = nextNode,
                            availableChoices = dialogueEngine.availableChoices(nextNode.choices, newState),
                        ),
                )
            }
        }

        /** Принудительно закрыть диалог. */
        fun endDialogue() {
            val ready = _uiState.value as? GameUiState.Ready ?: return
            _uiState.update { ready.copy(dialogue = null) }
        }

        /** Открыть/закрыть «сумку» почтальона со списком писем. */
        fun setBagOpen(open: Boolean) {
            val ready = _uiState.value as? GameUiState.Ready ?: return
            _uiState.update { ready.copy(isBagOpen = open) }
        }

        /** Письма в сумке игрока (см. [QuestEngine.knownLetters]). */
        fun bagLetters(): List<Letter> {
            val ready = _uiState.value as? GameUiState.Ready ?: return emptyList()
            return questEngine.knownLetters(ready.gameState)
        }

        /** Вручить письмо NPC, с которым идёт диалог. */
        fun deliverToCurrentNpc() {
            val ready = _uiState.value as? GameUiState.Ready ?: return
            val dialogue = ready.dialogue ?: return
            val letter = dialogue.deliverableLetter ?: return

            when (val result = deliverLetter(ready.gameState, letter.id, dialogue.npcId)) {
                is DeliverResult.Success -> {
                    val nextLetterUnlocked = unlockNextLetter(result.state, letter.id)
                    _uiState.update {
                        ready.copy(
                            gameState = nextLetterUnlocked,
                            dialogue = dialogue.copy(deliverableLetter = null),
                            deliveryFeedback = "Письмо доставлено! +${result.reward}",
                        )
                    }
                }

                else ->
                    _uiState.update {
                        ready.copy(deliveryFeedback = "Это письмо не для ${dialogue.speakerName ?: "него"}")
                    }
            }
        }

        /** Сбросить одноразовое сообщение о доставке (после показа в UI). */
        fun consumeDeliveryFeedback() {
            val ready = _uiState.value as? GameUiState.Ready ?: return
            if (ready.deliveryFeedback != null) {
                _uiState.update { ready.copy(deliveryFeedback = null) }
            }
        }

        private fun deliverableLetterFor(
            state: GameState,
            npcId: String,
        ): Letter? =
            state.letters.values.firstOrNull { letter ->
                letter.recipientNpcId == npcId &&
                    (letter.status == LetterStatus.RECEIVED || letter.status == LetterStatus.IN_TRANSIT)
            }

        /** После доставки выдаёт следующее по списку письмо игроку (LOCKED → RECEIVED). */
        private fun unlockNextLetter(
            state: GameState,
            deliveredLetterId: String,
        ): GameState {
            val nextLockedId =
                state.letters.values
                    .firstOrNull { it.id != deliveredLetterId && it.status == LetterStatus.LOCKED }
                    ?.id ?: return state
            return questEngine.receiveLetter(state, nextLockedId)
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
            _uiState.update { ready.copy(gameState = newState) }
        }
    }
