package com.podzemnayapochta.domain.model

/**
 * Житель подземного города — адресат или отправитель писем (см. docs/idea.md).
 */
data class Npc(
    val id: String,
    val name: String,
    val portraitAsset: String,
    /** Локация, где обычно находится NPC. */
    val locationId: String,
    /** Корневой узел диалога с этим NPC. */
    val dialogueRootId: String? = null,
)
