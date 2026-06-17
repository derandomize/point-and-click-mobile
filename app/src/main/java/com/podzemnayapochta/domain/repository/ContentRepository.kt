package com.podzemnayapochta.domain.repository

import com.podzemnayapochta.domain.model.DialogueNode
import com.podzemnayapochta.domain.model.Letter
import com.podzemnayapochta.domain.model.Location
import com.podzemnayapochta.domain.model.Npc

/**
 * Загруженный контент игры (см. docs/architecture.md — контент-пайплайн).
 */
data class GameContent(
    val locations: List<Location>,
    val npcs: List<Npc>,
    val letters: List<Letter>,
    val dialogues: List<DialogueNode>,
    val startLocationId: String,
) {
    fun location(id: String): Location? = locations.firstOrNull { it.id == id }

    fun npc(id: String): Npc? = npcs.firstOrNull { it.id == id }

    fun dialogue(id: String): DialogueNode? = dialogues.firstOrNull { it.id == id }
}

/**
 * Источник игрового контента. Реализация читает JSON из assets.
 */
interface ContentRepository {
    suspend fun loadContent(): GameContent
}
