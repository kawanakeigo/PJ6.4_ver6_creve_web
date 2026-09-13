#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
chmod +x mvnw
./mvnw -B clean verify
command -v docker >/dev/null || { echo '単体テスト完了。PostgreSQL結合テストにはDockerが必要です。'; exit 2; }
export CREVE_TEST_DB_PASSWORD=$(openssl rand -hex 24)
export CREVE_DATASOURCE_URL=jdbc:postgresql://localhost:55433/creve_modular_test
export CREVE_DATASOURCE_USERNAME=creve_test
export CREVE_DATASOURCE_PASSWORD="$CREVE_TEST_DB_PASSWORD"
export CREVE_SESSION_HASH_SALT=$(openssl rand -hex 32)
export CREVE_BOOTSTRAP_ADMIN=false
export CREVE_POSTING_ENABLED=true
# Dedicated test DB only. Never issue down -v to the persistent compose.yml project.
cleanup() { docker compose -f compose.test.yml down --remove-orphans >/dev/null 2>&1 || true; }
trap cleanup EXIT
cleanup
docker compose -f compose.test.yml up -d --wait db
./mvnw -B -Ppostgres-it verify
echo '単体テスト・実PostgreSQL結合テストが完了しました。各モジュールのtarget/*-reportsに結果があります。'
