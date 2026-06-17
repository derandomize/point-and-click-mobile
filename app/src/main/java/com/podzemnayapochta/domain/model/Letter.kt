package com.podzemnayapochta.domain.model

/**
 * Состояние письма-квеста (см. docs/architecture.md, QuestEngine).
 */
enum class LetterStatus {
    /** Ещё не выдано игроку. */
    LOCKED,

    /** Получено на почтовой станции, ждёт доставки. */
    RECEIVED,

    /** В пути — игрок определил адресата, но ещё не доставил. */
    IN_TRANSIT,

    /** Доставлено адресату. */
    DELIVERED,
}

/**
 * Письмо-квест. Игрок по подсказкам в тексте определяет адресата
 * и доставляет письмо нужному NPC.
 */
data class Letter(
    val id: String,
    val title: String,
    val body: String,
    /** Идентификатор NPC-адресата (разгадка квеста). */
    val recipientNpcId: String,
    /** Подсказки, помогающие определить адресата. */
    val hints: List<String> = emptyList(),
    /** Награда за доставку (очки/валюта). */
    val reward: Int = 0,
    val status: LetterStatus = LetterStatus.LOCKED,
) {
    val isDelivered: Boolean get() = status == LetterStatus.DELIVERED
}
