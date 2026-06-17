# Подземная Почта

Мобильная point-and-click игра для Android. Игрок — почтальон в подземном городе. Разносит письма, общается с жителями, постепенно раскрывает тайну заброшенного лифта на поверхность и в финале решает её судьбу.

**Платформа:** Android · **Язык:** Kotlin · **Движок:** не используется (свой мини-рендер на Canvas).

## Документация
- [Идея](docs/idea.md)
- [Архитектура](docs/architecture.md)
- [Стилистика и промпты для графики](docs/style.md)
- [Инструментарий и процесс](docs/tooling.md)

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

## Структура проекта

```
/app                       # Android-приложение (Kotlin, Jetpack Compose)
  /src/main/java/com/podzemnayapochta
    /ui/theme              # Тема и палитра (см. docs/style.md)
    MainActivity.kt        # Точка входа
  /src/main/res            # Ресурсы (иконки, строки, цвета)
/docs                      # Документация проекта
/gradle                    # Version catalog (libs.versions.toml) и wrapper
```

## Команда
- Алексей Токарев
- Дмитрий Деружинский
- Владимир Захаров
- Егор Никоненко
- Лев Шатохин
