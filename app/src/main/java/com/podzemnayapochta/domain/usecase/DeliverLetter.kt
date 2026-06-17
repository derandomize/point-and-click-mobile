package com.podzemnayapochta.domain.usecase

import com.podzemnayapochta.domain.model.GameState
import com.podzemnayapochta.domain.model.Letter
import com.podzemnayapochta.domain.model.LetterStatus

/**
 * Результат попытки доставить письмо.
 */
sealed interface DeliverResult {
    data class Success(
        val state: GameState,
        val reward: Int,
    ) : DeliverResult

    data object LetterNotFound : DeliverResult

    data object AlreadyDelivered : DeliverResult

    data object WrongRecipient : DeliverResult
}

/**
 * Use-case: доставить письмо NPC в текущем взаимодействии.
 * Проверяет адресата, обновляет статус письма и начисляет награду.
 */
class DeliverLetter {
    operator fun invoke(
        state: GameState,
        letterId: String,
        recipientNpcId: String,
    ): DeliverResult {
        val letter: Letter = state.letter(letterId) ?: return DeliverResult.LetterNotFound

        if (letter.isDelivered) return DeliverResult.AlreadyDelivered
        if (letter.recipientNpcId != recipientNpcId) return DeliverResult.WrongRecipient

        val delivered = letter.copy(status = LetterStatus.DELIVERED)
        val newState =
            state.copy(
                letters = state.letters + (letterId to delivered),
                score = state.score + letter.reward,
            )
        return DeliverResult.Success(newState, letter.reward)
    }
}
