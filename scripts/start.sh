#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
if ! command -v java >/dev/null; then echo 'JDK 21 をインストールしてください。'; exit 1; fi
major=$(java -version 2>&1 | head -n 1 | sed -E 's/.*version "([0-9]+).*/\1/')
case "$major" in ''|*[!0-9]*) echo 'Javaのバージョンを確認できません。java -version を確認してください。'; exit 1;; esac
[ "$major" -ge 21 ] || { echo 'JDK 21以上が必要です。'; exit 1; }
command -v docker >/dev/null || { echo 'ローカルDBの起動にはDockerとdocker composeが必要です。外部PostgreSQLでの起動はREADME参照。'; exit 1; }
docker compose version >/dev/null
bash scripts/configure-local.sh
set -a
# This is the private local configuration generated here and owned by the operator.
source .env.local
set +a
chmod +x mvnw
./mvnw -B clean verify
docker compose --env-file .env.local up -d --wait db
printf '\n起動先: %s\n管理画面: %s/admin/login\n' "$CREVE_BASE_URL" "$CREVE_BASE_URL"
echo '停止は Control+C。PostgreSQLのデータはDockerの専用ボリュームに残ります。'
exec java -jar app/target/app-1.0.0-SNAPSHOT.jar
