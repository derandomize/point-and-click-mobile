# Инструментарий и процесс

## Стек
- **Язык:** Kotlin.
- **Платформа:** Android (minSdk 24, targetSdk 34).
- **UI / рендер:** Jetpack Compose + `Canvas` (или классический `View` + `Canvas` при необходимости).
- **Игровой движок:** не используется. Свой мини-«движок» поверх Android.
- **Графика/физика-библиотеки (разрешены):** Android Graphics API, Compose Canvas. Физика не требуется.
- **DI:** Hilt (или ручной DI, по решению команды).
- **Асинхронность:** Kotlin Coroutines + Flow.
- **Сохранения:** Jetpack DataStore (Preferences или Proto).
- **JSON:** kotlinx.serialization.
- **Логи/крэши:** Timber + (опц.) Firebase Crashlytics.

## Сборка
- **Gradle (Kotlin DSL)**, version catalogs (`libs.versions.toml`).
- Android Studio (последняя стабильная).
- JDK 17.

## Качество кода
- ktlint + detekt (pre-commit hook).
- Unit-тесты: JUnit5 + kotlin.test + Turbine для Flow.
- CI: GitHub Actions — build + lint + tests на каждый PR.

## Документация
- Исходники — Markdown в `docs/`.
- Сборка сайта — [MkDocs Material](https://squidfunk.github.io/mkdocs-material/) (`mkdocs.yml`).
- Публикация на GitHub Pages автоматически из `main` (workflow `.github/workflows/docs.yml`).
- На PR документация собирается в строгом режиме (`mkdocs build --strict`) — битые ссылки ломают сборку.

## Контент
- Тексты, диалоги, квесты — JSON в `assets/`.
- Графика — PNG (можно генерить нейросетью по промптам из `docs/style.md`).
- Звук (опц.) — OGG/MP3, короткие лупы.

## Процесс разработки
- **Ветвление:** feature-ветки `feat/<name>` от `main`, PR открывается сразу в `main`.
- **PR-ревью** обязательно: минимум 1 апрув, проходящий CI.
- **Issues и доска:** GitHub Projects (kanban: Backlog → In progress → Review → Done).
- **Релизы:** теги `v0.x` на `main`, APK прикладывается к релизу.
- **Спринты:** по 1 неделе, в конце — демо текущего билда.

## Распределение ролей (5 человек)
- Тимлид / архитектура / core-engine.
- Геймплей и квестовая система.
- UI / экраны / диалоги.
- Контент: сценарий, тексты, JSON.
- Арт и звук: генерация и сборка ассетов по стайлгайду.

Роли пересекаются, точное распределение — на первом собрании.
