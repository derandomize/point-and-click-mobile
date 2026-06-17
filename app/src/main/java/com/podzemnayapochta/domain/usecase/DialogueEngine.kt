package com.podzemnayapochta.domain.usecase

import com.podzemnayapochta.domain.model.DialogueChoice
import com.podzemnayapochta.domain.model.DialogueCondition
import com.podzemnayapochta.domain.model.GameState

/**
 * Движок диалогов: фильтрует доступные варианты по флагам [GameState]
 * и применяет эффекты выбора (см. docs/architecture.md, DialogueEngine).
 */
class DialogueEngine {
    /** Возвращает только те варианты, условия которых выполнены. */
    fun availableChoices(
        choices: List<DialogueChoice>,
        state: GameState,
    ): List<DialogueChoice> = choices.filter { isConditionMet(it.condition, state) }

    /** Применяет эффекты выбранного варианта к состоянию. */
    fun applyChoice(
        state: GameState,
        choice: DialogueChoice,
    ): GameState {
        if (choice.effects.isEmpty()) return state
        val updatedFlags = state.flags.toMutableMap()
        choice.effects.forEach { effect -> updatedFlags[effect.flag] = effect.value }
        return state.copy(flags = updatedFlags)
    }

    private fun isConditionMet(
        condition: DialogueCondition?,
        state: GameState,
    ): Boolean {
        if (condition == null) return true
        return state.flag(condition.flag) == condition.expectedValue
    }
}
