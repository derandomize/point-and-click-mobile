package com.podzemnayapochta.data.mapper

import com.podzemnayapochta.data.dto.DialogueChoiceDto
import com.podzemnayapochta.data.dto.DialogueNodeDto
import com.podzemnayapochta.data.dto.LetterDto
import com.podzemnayapochta.data.dto.LocationDto
import com.podzemnayapochta.data.dto.NpcDto
import com.podzemnayapochta.domain.model.DialogueChoice
import com.podzemnayapochta.domain.model.DialogueCondition
import com.podzemnayapochta.domain.model.DialogueEffect
import com.podzemnayapochta.domain.model.DialogueNode
import com.podzemnayapochta.domain.model.Letter
import com.podzemnayapochta.domain.model.LetterStatus
import com.podzemnayapochta.domain.model.Location
import com.podzemnayapochta.domain.model.Npc

/** Мапперы из data-DTO в domain-модели. */

fun LocationDto.toDomain(): Location =
    Location(
        id = id,
        title = title,
        description = description,
        backgroundAsset = backgroundAsset,
        connectedLocationIds = connectedLocationIds,
        npcIds = npcIds,
    )

fun NpcDto.toDomain(): Npc =
    Npc(
        id = id,
        name = name,
        portraitAsset = portraitAsset,
        locationId = locationId,
        dialogueRootId = dialogueRootId,
    )

fun LetterDto.toDomain(): Letter =
    Letter(
        id = id,
        title = title,
        body = body,
        recipientNpcId = recipientNpcId,
        hints = hints,
        reward = reward,
        status = LetterStatus.LOCKED,
    )

fun DialogueNodeDto.toDomain(): DialogueNode =
    DialogueNode(
        id = id,
        speakerNpcId = speakerNpcId,
        text = text,
        choices = choices.map { it.toDomain() },
    )

fun DialogueChoiceDto.toDomain(): DialogueChoice =
    DialogueChoice(
        text = text,
        targetNodeId = targetNodeId,
        condition = condition?.let { DialogueCondition(it.flag, it.expectedValue) },
        effects = effects.map { DialogueEffect(it.flag, it.value) },
    )
