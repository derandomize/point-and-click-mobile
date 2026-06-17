package com.podzemnayapochta.domain.usecase

import com.podzemnayapochta.domain.model.GameState
import com.podzemnayapochta.domain.model.Location

/**
 * Результат перемещения между локациями.
 */
sealed interface MoveResult {
    data class Success(
        val state: GameState,
    ) : MoveResult

    data object NotConnected : MoveResult

    data object SameLocation : MoveResult
}

/**
 * Use-case: переместить игрока в соседнюю локацию.
 * Переход разрешён только в локацию, связанную с текущей.
 */
class MoveTo {
    operator fun invoke(
        state: GameState,
        from: Location,
        targetLocationId: String,
    ): MoveResult {
        if (targetLocationId == state.currentLocationId) return MoveResult.SameLocation
        if (targetLocationId !in from.connectedLocationIds) return MoveResult.NotConnected

        val newState =
            state.copy(
                currentLocationId = targetLocationId,
                unlockedLocationIds = state.unlockedLocationIds + targetLocationId,
            )
        return MoveResult.Success(newState)
    }
}
