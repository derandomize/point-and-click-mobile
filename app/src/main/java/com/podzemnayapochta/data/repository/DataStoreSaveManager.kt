package com.podzemnayapochta.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.podzemnayapochta.data.dto.SaveStateDto
import com.podzemnayapochta.data.mapper.toGameState
import com.podzemnayapochta.data.mapper.toSaveDto
import com.podzemnayapochta.domain.model.GameState
import com.podzemnayapochta.domain.repository.GameContent
import com.podzemnayapochta.domain.repository.SaveManager
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json

private val Context.saveDataStore by preferencesDataStore(name = "game_save")

/**
 * Сохранение прогресса в Preferences DataStore: весь снимок состояния
 * лежит одной JSON-строкой (см. docs/architecture.md — SaveManager).
 */
class DataStoreSaveManager(
    private val context: Context,
    private val json: Json,
) : SaveManager {
    override suspend fun save(state: GameState) {
        val raw = json.encodeToString(SaveStateDto.serializer(), state.toSaveDto())
        context.saveDataStore.edit { prefs -> prefs[KEY] = raw }
    }

    override suspend fun load(content: GameContent): GameState? {
        val raw = readRaw() ?: return null
        val dto =
            runCatching { json.decodeFromString(SaveStateDto.serializer(), raw) }
                .getOrNull() ?: return null
        return dto.toGameState(content)
    }

    override suspend fun hasSave(): Boolean = !readRaw().isNullOrBlank()

    override suspend fun clear() {
        context.saveDataStore.edit { prefs -> prefs.remove(KEY) }
    }

    private suspend fun readRaw(): String? = context.saveDataStore.data.first()[KEY]

    private companion object {
        val KEY = stringPreferencesKey("game_state_json")
    }
}
