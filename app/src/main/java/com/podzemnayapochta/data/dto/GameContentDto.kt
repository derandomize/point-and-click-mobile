package com.podzemnayapochta.data.dto

import kotlinx.serialization.Serializable

/**
 * Корневой контейнер контента игры, загружаемый из assets/content/game.json.
 * См. docs/architecture.md — контент-пайплайн.
 */
@Serializable
data class GameContentDto(
    val locations: List<LocationDto> = emptyList(),
    val npcs: List<NpcDto> = emptyList(),
    val letters: List<LetterDto> = emptyList(),
    val dialogues: List<DialogueNodeDto> = emptyList(),
    val startLocationId: String,
)

@Serializable
data class LocationDto(
    val id: String,
    val title: String,
    val description: String = "",
    val backgroundAsset: String,
    val connectedLocationIds: List<String> = emptyList(),
    val npcIds: List<String> = emptyList(),
)

@Serializable
data class NpcDto(
    val id: String,
    val name: String,
    val portraitAsset: String,
    val locationId: String,
    val dialogueRootId: String? = null,
)

@Serializable
data class LetterDto(
    val id: String,
    val title: String,
    val body: String,
    val recipientNpcId: String,
    val hints: List<String> = emptyList(),
    val reward: Int = 0,
)

@Serializable
data class DialogueNodeDto(
    val id: String,
    val speakerNpcId: String? = null,
    val text: String,
    val choices: List<DialogueChoiceDto> = emptyList(),
)

@Serializable
data class DialogueChoiceDto(
    val text: String,
    val targetNodeId: String? = null,
    val condition: DialogueConditionDto? = null,
    val effects: List<DialogueEffectDto> = emptyList(),
)

@Serializable
data class DialogueConditionDto(
    val flag: String,
    val expectedValue: Boolean = true,
)

@Serializable
data class DialogueEffectDto(
    val flag: String,
    val value: Boolean = true,
)
