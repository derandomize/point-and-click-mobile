package com.podzemnayapochta.presentation.game

import app.cash.turbine.test
import com.podzemnayapochta.domain.model.DialogueChoice
import com.podzemnayapochta.domain.model.DialogueEffect
import com.podzemnayapochta.domain.model.DialogueNode
import com.podzemnayapochta.domain.model.GameState
import com.podzemnayapochta.domain.model.Letter
import com.podzemnayapochta.domain.model.LetterStatus
import com.podzemnayapochta.domain.model.Location
import com.podzemnayapochta.domain.model.Npc
import com.podzemnayapochta.domain.repository.ContentRepository
import com.podzemnayapochta.domain.repository.GameContent
import com.podzemnayapochta.domain.repository.SaveManager
import com.podzemnayapochta.domain.usecase.DeliverLetter
import com.podzemnayapochta.domain.usecase.DeliverResult
import com.podzemnayapochta.domain.usecase.DialogueEngine
import com.podzemnayapochta.domain.usecase.MoveTo
import com.podzemnayapochta.domain.usecase.QuestEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class GameViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    private val content =
        GameContent(
            startLocationId = "post-office",
            locations =
                listOf(
                    Location("post-office", "Почта", "", "a.png", connectedLocationIds = listOf("market")),
                    Location("market", "Рынок", "", "b.png", connectedLocationIds = listOf("post-office")),
                ),
            npcs = listOf(Npc("npc-pm", "Начальник", "p.png", "post-office", dialogueRootId = "d1")),
            letters =
                listOf(
                    Letter("l1", "Письмо", "Текст", recipientNpcId = "npc-pm", reward = 10),
                ),
            dialogues =
                listOf(
                    DialogueNode(
                        id = "d1",
                        speakerNpcId = "npc-pm",
                        text = "Новенький?",
                        choices =
                            listOf(
                                DialogueChoice(
                                    text = "Беру письмо.",
                                    targetNodeId = "d2",
                                    effects = listOf(DialogueEffect("got_first_letter", true)),
                                ),
                                DialogueChoice(text = "Позже.", targetNodeId = null),
                            ),
                    ),
                    DialogueNode(id = "d2", speakerNpcId = "npc-pm", text = "Удачи.", choices = emptyList()),
                ),
        )

    private val repository =
        object : ContentRepository {
            override suspend fun loadContent(): GameContent = content
        }

    /** In-memory SaveManager: хранит последний сохранённый GameState. */
    private class FakeSaveManager(
        var saved: GameState? = null,
    ) : SaveManager {
        var saveCount = 0

        override suspend fun save(state: GameState) {
            saved = state
            saveCount++
        }

        override suspend fun load(content: GameContent): GameState? = saved

        override suspend fun hasSave(): Boolean = saved != null

        override suspend fun clear() {
            saved = null
        }
    }

    private fun viewModel(saveManager: SaveManager = FakeSaveManager()) =
        GameViewModel(
            contentRepository = repository,
            saveManager = saveManager,
            moveTo = MoveTo(),
            deliverLetter = DeliverLetter(),
            questEngine = QuestEngine(),
            dialogueEngine = DialogueEngine(),
        )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `после загрузки состояние Ready и первое письмо выдано`() =
        runTest(dispatcher) {
            val vm = viewModel()
            advanceUntilIdle()

            val state = assertIs<GameUiState.Ready>(vm.uiState.value)
            assertEquals("post-office", state.gameState.currentLocationId)
            assertEquals(LetterStatus.RECEIVED, state.gameState.letter("l1")?.status)
        }

    @Test
    fun `moveTo в связанную локацию меняет текущую локацию`() =
        runTest(dispatcher) {
            val vm = viewModel()
            advanceUntilIdle()

            vm.moveTo("market")
            advanceUntilIdle()

            val state = assertIs<GameUiState.Ready>(vm.uiState.value)
            assertEquals("market", state.gameState.currentLocationId)
        }

    @Test
    fun `moveTo помечает полученное письмо как в пути`() =
        runTest(dispatcher) {
            val vm = viewModel()
            advanceUntilIdle()

            vm.moveTo("market")
            advanceUntilIdle()

            val state = assertIs<GameUiState.Ready>(vm.uiState.value)
            assertEquals(LetterStatus.IN_TRANSIT, state.gameState.letter("l1")?.status)
        }

    @Test
    fun `deliver верному адресату начисляет награду`() =
        runTest(dispatcher) {
            val vm = viewModel()
            advanceUntilIdle()

            val result = vm.deliver("l1", "npc-pm")

            assertIs<DeliverResult.Success>(result)
            val state = assertIs<GameUiState.Ready>(vm.uiState.value)
            assertEquals(10, state.gameState.score)
            assertEquals(LetterStatus.DELIVERED, state.gameState.letter("l1")?.status)
        }

    @Test
    fun `uiState эмитит Loading затем Ready`() =
        runTest(dispatcher) {
            val vm = viewModel()
            vm.uiState.test {
                assertIs<GameUiState.Loading>(awaitItem())
                advanceUntilIdle()
                assertIs<GameUiState.Ready>(awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `startDialogue открывает корневой узел NPC с доступными вариантами`() =
        runTest(dispatcher) {
            val vm = viewModel()
            advanceUntilIdle()

            vm.startDialogue("npc-pm")

            val state = assertIs<GameUiState.Ready>(vm.uiState.value)
            val dialogue = requireNotNull(state.dialogue)
            assertEquals("d1", dialogue.node.id)
            assertEquals("Начальник", dialogue.speakerName)
            assertEquals(2, dialogue.availableChoices.size)
        }

    @Test
    fun `выбор варианта применяет эффекты и переходит к следующему узлу`() =
        runTest(dispatcher) {
            val vm = viewModel()
            advanceUntilIdle()
            vm.startDialogue("npc-pm")

            val first = (vm.uiState.value as GameUiState.Ready).dialogue!!.availableChoices.first()
            vm.chooseDialogueOption(first)

            val state = assertIs<GameUiState.Ready>(vm.uiState.value)
            assertEquals("d2", state.dialogue?.node?.id)
            assertTrue(state.gameState.flag("got_first_letter"))
        }

    @Test
    fun `выбор варианта без цели завершает диалог`() =
        runTest(dispatcher) {
            val vm = viewModel()
            advanceUntilIdle()
            vm.startDialogue("npc-pm")

            val close = (vm.uiState.value as GameUiState.Ready).dialogue!!.availableChoices.last()
            vm.chooseDialogueOption(close)

            assertNull((vm.uiState.value as GameUiState.Ready).dialogue)
        }

    @Test
    fun `endDialogue закрывает активный диалог`() =
        runTest(dispatcher) {
            val vm = viewModel()
            advanceUntilIdle()
            vm.startDialogue("npc-pm")

            vm.endDialogue()

            assertNull((vm.uiState.value as GameUiState.Ready).dialogue)
        }

    @Test
    fun `setBagOpen переключает флаг сумки`() =
        runTest(dispatcher) {
            val vm = viewModel()
            advanceUntilIdle()

            vm.setBagOpen(true)
            assertTrue((vm.uiState.value as GameUiState.Ready).isBagOpen)

            vm.setBagOpen(false)
            assertFalse((vm.uiState.value as GameUiState.Ready).isBagOpen)
        }

    @Test
    fun `bagLetters содержит выданное на старте письмо`() =
        runTest(dispatcher) {
            val vm = viewModel()
            advanceUntilIdle()

            val ids = vm.bagLetters().map { it.id }

            assertEquals(listOf("l1"), ids)
        }

    @Test
    fun `при диалоге с адресатом письмо доступно к вручению`() =
        runTest(dispatcher) {
            val vm = viewModel()
            advanceUntilIdle()

            vm.startDialogue("npc-pm")

            val dialogue = (vm.uiState.value as GameUiState.Ready).dialogue!!
            assertEquals("l1", dialogue.deliverableLetter?.id)
        }

    @Test
    fun `deliverToCurrentNpc доставляет письмо и начисляет награду`() =
        runTest(dispatcher) {
            val vm = viewModel()
            advanceUntilIdle()
            vm.startDialogue("npc-pm")

            vm.deliverToCurrentNpc()

            val state = assertIs<GameUiState.Ready>(vm.uiState.value)
            assertEquals(LetterStatus.DELIVERED, state.gameState.letter("l1")?.status)
            assertEquals(10, state.gameState.score)
            assertNull(state.dialogue?.deliverableLetter)
            assertTrue(state.deliveryFeedback?.contains("доставлено") == true)
        }

    @Test
    fun `consumeDeliveryFeedback сбрасывает сообщение`() =
        runTest(dispatcher) {
            val vm = viewModel()
            advanceUntilIdle()
            vm.startDialogue("npc-pm")
            vm.deliverToCurrentNpc()

            vm.consumeDeliveryFeedback()

            assertNull((vm.uiState.value as GameUiState.Ready).deliveryFeedback)
        }

    @Test
    fun `сохранённое состояние восстанавливается при старте`() =
        runTest(dispatcher) {
            val savedState =
                GameState(
                    currentLocationId = "market",
                    letters =
                        mapOf(
                            "l1" to
                                Letter(
                                    "l1",
                                    "Письмо",
                                    "Текст",
                                    recipientNpcId = "npc-pm",
                                    reward = 10,
                                    status = LetterStatus.DELIVERED,
                                ),
                        ),
                    score = 99,
                    unlockedLocationIds = setOf("post-office", "market"),
                )
            val vm = viewModel(FakeSaveManager(saved = savedState))
            advanceUntilIdle()

            val state = assertIs<GameUiState.Ready>(vm.uiState.value)
            assertEquals("market", state.gameState.currentLocationId)
            assertEquals(99, state.gameState.score)
            assertEquals(LetterStatus.DELIVERED, state.gameState.letter("l1")?.status)
        }

    @Test
    fun `изменение состояния автоматически сохраняется`() =
        runTest(dispatcher) {
            val saveManager = FakeSaveManager()
            val vm = viewModel(saveManager)
            advanceUntilIdle()
            vm.startDialogue("npc-pm")

            vm.deliverToCurrentNpc()
            advanceUntilIdle()

            assertEquals(LetterStatus.DELIVERED, saveManager.saved?.letter("l1")?.status)
            assertTrue(saveManager.saveCount > 0)
        }
}
