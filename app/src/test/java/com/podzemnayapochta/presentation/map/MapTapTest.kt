package com.podzemnayapochta.presentation.map

import com.podzemnayapochta.engine.HitTester
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MapTapTest {
    private val hitTester = HitTester()

    private val locations =
        listOf(
            MapLocation("a", "A", x = 0.3f, y = 0.3f, unlocked = true),
            MapLocation("b", "B", x = 0.7f, y = 0.7f, unlocked = false),
        )

    @Test
    fun `тап по открытой локации возвращает её id`() {
        assertEquals("a", resolveMapTap(locations, hitTester, nx = 0.3f, ny = 0.3f))
    }

    @Test
    fun `тап по запертой локации недоступен`() {
        assertNull(resolveMapTap(locations, hitTester, nx = 0.7f, ny = 0.7f))
    }

    @Test
    fun `тап по пустому месту возвращает null`() {
        assertNull(resolveMapTap(locations, hitTester, nx = 0.95f, ny = 0.05f))
    }
}
