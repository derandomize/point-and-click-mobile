package com.podzemnayapochta.domain.usecase

import com.podzemnayapochta.domain.model.GameState
import com.podzemnayapochta.domain.model.Letter
import com.podzemnayapochta.domain.model.LetterStatus

/**
 * Управляет жизненным циклом писем: получено → в пути → доставлено
 * (см. docs/architecture.md, QuestEngine).
 */
class QuestEngine {
    /** Выдать письмо игроку на почтовой станции (LOCKED → RECEIVED). */
    fun receiveLetter(
        state: GameState,
        letterId: String,
    ): GameState = transition(state, letterId, from = LetterStatus.LOCKED, to = LetterStatus.RECEIVED)

    /** Пометить письмо как «в пути» после определения адресата (RECEIVED → IN_TRANSIT). */
    fun markInTransit(
        state: GameState,
        letterId: String,
    ): GameState = transition(state, letterId, from = LetterStatus.RECEIVED, to = LetterStatus.IN_TRANSIT)

    /**
     * Помечает все полученные письма как «в пути»: игрок вышел на маршрут
     * (см. ROADMAP, PR 2 — RECEIVED → IN_TRANSIT при первом перемещении).
     */
    fun markCarriedInTransit(state: GameState): GameState =
        state.letters.values
            .filter { it.status == LetterStatus.RECEIVED }
            .fold(state) { acc, letter -> markInTransit(acc, letter.id) }

    /** Письма, которые можно сейчас доставить (получены или в пути). */
    fun deliverableLetters(state: GameState): List<Letter> =
        state.letters.values.filter {
            it.status == LetterStatus.RECEIVED || it.status == LetterStatus.IN_TRANSIT
        }

    /**
     * Письма в «сумке» почтальона — все, что игрок уже видел (не [LetterStatus.LOCKED]).
     * Недоставленные идут раньше доставленных, чтобы активные квесты были сверху.
     */
    fun knownLetters(state: GameState): List<Letter> =
        state.letters.values
            .filter { it.status != LetterStatus.LOCKED }
            .sortedBy { it.isDelivered }

    private fun transition(
        state: GameState,
        letterId: String,
        from: LetterStatus,
        to: LetterStatus,
    ): GameState {
        val letter = state.letter(letterId) ?: return state
        if (letter.status != from) return state
        val updated = letter.copy(status = to)
        return state.copy(letters = state.letters + (letterId to updated))
    }
}
