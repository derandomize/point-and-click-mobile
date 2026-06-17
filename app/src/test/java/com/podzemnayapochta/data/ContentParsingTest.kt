package com.podzemnayapochta.data

import com.podzemnayapochta.data.dto.GameContentDto
import com.podzemnayapochta.data.mapper.toDomain
import com.podzemnayapochta.domain.model.LetterStatus
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ContentParsingTest {
    private val json = Json { ignoreUnknownKeys = true }

    private val sample =
        """
        {
          "startLocationId": "post-office",
          "locations": [
            { "id": "post-office", "title": "Почта", "backgroundAsset": "a.png",
              "connectedLocationIds": ["market"], "npcIds": ["npc-pm"] }
          ],
          "npcs": [
            { "id": "npc-pm", "name": "Начальник", "portraitAsset": "p.png", "locationId": "post-office" }
          ],
          "letters": [
            { "id": "l1", "title": "Письмо", "body": "Текст", "recipientNpcId": "npc-pm", "reward": 10 }
          ],
          "dialogues": [
            { "id": "d1", "text": "Привет",
              "choices": [ { "text": "Ок", "targetNodeId": null,
                "effects": [ { "flag": "f", "value": true } ] } ] }
          ]
        }
        """.trimIndent()

    @Test
    fun `парсинг и маппинг контента в domain работает`() {
        val dto = json.decodeFromString(GameContentDto.serializer(), sample)

        assertEquals("post-office", dto.startLocationId)
        assertEquals(1, dto.locations.size)

        val location = dto.locations.first().toDomain()
        assertEquals(listOf("market"), location.connectedLocationIds)

        val letter = dto.letters.first().toDomain()
        assertEquals(LetterStatus.LOCKED, letter.status)
        assertEquals("npc-pm", letter.recipientNpcId)

        val node = dto.dialogues.first().toDomain()
        assertTrue(
            node.choices
                .first()
                .effects
                .isNotEmpty(),
        )
        assertEquals(
            "f",
            node.choices
                .first()
                .effects
                .first()
                .flag,
        )
    }
}
