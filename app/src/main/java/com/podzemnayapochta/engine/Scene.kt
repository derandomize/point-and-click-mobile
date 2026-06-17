package com.podzemnayapochta.engine

/**
 * Описание сцены для рендера: фон + слой кликабельных областей
 * (см. docs/architecture.md, SceneRenderer).
 */
data class Scene(
    val locationId: String,
    val title: String,
    val backgroundAsset: String,
    val hitAreas: List<HitArea> = emptyList(),
)
