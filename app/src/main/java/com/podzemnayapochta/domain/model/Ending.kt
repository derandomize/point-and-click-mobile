package com.podzemnayapochta.domain.model

/**
 * Концовки игры (см. docs/idea.md — 2 концовки: открыть лифт / оставить тайну).
 * [flag] — сюжетный флаг, выставляемый при выборе концовки.
 */
enum class Ending(
    val flag: String,
) {
    /** Открыть путь наверх, к поверхности. */
    OPEN_PATH("ending_open_path"),

    /** Оставить тайну и сохранить город под землёй. */
    KEEP_SECRET("ending_keep_secret"),
}
