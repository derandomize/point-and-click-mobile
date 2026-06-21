package com.podzemnayapochta.presentation.letters

import com.podzemnayapochta.domain.model.LetterStatus
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class LetterBagLabelTest {
    @Test
    fun `каждый статус письма имеет метку для UI`() {
        assertEquals("Не получено", LetterStatus.LOCKED.bagLabel())
        assertEquals("Получено", LetterStatus.RECEIVED.bagLabel())
        assertEquals("В пути", LetterStatus.IN_TRANSIT.bagLabel())
        assertEquals("Доставлено", LetterStatus.DELIVERED.bagLabel())
    }
}
