package com.podzemnayapochta.presentation.map

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MapLocationTest {
    @Test
    fun `toHitArea центрируется вокруг узла`() {
        val loc = MapLocation("post-office", "Почта", x = 0.5f, y = 0.5f)
        val area = loc.toHitArea(half = 0.1f)

        assertEquals("post-office", area.id)
        assertTrue(area.contains(0.5f, 0.5f))
        assertEquals(0.4f, area.left, 0.0001f)
        assertEquals(0.6f, area.right, 0.0001f)
    }

    @Test
    fun `hit-области у края обрезаются до диапазона`() {
        val loc = MapLocation("edge", "Край", x = 0.02f, y = 0.02f)
        val area = loc.toHitArea(half = 0.08f)

        assertTrue(area.left >= 0f)
        assertTrue(area.top >= 0f)
    }

    @Test
    fun `плейсхолдер-город содержит все локации из idea`() {
        val ids = MapLocation.placeholderCity().map { it.id }.toSet()

        assertTrue("post-office" in ids)
        assertTrue("old-elevator" in ids)
        assertEquals(6, ids.size)
    }
}
