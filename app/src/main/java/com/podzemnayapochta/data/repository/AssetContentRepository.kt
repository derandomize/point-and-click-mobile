package com.podzemnayapochta.data.repository

import android.content.Context
import com.podzemnayapochta.data.dto.GameContentDto
import com.podzemnayapochta.data.mapper.toDomain
import com.podzemnayapochta.domain.repository.ContentRepository
import com.podzemnayapochta.domain.repository.GameContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

/**
 * Читает игровой контент из assets/content/game.json (см. docs/architecture.md).
 */
class AssetContentRepository(
    private val context: Context,
    private val json: Json = DefaultJson,
    private val assetPath: String = DEFAULT_ASSET_PATH,
) : ContentRepository {
    override suspend fun loadContent(): GameContent =
        withContext(Dispatchers.IO) {
            val raw =
                context.assets
                    .open(assetPath)
                    .bufferedReader()
                    .use { it.readText() }
            val dto = json.decodeFromString(GameContentDto.serializer(), raw)
            dto.toGameContent()
        }

    private fun GameContentDto.toGameContent(): GameContent =
        GameContent(
            locations = locations.map { it.toDomain() },
            npcs = npcs.map { it.toDomain() },
            letters = letters.map { it.toDomain() },
            dialogues = dialogues.map { it.toDomain() },
            startLocationId = startLocationId,
        )

    companion object {
        const val DEFAULT_ASSET_PATH = "content/game.json"
        val DefaultJson =
            Json {
                ignoreUnknownKeys = true
                isLenient = true
            }
    }
}
