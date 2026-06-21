package com.podzemnayapochta.presentation.game

import com.podzemnayapochta.domain.model.DialogueChoice
import com.podzemnayapochta.domain.model.DialogueNode

/**
 * Состояние активного диалога для UI: текущий узел, имя говорящего
 * и список доступных (прошедших условия) вариантов ответа.
 */
data class DialogueUiState(
    val npcId: String,
    val node: DialogueNode,
    val speakerName: String?,
    val availableChoices: List<DialogueChoice>,
)
