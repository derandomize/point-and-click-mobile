package com.podzemnayapochta.data

import com.podzemnayapochta.data.dto.GameContentDto
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Проверяет, что реальный assets/content/game.json соответствует объёму MVP
 * (см. docs/idea.md) и инвариантам игрового цикла:
 *  - письмо можно вручить только в диалоге, значит у адресата должен быть dialogueRootId;
 *  - локации открываются ходьбой по связям, значит граф должен быть связным от старта.
 */
class GameContentIntegrityTest {
    private val json = Json { ignoreUnknownKeys = true }

    private val content: GameContentDto by lazy {
        val candidates =
            listOf(
                "src/main/assets/content/game.json",
                "app/src/main/assets/content/game.json",
            )
        val file =
            candidates
                .map(::File)
                .firstOrNull { it.exists() }
                ?: error("game.json не найден (cwd=${File("").absolutePath})")
        json.decodeFromString(GameContentDto.serializer(), file.readText())
    }

    @Test
    fun `объём контента в рамках MVP`() {
        assertTrue(content.locations.size in 5..7, "локаций должно быть 5..7, а их ${content.locations.size}")
        assertTrue(content.npcs.size in 6..10, "NPC должно быть 6..10, а их ${content.npcs.size}")
        assertTrue(content.letters.size in 8..12, "писем должно быть 8..12, а их ${content.letters.size}")
    }

    @Test
    fun `локация тоннеля присутствует`() {
        assertNotNull(
            content.locations.firstOrNull { it.id == "tunnel" },
            "должна быть локация 'tunnel' (Заброшенный тоннель)",
        )
    }

    @Test
    fun `каждый адресат письма существует и имеет диалог`() {
        val npcsById = content.npcs.associateBy { it.id }
        content.letters.forEach { letter ->
            val recipient = npcsById[letter.recipientNpcId]
            assertNotNull(recipient, "адресат '${letter.recipientNpcId}' письма '${letter.id}' не найден")
            assertNotNull(
                recipient.dialogueRootId,
                "адресат '${recipient.id}' письма '${letter.id}' без диалога — вручить нельзя",
            )
        }
    }

    @Test
    fun `все переходы диалогов ведут в существующие узлы`() {
        val dialogueIds = content.dialogues.map { it.id }.toSet()
        content.dialogues.forEach { node ->
            node.choices.forEach { choice ->
                val target = choice.targetNodeId
                if (target != null) {
                    assertTrue(target in dialogueIds, "диалог '${node.id}' ведёт в несуществующий узел '$target'")
                }
            }
        }
    }

    @Test
    fun `граф локаций связен от стартовой`() {
        val byId = content.locations.associateBy { it.id }
        val visited = mutableSetOf(content.startLocationId)
        val queue = ArrayDeque<String>().apply { add(content.startLocationId) }
        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            byId[current]?.connectedLocationIds?.forEach { next ->
                if (visited.add(next)) queue.add(next)
            }
        }
        assertEquals(
            content.locations.map { it.id }.toSet(),
            visited,
            "не все локации достижимы от '${content.startLocationId}'",
        )
    }
}
