# Архитектура

## Принципы
- Без игровых движков. Рендер — Android `Canvas` / `View` / Jetpack Compose `Canvas`.
- Чёткое разделение слоёв: data → domain → presentation.
- Контент игры (локации, NPC, письма, диалоги) — внешние JSON, а не хардкод.
- Состояние игры — единый immutable `GameState`, обновляется через события.

## Слои
- **app** — точка входа, навигация между экранами (меню, карта, локация, диалог).
- **presentation** — экраны и `ViewModel`-ы, отрисовка `Canvas`, обработка тапов.
- **domain** — модели (`Letter`, `Npc`, `Location`, `DialogueNode`, `GameState`), use-case-ы (`DeliverLetter`, `StartDialogue`, `MoveTo`).
- **data** — загрузка JSON-контента из `assets`, репозитории, сохранения через `DataStore`.
- **engine** — мини-«движок» поверх Android: сцена, спрайты, hit-области, рендер-цикл, простая анимация (tween/spritesheet).

## Ключевые модули
- `SceneRenderer` — рисует фон + слой кликабельных объектов на `Canvas`.
- `HitTester` — определяет, по какому объекту тапнули.
- `DialogueEngine` — дерево реплик с условиями (флаги в `GameState`).
- `QuestEngine` — состояние писем: получено / в пути / доставлено.
- `SaveManager` — сериализация `GameState` в `DataStore` (JSON).

> **Реализация SceneRenderer/HitTester.** Экран локации (`presentation/location`)
> рисует сцену на Compose Canvas. `SceneBuilder` собирает `Scene` из `GameContent`:
> NPC, выходы и (в финале) сюжетный HOTSPOT становятся `SceneObject`
> с нормализованными hit-областями. `LocationScreen` декодирует из `assets`
> нарисованный фон локации (`bg.png`) и портреты NPC и рисует их на Canvas;
> выходы и hotspot помечаются компактными полупрозрачными «табличками».
> Тап обрабатывается `HitTester`: по выходу — переход (`MoveTo`) и навигация,
> по NPC — диалог (оверлей `DialogueOverlay`), по hotspot — финал.

> **Карта.** `MapBuilder` раскладывает локации из `GameContent` по сетке в
> нормализованных координатах; `MapScreen` рисует узлы и связи на Canvas и
> гейтит переходы по множеству открытых локаций (`GameState.unlockedLocationIds`).

> **Сохранения и финал.** `SaveManager` (реализация `DataStoreSaveManager`)
> хранит снимок `GameState` одной JSON-строкой в Preferences DataStore
> (`game_save`); игра автосохраняется при изменении состояния и
> восстанавливается на старте («Продолжить» / «Новая игра» в меню).
> Use-case `ElevatorFinale` открывает финал у локации `old-elevator` при
> собранных условиях и ведёт к одной из двух концовок (`Ending`).

## Поток управления
`Input (tap) → ViewModel → UseCase → GameState update → State flow → UI redraw`.

> **Реализация.** `GameViewModel` (Hilt) держит `StateFlow<GameUiState>`,
> загружает контент через `ContentRepository`, инициализирует `GameState`
> (первое письмо выдаётся на старте) и обновляет его через use-case-ы
> (`MoveTo`, `DeliverLetter`, `QuestEngine`). Экраны подписываются на состояние
> через `collectAsStateWithLifecycle`. DI собран в `di/AppModule`.

## Контент-пайплайн
1. Сценарист пишет письма/диалоги в шаблонных JSON.
2. Художник генерит/кладёт PNG в `assets/art/<location>/bg.png` и
   `assets/art/npc/<id>.png` по промптам из `assets/art/prompts/`.
3. Сборка проверяет ссылки контента линтером (gradle task).

> **Реализация.** Контент игры лежит в `app/src/main/assets/content/game.json`
> (локации, NPC, письма, диалоги). Загружается через `AssetContentRepository`
> (kotlinx.serialization) и мапится в domain-модели. Ссылочная целостность
> проверяется gradle-задачей `./gradlew lintContent` (входит в `check` и CI):
> уникальность id, существование связанных локаций и NPC, адресаты писем
> (с диалогом для вручения), целевые узлы диалогов и то, что каждый проверяемый
> флаг где-то выставляется эффектом. Инвариант контента дополнительно покрыт
> JVM-тестом `GameContentIntegrityTest`.

> **Графика.** Промпты для генерации фонов и портретов — копируемые
> самодостаточные тексты в `assets/art/prompts/` (по файлу на локацию и на
> NPC, плюс `README.md` с легендой кликабельных зон). Каждый промпт «пинит»
> выходы/NPC/рычаг лифта к детерминированным координатам из `SceneBuilder`,
> чтобы сгенерированная картинка совпадала с зонами тапа.

## Структура репозитория
```
/app                          # Android-приложение
  /src/main/java/...          # Kotlin-код по слоям (data/domain/engine/presentation)
  /src/main/assets/content/   # game.json — локации, NPC, письма, диалоги
  /src/main/assets/art/       # графика: <location>/bg.png, npc/<id>.png
  /src/main/assets/art/prompts/  # промпты для генерации графики
  /src/test/java/...          # JVM-тесты (domain, контент, presentation)
/docs                         # документация
/tools                        # скрипты валидации контента
```

## Тестирование
- Unit-тесты на `domain` (use-case-ы, QuestEngine, DialogueEngine).
- Скриншот-тесты ключевых экранов (по желанию).
- Ручное тестирование сценариев писем.
