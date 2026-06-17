package com.podzemnayapochta.domain.model

/**
 * Условие показа реплики/перехода — проверка флага в [GameState] (см. DialogueEngine).
 */
data class DialogueCondition(
    val flag: String,
    val expectedValue: Boolean = true,
)

/**
 * Эффект выбора реплики: устанавливает флаг в [GameState].
 */
data class DialogueEffect(
    val flag: String,
    val value: Boolean = true,
)

/**
 * Вариант ответа игрока, ведущий к следующему узлу диалога.
 */
data class DialogueChoice(
    val text: String,
    val targetNodeId: String?,
    /** Если задано — вариант доступен только при выполнении условия. */
    val condition: DialogueCondition? = null,
    /** Эффекты, применяемые при выборе этого варианта. */
    val effects: List<DialogueEffect> = emptyList(),
)

/**
 * Узел дерева диалога (см. docs/architecture.md, DialogueEngine).
 */
data class DialogueNode(
    val id: String,
    /** Кто говорит: id NPC или null для рассказчика. */
    val speakerNpcId: String?,
    val text: String,
    val choices: List<DialogueChoice> = emptyList(),
) {
    val isTerminal: Boolean get() = choices.isEmpty()
}
