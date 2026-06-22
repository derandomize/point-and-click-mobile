package com.podzemnayapochta.engine

/**
 * Тип кликабельного объекта сцены — влияет на отрисовку и реакцию на тап.
 */
enum class SceneObjectKind {
    /** Персонаж в локации (тап → диалог). */
    NPC,

    /** Выход в соседнюю локацию (тап → переход). */
    EXIT,

    /** Сюжетный объект/точка интереса. */
    HOTSPOT,
}

/**
 * Кликабельный объект сцены: визуальный тип + геометрия ([HitArea]).
 */
data class SceneObject(
    val kind: SceneObjectKind,
    val label: String,
    val area: HitArea,
    /** Путь к ассету-изображению объекта (например, портрет NPC), если есть. */
    val imageAsset: String? = null,
) {
    /** Полезная нагрузка области: id NPC / id локации перехода. */
    val payload: String? get() = area.payload
}

/**
 * Описание сцены для рендера: фон + слой кликабельных объектов
 * (см. docs/architecture.md, SceneRenderer).
 */
data class Scene(
    val locationId: String,
    val title: String,
    val backgroundAsset: String,
    val objects: List<SceneObject> = emptyList(),
) {
    val hitAreas: List<HitArea> get() = objects.map { it.area }
}
