package com.podzemnayapochta.presentation.menu

import com.podzemnayapochta.domain.model.GameState
import com.podzemnayapochta.domain.repository.GameContent
import com.podzemnayapochta.domain.repository.SaveManager
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
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class MenuViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    private class FakeSaveManager(
        var hasSaved: Boolean,
    ) : SaveManager {
        var cleared = false

        override suspend fun save(state: GameState) = Unit

        override suspend fun load(content: GameContent): GameState? = null

        override suspend fun hasSave(): Boolean = hasSaved

        override suspend fun clear() {
            cleared = true
            hasSaved = false
        }
    }

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `hasSave отражает наличие сохранения`() =
        runTest(dispatcher) {
            val vm = MenuViewModel(FakeSaveManager(hasSaved = true))
            advanceUntilIdle()

            assertTrue(vm.hasSave.value)
        }

    @Test
    fun `startNewGame стирает сейв и вызывает колбэк`() =
        runTest(dispatcher) {
            val saveManager = FakeSaveManager(hasSaved = true)
            val vm = MenuViewModel(saveManager)
            advanceUntilIdle()

            var navigated = false
            vm.startNewGame { navigated = true }
            advanceUntilIdle()

            assertTrue(saveManager.cleared)
            assertTrue(navigated)
            assertFalse(vm.hasSave.value)
        }
}
