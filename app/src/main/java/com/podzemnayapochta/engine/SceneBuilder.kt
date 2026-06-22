package com.podzemnayapochta.engine

import com.podzemnayapochta.domain.model.Location
import com.podzemnayapochta.domain.repository.GameContent

/**
 * Собирает [Scene] для локации из загруженного [GameContent]:
 * NPC размещаются вдоль нижней части сцены, выходы — по верхним углам.
 * Координаты детерминированы, чтобы рендер и тесты были стабильны
 * (см. docs/architecture.md, SceneRenderer).
 */
class SceneBuilder {
    fun build(
        content: GameContent,
        locationId: String,
        includeFinaleHotspot: Boolean = false,
    ): Scene? {
        val location = content.location(locationId) ?: return null
        val npcObjects = buildNpcObjects(content, location)
        val exitObjects = buildExitObjects(content, location)
        val hotspots = if (includeFinaleHotspot) listOf(finaleHotspot()) else emptyList()
        return Scene(
            locationId = location.id,
            title = location.title,
            backgroundAsset = location.backgroundAsset,
            objects = npcObjects + exitObjects + hotspots,
        )
    }

    /** Сюжетная точка интереса по центру сцены (например, рычаг лифта в финале). */
    private fun finaleHotspot(): SceneObject =
        SceneObject(
            kind = SceneObjectKind.HOTSPOT,
            label = "Лифт наверх",
            area =
                HitArea(
                    id = HOTSPOT_FINALE_ID,
                    left = 0.5f - HOTSPOT_HALF_W,
                    top = HOTSPOT_TOP,
                    right = 0.5f + HOTSPOT_HALF_W,
                    bottom = HOTSPOT_BOTTOM,
                    payload = HOTSPOT_FINALE_PAYLOAD,
                ),
        )

    private fun buildNpcObjects(
        content: GameContent,
        location: Location,
    ): List<SceneObject> {
        val npcs = location.npcIds.mapNotNull { content.npc(it) }
        if (npcs.isEmpty()) return emptyList()
        val step = 1f / (npcs.size + 1)
        return npcs.mapIndexed { index, npc ->
            val cx = step * (index + 1)
            SceneObject(
                kind = SceneObjectKind.NPC,
                label = npc.name,
                area =
                    HitArea(
                        id = "npc:${npc.id}",
                        left = (cx - NPC_HALF_W).coerceIn(0f, 1f),
                        top = NPC_TOP,
                        right = (cx + NPC_HALF_W).coerceIn(0f, 1f),
                        bottom = NPC_BOTTOM,
                        payload = npc.id,
                    ),
                imageAsset = npc.portraitAsset,
            )
        }
    }

    private fun buildExitObjects(
        content: GameContent,
        location: Location,
    ): List<SceneObject> {
        val exits = location.connectedLocationIds.mapNotNull { content.location(it) }
        if (exits.isEmpty()) return emptyList()
        val step = 1f / (exits.size + 1)
        return exits.mapIndexed { index, target ->
            val cx = step * (index + 1)
            SceneObject(
                kind = SceneObjectKind.EXIT,
                label = target.title,
                area =
                    HitArea(
                        id = "exit:${target.id}",
                        left = (cx - EXIT_HALF_W).coerceIn(0f, 1f),
                        top = EXIT_TOP,
                        right = (cx + EXIT_HALF_W).coerceIn(0f, 1f),
                        bottom = EXIT_BOTTOM,
                        payload = target.id,
                    ),
            )
        }
    }

    companion object {
        /** Id hit-области финального интерактива. */
        const val HOTSPOT_FINALE_ID = "hotspot:finale"

        /** Полезная нагрузка финального интерактива (см. LocationScreen). */
        const val HOTSPOT_FINALE_PAYLOAD = "finale"

        private const val NPC_HALF_W = 0.09f
        private const val NPC_TOP = 0.55f
        private const val NPC_BOTTOM = 0.9f

        private const val EXIT_HALF_W = 0.1f
        private const val EXIT_TOP = 0.05f
        private const val EXIT_BOTTOM = 0.2f

        private const val HOTSPOT_HALF_W = 0.18f
        private const val HOTSPOT_TOP = 0.32f
        private const val HOTSPOT_BOTTOM = 0.48f
    }
}
