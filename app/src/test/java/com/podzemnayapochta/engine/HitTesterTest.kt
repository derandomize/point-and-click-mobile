package com.podzemnayapochta.engine

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HitTesterTest {
    private val tester = HitTester()

    private val areas =
        listOf(
            HitArea("a", left = 0.0f, top = 0.0f, right = 0.4f, bottom = 0.4f),
            HitArea("b", left = 0.5f, top = 0.5f, right = 1.0f, bottom = 1.0f),
        )

    @Test
    fun `тап внутри области возвращает её`() {
        assertEquals("a", tester.hitTest(areas, 0.1f, 0.1f)?.id)
        assertEquals("b", tester.hitTest(areas, 0.8f, 0.8f)?.id)
    }

    @Test
    fun `тап мимо всех областей возвращает null`() {
        assertNull(tester.hitTest(areas, 0.45f, 0.45f))
    }

    @Test
    fun `при перекрытии возвращается верхняя область`() {
        val overlapping =
            listOf(
                HitArea("bottom", 0.0f, 0.0f, 1.0f, 1.0f),
                HitArea("top", 0.2f, 0.2f, 0.8f, 0.8f),
            )
        assertEquals("top", tester.hitTest(overlapping, 0.5f, 0.5f)?.id)
    }

    @Test
    fun `contains проверяет границы области`() {
        val area = HitArea("x", 0.1f, 0.1f, 0.3f, 0.3f)
        assertTrue(area.contains(0.2f, 0.2f))
        assertFalse(area.contains(0.05f, 0.2f))
    }

    @Test
    fun `некорректные границы бросают исключение`() {
        assertFailsWith<IllegalArgumentException> {
            HitArea("bad", left = 0.5f, top = 0.5f, right = 0.4f, bottom = 0.6f)
        }
    }

    @Test
    fun `координаты вне диапазона бросают исключение`() {
        assertFailsWith<IllegalArgumentException> {
            HitArea("oob", left = -0.1f, top = 0.0f, right = 0.4f, bottom = 0.4f)
        }
    }
}
