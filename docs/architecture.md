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
> NPC и выходы становятся `SceneObject` с нормализованными hit-областями.
> Тап обрабатывается `HitTester`: по выходу — переход (`MoveTo`) и навигация,
> по NPC — вход в диалог (UI диалога — в следующем PR).

## Поток управления
`Input (tap) → ViewModel → UseCase → GameState update → State flow → UI redraw`.

> **Реализация.** `GameViewModel` (Hilt) держит `StateFlow<GameUiState>`,
> загружает контент через `ContentRepository`, инициализирует `GameState`
> (первое письмо выдаётся на старте) и обновляет его через use-case-ы
> (`MoveTo`, `DeliverLetter`, `QuestEngine`). Экраны подписываются на состояние
> через `collectAsStateWithLifecycle`. DI собран в `di/AppModule`.

## Контент-пайплайн
1. Сценарист пишет письма/диалоги в шаблонных JSON.
2. Художник кладёт PNG в `assets/art/<location>/`.
3. Сборка проверяет ссылки контента линтером (gradle task).

> **Реализация.** Контент игры лежит в `app/src/main/assets/content/game.json`
> (локации, NPC, письма, диалоги). Загружается через `AssetContentRepository`
> (kotlinx.serialization) и мапится в domain-модели. Ссылочная целостность
> проверяется gradle-задачей `./gradlew lintContent` (входит в `check` и CI):
> проверяются уникальность id, существование связанных локаций, NPC,
> адресатов писем и целевых узлов диалогов.

## Структура репозитория
```
/app                 # Android-приложение
  /src/main/java/... # Kotlin-код по слоям
  /src/main/assets/  # JSON-контент и графика
/docs                # документация
/tools               # скрипты валидации контента
```

## Тестирование
- Unit-тесты на `domain` (use-case-ы, QuestEngine, DialogueEngine).
- Скриншот-тесты ключевых экранов (по желанию).
- Ручное тестирование сценариев писем.
