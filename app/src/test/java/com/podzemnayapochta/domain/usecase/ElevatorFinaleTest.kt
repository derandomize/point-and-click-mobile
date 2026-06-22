package com.podzemnayapochta.domain.usecase

import com.podzemnayapochta.domain.model.Ending
import com.podzemnayapochta.domain.model.GameState
import com.podzemnayapochta.domain.model.Letter
import com.podzemnayapochta.domain.model.LetterStatus
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ElevatorFinaleTest {
    private val finale = ElevatorFinale(requiredDeliveries = 2)

    private fun stateWith(
        rumor: Boolean,
        delivered: Int,
        finished: Boolean = false,
    ): GameState {
        val letters =
            (1..3).associate { i ->
                val status = if (i <= delivered) LetterStatus.DELIVERED else LetterStatus.LOCKED
                "l$i" to Letter("l$i", "Письмо $i", "Текст", recipientNpcId = "npc", status = status)
            }
        val flags =
            buildMap {
                if (rumor) put(ElevatorFinale.FLAG_RUMOR, true)
                if (finished) put(ElevatorFinale.FLAG_FINISHED, true)
            }
        return GameState(currentLocationId = "old-elevator", letters = letters, flags = flags)
    }

    @Test
    fun `недоступен без слуха о лифте`() {
        assertFalse(finale.isAvailable(stateWith(rumor = false, delivered = 3)))
    }

    @Test
    fun `недоступен при нехватке доставленных писем`() {
        assertFalse(finale.isAvailable(stateWith(rumor = true, delivered = 1)))
    }

    @Test
    fun `доступен при слухе и достаточном числе доставок`() {
        assertTrue(finale.isAvailable(stateWith(rumor = true, delivered = 2)))
    }

    @Test
    fun `недоступен, если игра уже завершена`() {
        assertFalse(finale.isAvailable(stateWith(rumor = true, delivered = 3, finished = true)))
    }

    @Test
    fun `выбор концовки помечает игру завершённой и ставит флаг концовки`() {
        val start = stateWith(rumor = true, delivered = 2)

        val open = finale.choose(start, Ending.OPEN_PATH)
        assertTrue(open.flag(ElevatorFinale.FLAG_FINISHED))
        assertTrue(open.flag(Ending.OPEN_PATH.flag))
        assertFalse(open.flag(Ending.KEEP_SECRET.flag))

        val secret = finale.choose(start, Ending.KEEP_SECRET)
        assertTrue(secret.flag(ElevatorFinale.FLAG_FINISHED))
        assertTrue(secret.flag(Ending.KEEP_SECRET.flag))
    }
}
