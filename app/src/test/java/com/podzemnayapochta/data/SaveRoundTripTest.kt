package com.podzemnayapochta.data

import com.podzemnayapochta.data.dto.SaveStateDto
import com.podzemnayapochta.data.mapper.toGameState
import com.podzemnayapochta.data.mapper.toSaveDto
import com.podzemnayapochta.domain.model.GameState
import com.podzemnayapochta.domain.model.Letter
import com.podzemnayapochta.domain.model.LetterStatus
import com.podzemnayapochta.domain.repository.GameContent
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class SaveRoundTripTest {
    private val json = Json { ignoreUnknownKeys = true }

    private val content =
        GameContent(
            startLocationId = "post-office",
            locations = emptyList(),
            npcs = emptyList(),
            letters =
                listOf(
                    Letter("l1", "Письмо 1", "Текст", recipientNpcId = "npc-a", reward = 10),
                    Letter("l2", "Письмо 2", "Текст", recipientNpcId = "npc-b", reward = 15),
                    Letter("l3", "Письмо 3", "Текст", recipientNpcId = "npc-c", reward = 20),
                ),
            dialogues = emptyList(),
        )

    private val original: GameState =
        GameState(
            currentLocationId = "tunnel",
            letters =
                content.letters
                    .associateBy { it.id }
                    .toMutableMap()
                    .apply {
                        this["l1"] = getValue("l1").copy(status = LetterStatus.DELIVERED)
                        this["l2"] = getValue("l2").copy(status = LetterStatus.IN_TRANSIT)
                        this["l3"] = getValue("l3").copy(status = LetterStatus.RECEIVED)
                    },
            flags = mapOf("got_first_letter" to true, "knows_elevator_rumor" to true),
            score = 45,
            unlockedLocationIds = setOf("post-office", "market", "tunnel"),
        )

    @Test
    fun `GameState переживает round-trip через JSON`() {
        val encoded = json.encodeToString(SaveStateDto.serializer(), original.toSaveDto())
        val decoded = json.decodeFromString(SaveStateDto.serializer(), encoded)

        val restored = decoded.toGameState(content)

        assertEquals(original, restored)
    }

    @Test
    fun `неизвестные id писем восстанавливаются как LOCKED`() {
        val dto =
            SaveStateDto(
                currentLocationId = "post-office",
                letterStatuses = mapOf("l1" to "DELIVERED", "ghost" to "DELIVERED"),
            )

        val restored = dto.toGameState(content)

        assertEquals(LetterStatus.DELIVERED, restored.letter("l1")?.status)
        assertEquals(LetterStatus.LOCKED, restored.letter("l2")?.status)
        assertEquals(LetterStatus.LOCKED, restored.letter("l3")?.status)
        assertEquals(null, restored.letter("ghost"))
    }
}
