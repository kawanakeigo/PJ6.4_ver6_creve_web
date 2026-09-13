#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
[ -f .env.local ] || { echo '.env.local がありません。'; exit 1; }
umask 077
mkdir -p private-backups
file="private-backups/creve_modular_$(date +%Y%m%d_%H%M%S).dump"
docker compose --env-file .env.local exec -T db pg_dump -U creve -d creve_modular -Fc > "$file"
[ -s "$file" ] || { echo 'バックアップが空です。'; exit 1; }
printf 'この修正版のDBを保存しました: %s\n' "$file"
echo '旧版のDBはこのコマンドの対象ではありません。'
