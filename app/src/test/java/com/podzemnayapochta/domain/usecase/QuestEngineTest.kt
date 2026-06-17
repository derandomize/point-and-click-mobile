package com.podzemnayapochta.domain.usecase

import com.podzemnayapochta.domain.TestFixtures
import com.podzemnayapochta.domain.model.LetterStatus
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class QuestEngineTest {
    private val quests = QuestEngine()

    @Test
    fun `receiveLetter переводит LOCKED в RECEIVED`() {
        val locked = TestFixtures.letter(status = LetterStatus.LOCKED)
        val state = TestFixtures.state(letters = listOf(locked))

        val newState = quests.receiveLetter(state, "letter-1")

        assertEquals(LetterStatus.RECEIVED, newState.letter("letter-1")?.status)
    }

    @Test
    fun `receiveLetter не трогает письмо в другом статусе`() {
        val delivered = TestFixtures.letter(status = LetterStatus.DELIVERED)
        val state = TestFixtures.state(letters = listOf(delivered))

        val newState = quests.receiveLetter(state, "letter-1")

        assertEquals(LetterStatus.DELIVERED, newState.letter("letter-1")?.status)
    }

    @Test
    fun `markInTransit переводит RECEIVED в IN_TRANSIT`() {
        val received = TestFixtures.letter(status = LetterStatus.RECEIVED)
        val state = TestFixtures.state(letters = listOf(received))

        val newState = quests.markInTransit(state, "letter-1")

        assertEquals(LetterStatus.IN_TRANSIT, newState.letter("letter-1")?.status)
    }

    @Test
    fun `deliverableLetters возвращает только RECEIVED и IN_TRANSIT`() {
        val state =
            TestFixtures.state(
                letters =
                    listOf(
                        TestFixtures.letter(id = "a", status = LetterStatus.LOCKED),
                        TestFixtures.letter(id = "b", status = LetterStatus.RECEIVED),
                        TestFixtures.letter(id = "c", status = LetterStatus.IN_TRANSIT),
                        TestFixtures.letter(id = "d", status = LetterStatus.DELIVERED),
                    ),
            )

        val deliverable = quests.deliverableLetters(state).map { it.id }.toSet()

        assertEquals(setOf("b", "c"), deliverable)
    }
}
