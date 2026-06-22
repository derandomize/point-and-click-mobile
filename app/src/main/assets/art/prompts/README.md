# Промпты для генерации графики

Здесь лежат промпты для генерации игровых изображений. Их соседи — сами ассеты:
фоны локаций кладутся в `app/src/main/assets/art/<location>/bg.png`, портреты NPC —
в `app/src/main/assets/art/npc/<id>.png` (пути берутся из `assets/content/game.json`).

Цель этих промптов — не просто «нарисовать красиво», а нарисовать так, чтобы
**нарисованные объекты совпадали с кликабельными зонами**. Зоны рассчитываются
детерминированно в `engine/SceneBuilder.kt`, поэтому если художник/нейросеть
поставит дверь или персонажа в указанный прямоугольник — тап по нему сработает.

## Формат вывода

- **Соотношение сторон:** 9:16, вертикальное (экран телефона). Фон растягивается
  на весь `Canvas`, поэтому ключевые объекты держим внутри указанных зон с запасом.
- **Рекомендуемый размер:** 1080 x 1920 px, PNG.
- **Фоны локаций:** без персонажей и без UI (персонажей движок рисует поверх).
- **Портреты NPC:** прозрачный фон (alpha), вид сбоку, в полный рост.

## Система координат

Координаты нормализованы в диапазоне `[0..1]`:

- `x` — слева направо (0 = левый край, 1 = правый край).
- `y` — сверху вниз (0 = верх экрана, 1 = низ экрана).

Проценты ниже — это доли ширины/высоты кадра. Например «33% ширины, 5–20% высоты»
означает точку на трети ширины слева, в верхней полосе кадра.

## Где какие кликабельные зоны (из `SceneBuilder`)

| Тип | Назначение | Где рисовать |
| --- | --- | --- |
| Выход (EXIT) | переход в соседнюю локацию | верхняя полоса, `y` 5–20%; центр по `x` зависит от числа выходов |
| Персонаж (NPC) | тап открывает диалог | нижняя полоса, `y` 55–90%; центр по `x` зависит от числа персонажей |
| Интерактив (HOTSPOT) | сюжетная точка (рычаг лифта) | центр кадра, `x` 32–68%, `y` 32–48% |

Расчёт центров по горизонтали:

- **Один объект:** центр на 50% ширины.
- **Два объекта:** центры на 33% и 67% ширины (в порядке из `game.json`).

Полуширина зоны: выходы ~±10% ширины, персонажи ~±9% ширины. Рисуем объект так,
чтобы его «тело» попадало в этот прямоугольник.

## Глобальный промпт стиля

> Hand-painted 2D illustration, point-and-click adventure game art, cozy and
> slightly melancholic underground town. Soft warm lantern light (amber, honey,
> teal glow) against deep cool shadows (dark brown, burgundy, dusty violet).
> Visible imperfect ink linework, painterly textures, subtle paper grain.
> Static "diorama" composition, flat soft lighting, no harsh shadows.
> Inspired by Machinarium, Botanicula, Night in the Woods, Ghibli backgrounds.
> Vertical 9:16 framing, no text, no UI, no watermark.

## Негативные подсказки

`3d render, photorealistic, anime, cel shading, sharp digital lines, neon,
sci-fi, blood, gore, modern technology, text, watermark, logo`.

## Файлы

- `post-office.md`, `market.md`, `clock-house.md`, `tavern.md`, `archive.md`,
  `tunnel.md`, `old-elevator.md` — фоны локаций с раскладкой кликабельных зон.
- `npc-portraits.md` — портреты всех NPC.
