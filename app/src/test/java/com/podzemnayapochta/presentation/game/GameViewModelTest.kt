package com.podzemnayapochta.presentation.game

import app.cash.turbine.test
import com.podzemnayapochta.domain.model.DialogueNode
import com.podzemnayapochta.domain.model.Letter
import com.podzemnayapochta.domain.model.LetterStatus
import com.podzemnayapochta.domain.model.Location
import com.podzemnayapochta.domain.model.Npc
import com.podzemnayapochta.domain.repository.ContentRepository
import com.podzemnayapochta.domain.repository.GameContent
import com.podzemnayapochta.domain.usecase.DeliverLetter
import com.podzemnayapochta.domain.usecase.DeliverResult
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
import kotlin.test.assertIs

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
            npcs = listOf(Npc("npc-pm", "Начальник", "p.png", "post-office")),
            letters =
                listOf(
                    Letter("l1", "Письмо", "Текст", recipientNpcId = "npc-pm", reward = 10),
                ),
            dialogues = emptyList<DialogueNode>(),
        )

    private val repository =
        object : ContentRepository {
            override suspend fun loadContent(): GameContent = content
        }

    private fun viewModel() =
        GameViewModel(
            contentRepository = repository,
            moveTo = MoveTo(),
            deliverLetter = DeliverLetter(),
            questEngine = QuestEngine(),
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
}
