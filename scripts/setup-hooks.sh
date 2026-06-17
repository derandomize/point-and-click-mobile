#!/bin/sh
# Устанавливает git-хуки проекта в .git/hooks.
# Запуск из корня репозитория: ./scripts/setup-hooks.sh

set -e

HOOKS_DIR="$(git rev-parse --git-path hooks)"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

cp "$SCRIPT_DIR/pre-commit" "$HOOKS_DIR/pre-commit"
chmod +x "$HOOKS_DIR/pre-commit"

echo "Git-хуки установлены в $HOOKS_DIR"
