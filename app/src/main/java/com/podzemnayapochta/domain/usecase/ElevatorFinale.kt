package com.podzemnayapochta.domain.usecase

import com.podzemnayapochta.domain.model.Ending
import com.podzemnayapochta.domain.model.GameState

/**
 * Логика финала у старого лифта (см. docs/idea.md — финал и 2 концовки).
 * Финал открывается по сюжету: игрок прознал про лифт ([FLAG_RUMOR])
 * и доставил достаточно писем ([requiredDeliveries]).
 */
class ElevatorFinale(
    private val requiredDeliveries: Int = DEFAULT_REQUIRED_DELIVERIES,
) {
    /** Доступен ли финальный выбор у лифта прямо сейчас. */
    fun isAvailable(state: GameState): Boolean =
        state.flag(FLAG_RUMOR) &&
            state.deliveredCount >= requiredDeliveries &&
            !isFinished(state)

    /** Сделан ли уже финальный выбор. */
    fun isFinished(state: GameState): Boolean = state.flag(FLAG_FINISHED)

    /** Применить выбор концовки: пометить игру завершённой и выставить флаг концовки. */
    fun choose(
        state: GameState,
        ending: Ending,
    ): GameState =
        state.copy(
            flags = state.flags + (FLAG_FINISHED to true) + (ending.flag to true),
        )

    companion object {
        /** Локация с финальным интерактивом. */
        const val FINALE_LOCATION_ID = "old-elevator"

        /** Слух о лифте (выставляется в диалоге с архивариусом). */
        const val FLAG_RUMOR = "knows_elevator_rumor"

        /** Игра завершена выбором концовки. */
        const val FLAG_FINISHED = "game_finished"

        /** Сколько писем нужно доставить, чтобы открыть финал. */
        const val DEFAULT_REQUIRED_DELIVERIES = 6
    }
}
