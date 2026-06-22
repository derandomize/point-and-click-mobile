package com.podzemnayapochta.domain.repository

import com.podzemnayapochta.domain.model.GameState

/**
 * Сохранение и восстановление прогресса игры (см. docs/architecture.md —
 * SaveManager: сериализация GameState в DataStore (JSON)).
 *
 * Хранится только динамическая часть состояния; письма восстанавливаются
 * из актуального [GameContent], чтобы сейв переживал правки контента.
 */
interface SaveManager {
    /** Сохранить текущее состояние игры. */
    suspend fun save(state: GameState)

    /**
     * Восстановить состояние, наложив сейв на актуальный контент.
     * Возвращает null, если сохранения нет.
     */
    suspend fun load(content: GameContent): GameState?

    /** Есть ли сохранённый прогресс. */
    suspend fun hasSave(): Boolean

    /** Удалить сохранение (например, при старте новой игры). */
    suspend fun clear()
}
