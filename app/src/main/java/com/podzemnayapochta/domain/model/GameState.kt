package com.podzemnayapochta.domain.model

/**
 * Единое immutable-состояние игры (см. docs/architecture.md).
 * Обновляется только через use-case-ы, возвращающие новый экземпляр.
 */
data class GameState(
    /** Локация, где сейчас находится игрок. */
    val currentLocationId: String,
    /** Состояния всех писем по их id. */
    val letters: Map<String, Letter> = emptyMap(),
    /** Сюжетные флаги (для условий диалогов и ветвлений). */
    val flags: Map<String, Boolean> = emptyMap(),
    /** Набранные очки/награда. */
    val score: Int = 0,
    /** Открытые (посещённые/доступные) локации. */
    val unlockedLocationIds: Set<String> = emptySet(),
) {
    fun flag(name: String): Boolean = flags[name] ?: false

    fun letter(id: String): Letter? = letters[id]

    val deliveredCount: Int get() = letters.values.count { it.isDelivered }
}
