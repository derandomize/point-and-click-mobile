package com.podzemnayapochta.engine

/**
 * Определяет, по какой [HitArea] пришёлся тап (см. docs/architecture.md, HitTester).
 * Координаты тапа — нормализованные [0..1]. При перекрытии областей возвращается
 * последняя (верхний слой).
 */
class HitTester {
    fun hitTest(
        areas: List<HitArea>,
        x: Float,
        y: Float,
    ): HitArea? = areas.lastOrNull { it.contains(x, y) }
}
