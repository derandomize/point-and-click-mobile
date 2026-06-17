package com.podzemnayapochta.domain.usecase

import com.podzemnayapochta.domain.TestFixtures
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class MoveToTest {
    private val moveTo = MoveTo()

    @Test
    fun `переход в связанную локацию обновляет текущую и открывает её`() {
        val state = TestFixtures.state(currentLocationId = "post-office")
        val from = TestFixtures.location(id = "post-office", connected = listOf("market"))

        val result = moveTo(state, from = from, targetLocationId = "market")

        val success = assertIs<MoveResult.Success>(result)
        assertEquals("market", success.state.currentLocationId)
        assertTrue("market" in success.state.unlockedLocationIds)
    }

    @Test
    fun `переход в несвязанную локацию запрещён`() {
        val state = TestFixtures.state(currentLocationId = "post-office")
        val from = TestFixtures.location(id = "post-office", connected = listOf("market"))

        val result = moveTo(state, from = from, targetLocationId = "archive")

        assertEquals(MoveResult.NotConnected, result)
    }

    @Test
    fun `переход в ту же локацию возвращает SameLocation`() {
        val state = TestFixtures.state(currentLocationId = "post-office")
        val from = TestFixtures.location(id = "post-office", connected = listOf("market"))

        val result = moveTo(state, from = from, targetLocationId = "post-office")

        assertEquals(MoveResult.SameLocation, result)
    }
}
