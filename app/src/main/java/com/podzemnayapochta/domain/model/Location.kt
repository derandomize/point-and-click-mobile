package com.podzemnayapochta.domain.model

/**
 * Локация подземного города (см. docs/idea.md).
 * «Открытка» с фоном и кликабельными объектами.
 */
data class Location(
    val id: String,
    val title: String,
    val description: String,
    val backgroundAsset: String,
    /** Идентификаторы локаций, в которые можно перейти отсюда. */
    val connectedLocationIds: List<String> = emptyList(),
    /** NPC, находящиеся в этой локации. */
    val npcIds: List<String> = emptyList(),
)
