package com.podzemnayapochta.data.dto

import kotlinx.serialization.Serializable

/**
 * Сериализуемый снимок динамической части GameState (см. SaveManager).
 * Письма хранятся как карта id -> статус; полные тексты берутся из контента
 * при восстановлении, поэтому сейв не дублирует контент и переживает его правки.
 */
@Serializable
data class SaveStateDto(
    val version: Int = CURRENT_VERSION,
    val currentLocationId: String,
    val letterStatuses: Map<String, String> = emptyMap(),
    val flags: Map<String, Boolean> = emptyMap(),
    val score: Int = 0,
    val unlockedLocationIds: List<String> = emptyList(),
) {
    companion object {
        const val CURRENT_VERSION = 1
    }
}
