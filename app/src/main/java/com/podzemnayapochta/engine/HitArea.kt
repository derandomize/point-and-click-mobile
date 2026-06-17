package com.podzemnayapochta.engine

/**
 * Прямоугольная кликабельная область сцены в нормализованных координатах [0..1].
 * Не зависит от размера экрана — масштабируется рендером (см. docs/architecture.md, HitTester).
 */
data class HitArea(
    val id: String,
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
    /** Произвольная метка: id локации перехода, id NPC и т.п. */
    val payload: String? = null,
) {
    init {
        require(left in 0f..1f && right in 0f..1f && top in 0f..1f && bottom in 0f..1f) {
            "Координаты hit-области должны быть в диапазоне [0,1]"
        }
        require(left < right && top < bottom) { "Некорректные границы hit-области: $id" }
    }

    fun contains(
        x: Float,
        y: Float,
    ): Boolean = x in left..right && y in top..bottom
}
