package com.podzemnayapochta.data.mapper

import com.podzemnayapochta.data.dto.SaveStateDto
import com.podzemnayapochta.domain.model.GameState
import com.podzemnayapochta.domain.model.LetterStatus
import com.podzemnayapochta.domain.repository.GameContent

/** Мапперы между [GameState] и сохраняемым [SaveStateDto]. */

fun GameState.toSaveDto(): SaveStateDto =
    SaveStateDto(
        currentLocationId = currentLocationId,
        letterStatuses = letters.mapValues { (_, letter) -> letter.status.name },
        flags = flags,
        score = score,
        unlockedLocationIds = unlockedLocationIds.toList(),
    )

/**
 * Восстанавливает [GameState], накладывая сейв на актуальный [content]:
 * письма берутся из контента (полные тексты), статус — из сейва,
 * неизвестные id (например, новые письма) остаются [LetterStatus.LOCKED].
 */
fun SaveStateDto.toGameState(content: GameContent): GameState {
    val letters =
        content.letters.associate { letter ->
            val status = letterStatuses[letter.id]?.let(::parseStatus) ?: LetterStatus.LOCKED
            letter.id to letter.copy(status = status)
        }
    return GameState(
        currentLocationId = currentLocationId,
        letters = letters,
        flags = flags,
        score = score,
        unlockedLocationIds = unlockedLocationIds.toSet(),
    )
}

private fun parseStatus(raw: String): LetterStatus = runCatching { LetterStatus.valueOf(raw) }.getOrDefault(LetterStatus.LOCKED)
