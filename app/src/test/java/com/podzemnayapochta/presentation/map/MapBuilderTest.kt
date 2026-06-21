package com.podzemnayapochta.presentation.map

import com.podzemnayapochta.domain.model.Location
import com.podzemnayapochta.domain.repository.GameContent
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MapBuilderTest {
    private val builder = MapBuilder()

    private fun content(vararg ids: String) =
        GameContent(
            startLocationId = ids.first(),
            locations = ids.map { Location(it, it.uppercase(), "", "$it.png", connectedLocationIds = emptyList()) },
            npcs = emptyList(),
            letters = emptyList(),
            dialogues = emptyList(),
        )

    @Test
    fun `карта строится из всех локаций контента`() {
        val nodes = builder.build(content("a", "b", "c"), unlockedLocationIds = setOf("a"))

        assertEquals(listOf("a", "b", "c"), nodes.map { it.id })
    }

    @Test
    fun `unlocked отражает множество открытых локаций`() {
        val nodes = builder.build(content("a", "b", "c"), unlockedLocationIds = setOf("a", "c"))

        assertTrue(nodes.first { it.id == "a" }.unlocked)
        assertFalse(nodes.first { it.id == "b" }.unlocked)
        assertTrue(nodes.first { it.id == "c" }.unlocked)
    }

    @Test
    fun `координаты узлов лежат внутри диапазона 0 1`() {
        val nodes = builder.build(content("a", "b", "c", "d", "e", "f"), unlockedLocationIds = emptySet())

        assertTrue(nodes.all { it.x > 0f && it.x < 1f && it.y > 0f && it.y < 1f })
    }

    @Test
    fun `связи переносятся из контента`() {
        val withEdges =
            GameContent(
                startLocationId = "a",
                locations =
                    listOf(
                        Location("a", "A", "", "a.png", connectedLocationIds = listOf("b")),
                        Location("b", "B", "", "b.png", connectedLocationIds = listOf("a")),
                    ),
                npcs = emptyList(),
                letters = emptyList(),
                dialogues = emptyList(),
            )

        val nodes = builder.build(withEdges, unlockedLocationIds = setOf("a"))

        assertEquals(listOf("b"), nodes.first { it.id == "a" }.connectedIds)
    }
}
