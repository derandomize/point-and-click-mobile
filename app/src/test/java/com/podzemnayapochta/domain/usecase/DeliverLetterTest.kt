package com.podzemnayapochta.domain.usecase

import com.podzemnayapochta.domain.TestFixtures
import com.podzemnayapochta.domain.model.LetterStatus
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class DeliverLetterTest {
    private val deliver = DeliverLetter()

    @Test
    fun `доставка верному адресату начисляет награду и помечает доставленным`() {
        val state = TestFixtures.state(letters = listOf(TestFixtures.letter(reward = 25)))

        val result = deliver(state, letterId = "letter-1", recipientNpcId = "npc-clockmaker")

        val success = assertIs<DeliverResult.Success>(result)
        assertEquals(25, success.reward)
        assertEquals(25, success.state.score)
        assertEquals(LetterStatus.DELIVERED, success.state.letter("letter-1")?.status)
        assertTrue(success.state.letter("letter-1")?.isDelivered == true)
    }

    @Test
    fun `доставка не тому адресату возвращает WrongRecipient`() {
        val state = TestFixtures.state()

        val result = deliver(state, letterId = "letter-1", recipientNpcId = "npc-bartender")

        assertEquals(DeliverResult.WrongRecipient, result)
    }

    @Test
    fun `несуществующее письмо возвращает LetterNotFound`() {
        val state = TestFixtures.state()

        val result = deliver(state, letterId = "missing", recipientNpcId = "npc-clockmaker")

        assertEquals(DeliverResult.LetterNotFound, result)
    }

    @Test
    fun `повторная доставка возвращает AlreadyDelivered`() {
        val delivered = TestFixtures.letter(status = LetterStatus.DELIVERED)
        val state = TestFixtures.state(letters = listOf(delivered))

        val result = deliver(state, letterId = "letter-1", recipientNpcId = "npc-clockmaker")

        assertEquals(DeliverResult.AlreadyDelivered, result)
    }

    @Test
    fun `исходное состояние не мутируется`() {
        val state = TestFixtures.state(letters = listOf(TestFixtures.letter(reward = 10)))

        deliver(state, letterId = "letter-1", recipientNpcId = "npc-clockmaker")

        assertEquals(0, state.score)
        assertEquals(LetterStatus.RECEIVED, state.letter("letter-1")?.status)
    }
}
