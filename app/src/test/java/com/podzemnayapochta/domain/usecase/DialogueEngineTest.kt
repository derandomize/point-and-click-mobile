package com.podzemnayapochta.domain.usecase

import com.podzemnayapochta.domain.TestFixtures
import com.podzemnayapochta.domain.model.DialogueChoice
import com.podzemnayapochta.domain.model.DialogueCondition
import com.podzemnayapochta.domain.model.DialogueEffect
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DialogueEngineTest {
    private val engine = DialogueEngine()

    @Test
    fun `варианты без условий всегда доступны`() {
        val choices = listOf(DialogueChoice("Привет", targetNodeId = "n2"))

        val available = engine.availableChoices(choices, TestFixtures.state())

        assertEquals(1, available.size)
    }

    @Test
    fun `вариант с невыполненным условием скрыт`() {
        val choices =
            listOf(
                DialogueChoice(
                    text = "Открыть тайну лифта",
                    targetNodeId = "secret",
                    condition = DialogueCondition(flag = "knows_secret", expectedValue = true),
                ),
            )

        val available = engine.availableChoices(choices, TestFixtures.state())

        assertTrue(available.isEmpty())
    }

    @Test
    fun `вариант с выполненным условием доступен`() {
        val choices =
            listOf(
                DialogueChoice(
                    text = "Открыть тайну лифта",
                    targetNodeId = "secret",
                    condition = DialogueCondition(flag = "knows_secret", expectedValue = true),
                ),
            )
        val state = TestFixtures.state(flags = mapOf("knows_secret" to true))

        val available = engine.availableChoices(choices, state)

        assertEquals(1, available.size)
    }

    @Test
    fun `выбор применяет эффекты к флагам состояния`() {
        val choice =
            DialogueChoice(
                text = "Я узнал про лифт",
                targetNodeId = "n3",
                effects = listOf(DialogueEffect(flag = "knows_secret", value = true)),
            )

        val newState = engine.applyChoice(TestFixtures.state(), choice)

        assertTrue(newState.flag("knows_secret"))
    }
}
