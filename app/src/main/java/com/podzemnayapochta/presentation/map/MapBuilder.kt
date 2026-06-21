package com.podzemnayapochta.presentation.map

import com.podzemnayapochta.domain.repository.GameContent
import kotlin.math.ceil
import kotlin.math.sqrt

/**
 * Собирает узлы карты из [GameContent] (см. ROADMAP, PR 4).
 * Локации раскладываются по детерминированной сетке в нормализованных
 * координатах [0..1], связи берутся из контента, флаг [MapLocation.unlocked]
 * выставляется по множеству открытых локаций.
 */
class MapBuilder {
    fun build(
        content: GameContent,
        unlockedLocationIds: Set<String>,
    ): List<MapLocation> {
        val locations = content.locations
        if (locations.isEmpty()) return emptyList()

        val columns = ceil(sqrt(locations.size.toFloat())).toInt()
        val rows = ceil(locations.size.toFloat() / columns).toInt()

        return locations.mapIndexed { index, location ->
            val column = index % columns
            val row = index / columns
            MapLocation(
                id = location.id,
                title = location.title,
                x = (column + 1f) / (columns + 1f),
                y = (row + 1f) / (rows + 1f),
                connectedIds = location.connectedLocationIds,
                unlocked = location.id in unlockedLocationIds,
            )
        }
    }
}
