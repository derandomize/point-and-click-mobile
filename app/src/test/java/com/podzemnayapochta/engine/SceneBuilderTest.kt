package com.podzemnayapochta.engine

import com.podzemnayapochta.domain.model.Location
import com.podzemnayapochta.domain.model.Npc
import com.podzemnayapochta.domain.repository.GameContent
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SceneBuilderTest {
    private val builder = SceneBuilder()

    private val content =
        GameContent(
            startLocationId = "post-office",
            locations =
                listOf(
                    Location(
                        id = "post-office",
                        title = "Почта",
                        description = "",
                        backgroundAsset = "bg.png",
                        connectedLocationIds = listOf("market", "clock-house"),
                        npcIds = listOf("npc-pm"),
                    ),
                    Location("market", "Рынок", "", "m.png"),
                    Location("clock-house", "Часовщик", "", "c.png"),
                ),
            npcs = listOf(Npc("npc-pm", "Начальник", "p.png", "post-office")),
            letters = emptyList(),
            dialogues = emptyList(),
        )

    @Test
    fun `сцена содержит NPC и выходы локации`() {
        val scene = builder.build(content, "post-office")!!

        val npcs = scene.objects.filter { it.kind == SceneObjectKind.NPC }
        val exits = scene.objects.filter { it.kind == SceneObjectKind.EXIT }

        assertEquals(1, npcs.size)
        assertEquals("npc-pm", npcs.first().payload)
        assertEquals(2, exits.size)
        assertTrue(exits.map { it.payload }.containsAll(listOf("market", "clock-house")))
    }

    @Test
    fun `id областей различают NPC и выходы`() {
        val scene = builder.build(content, "post-office")!!

        assertTrue(scene.hitAreas.any { it.id == "npc:npc-pm" })
        assertTrue(scene.hitAreas.any { it.id == "exit:market" })
    }

    @Test
    fun `несуществующая локация возвращает null`() {
        assertNull(builder.build(content, "nowhere"))
    }

    @Test
    fun `по умолчанию сцена без финального интерактива`() {
        val scene = builder.build(content, "post-office")!!

        assertTrue(scene.objects.none { it.kind == SceneObjectKind.HOTSPOT })
    }

    @Test
    fun `при includeFinaleHotspot добавляется HOTSPOT`() {
        val scene = builder.build(content, "post-office", includeFinaleHotspot = true)!!

        val hotspots = scene.objects.filter { it.kind == SceneObjectKind.HOTSPOT }
        assertEquals(1, hotspots.size)
        assertEquals(SceneBuilder.HOTSPOT_FINALE_PAYLOAD, hotspots.first().payload)
    }

    @Test
    fun `hit-области сцены не перекрывают NPC и выходы по вертикали`() {
        val scene = builder.build(content, "post-office")!!
        val npc = scene.objects.first { it.kind == SceneObjectKind.NPC }.area
        val exit = scene.objects.first { it.kind == SceneObjectKind.EXIT }.area

        // выходы сверху, NPC снизу
        assertTrue(exit.bottom <= npc.top)
    }
}
