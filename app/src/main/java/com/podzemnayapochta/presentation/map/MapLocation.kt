package com.podzemnayapochta.presentation.map

import com.podzemnayapochta.engine.HitArea

/**
 * Узел локации на карте в нормализованных координатах [0..1].
 */
data class MapLocation(
    val id: String,
    val title: String,
    val x: Float,
    val y: Float,
    val connectedIds: List<String> = emptyList(),
) {
    /** Квадратная hit-область вокруг центра узла. */
    fun toHitArea(half: Float = HIT_HALF): HitArea =
        HitArea(
            id = id,
            left = (x - half).coerceIn(0f, 1f),
            top = (y - half).coerceIn(0f, 1f),
            right = (x + half).coerceIn(0f, 1f),
            bottom = (y + half).coerceIn(0f, 1f),
            payload = id,
        )

    companion object {
        const val HIT_HALF = 0.08f

        /** Плейсхолдер-расположение локаций (см. docs/idea.md). */
        fun placeholderCity(): List<MapLocation> =
            listOf(
                MapLocation("post-office", "Почта", 0.5f, 0.2f, listOf("market", "clock-house")),
                MapLocation("market", "Рынок", 0.25f, 0.45f, listOf("post-office", "tavern")),
                MapLocation("clock-house", "Часовщик", 0.75f, 0.45f, listOf("post-office", "old-elevator")),
                MapLocation("tavern", "Таверна", 0.3f, 0.72f, listOf("market", "archive")),
                MapLocation("archive", "Архив", 0.55f, 0.85f, listOf("tavern")),
                MapLocation("old-elevator", "Лифт", 0.82f, 0.72f, listOf("clock-house")),
            )
    }
}
