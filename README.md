# Подземная Почта

[![CI](https://github.com/derandomize/point-and-click-mobile/actions/workflows/ci.yml/badge.svg)](https://github.com/derandomize/point-and-click-mobile/actions/workflows/ci.yml)

Мобильная point-and-click игра для Android. Игрок — почтальон в подземном городе. Разносит письма, общается с жителями, постепенно раскрывает тайну заброшенного лифта на поверхность и в финале решает её судьбу.

**Платформа:** Android · **Язык:** Kotlin · **Движок:** не используется (свой мини-рендер на Canvas).

## Документация

Собирается с помощью [MkDocs Material](https://squidfunk.github.io/mkdocs-material/)
и публикуется на GitHub Pages (workflow `.github/workflows/docs.yml`).

- [Идея](docs/idea.md)
- [Архитектура](docs/architecture.md)
- [Стилистика и промпты для графики](docs/style.md)
- [Инструментарий и процесс](docs/tooling.md)

Локальный предпросмотр:

```bash
pip install -r docs/requirements.txt
mkdocs serve   # http://127.0.0.1:8000
```

## Сборка

Требования: **JDK 17**, Android SDK (API 34).

```bash
# Debug-сборка APK
./gradlew assembleDebug

# Unit-тесты
./gradlew test

# Проверка кода (lint)
./gradlew ktlintCheck detekt
```

APK появится в `app/build/outputs/apk/debug/`.

## Качество кода

В проекте настроены **ktlint** (форматирование) и **detekt** (статический анализ).

```bash
./gradlew ktlintCheck      # проверка стиля
./gradlew ktlintFormat     # авто-исправление
./gradlew detekt           # статический анализ
```

Установить pre-commit хук (запускает ktlint + detekt перед коммитом):

```bash
./scripts/setup-hooks.sh
```

## Структура проекта

```
/app                       # Android-приложение (Kotlin, Jetpack Compose)
  /src/main/java/com/podzemnayapochta
    /domain                # Модели и use-case-ы (Letter, GameState, DeliverLetter, ...)
    /data                  # DTO, мапперы, репозитории (загрузка JSON-контента)
    /engine                # Мини-движок: Scene, HitArea, HitTester
    /presentation          # Экраны (меню, карта) и навигация на Compose Canvas
    /ui/theme              # Тема и палитра (см. docs/style.md)
    MainActivity.kt        # Точка входа
  /src/main/assets/content # JSON-контент игры (game.json)
  /src/main/res            # Ресурсы (иконки, строки, цвета)
  /src/test                # Unit-тесты (domain, data, engine)
/docs                      # Документация проекта
/gradle                    # Version catalog (libs.versions.toml) и wrapper
/.github/workflows         # CI/CD (GitHub Actions)
```

## CI/CD

GitHub Actions, два workflow:

- **CI** (`.github/workflows/ci.yml`) — на каждый push/PR в `main`:
  - `lint` — ktlint + detekt,
  - `test` — unit-тесты,
  - `build` — сборка debug APK (артефакт `app-debug`).
- **Release** (`.github/workflows/release.yml`) — по тегу `v*`:
  собирает release APK и публикует GitHub Release с приложенным APK.

## Процесс разработки

Feature-ветки `feat/<name>` отходят от `main`, PR открывается **сразу в `main`**.

1. Создать ветку от `main`: `git checkout main && git pull && git checkout -b feat/my-feature`.
2. Закоммитить изменения, запушить, открыть PR в `main`.
3. PR должен пройти CI и получить минимум 1 апрув.
4. Релиз — тег `v0.x` на `main`.

## Команда
- Алексей Токарев
- Дмитрий Деружинский
- Владимир Захаров
- Егор Никоненко
- Лев Шатохин
