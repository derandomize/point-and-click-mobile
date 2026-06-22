package com.podzemnayapochta.presentation.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.podzemnayapochta.domain.repository.SaveManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel главного меню: знает, есть ли сохранённый прогресс, и умеет
 * начинать новую игру, сбрасывая сейв (см. ROADMAP, PR 6).
 */
@HiltViewModel
class MenuViewModel
    @Inject
    constructor(
        private val saveManager: SaveManager,
    ) : ViewModel() {
        private val _hasSave = MutableStateFlow(false)
        val hasSave: StateFlow<Boolean> = _hasSave.asStateFlow()

        init {
            refreshHasSave()
        }

        fun refreshHasSave() {
            viewModelScope.launch { _hasSave.value = saveManager.hasSave() }
        }

        /** Стереть прежний прогресс и затем продолжить (например, навигировать в игру). */
        fun startNewGame(onCleared: () -> Unit) {
            viewModelScope.launch {
                saveManager.clear()
                _hasSave.value = false
                onCleared()
            }
        }
    }
