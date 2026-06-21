package com.podzemnayapochta.presentation.game

import com.podzemnayapochta.domain.model.DialogueChoice
import com.podzemnayapochta.domain.model.DialogueNode
import com.podzemnayapochta.domain.model.Letter

/**
 * Состояние активного диалога для UI: текущий узел, имя говорящего,
 * список доступных (прошедших условия) вариантов ответа и — если для этого
 * NPC есть письмо к доставке — само письмо (для кнопки «Вручить письмо»).
 */
data class DialogueUiState(
    val npcId: String,
    val node: DialogueNode,
    val speakerName: String?,
    val availableChoices: List<DialogueChoice>,
    val deliverableLetter: Letter? = null,
)
