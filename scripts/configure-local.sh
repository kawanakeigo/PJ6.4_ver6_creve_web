#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
if [ -f .env.local ]; then
  echo '.env.local は既存のため変更しません。'
  exit 0
fi
command -v openssl >/dev/null || { echo 'openssl が必要です。'; exit 1; }
umask 077
cat > .env.local <<EOF
CREVE_DATASOURCE_URL=jdbc:postgresql://localhost:55432/creve_modular
CREVE_DATASOURCE_USERNAME=creve
CREVE_DATASOURCE_PASSWORD=$(openssl rand -hex 24)
CREVE_SESSION_HASH_SALT=$(openssl rand -hex 32)
CREVE_BASE_URL=http://localhost:8080
PORT=8080
CREVE_DB_PORT=55432
CREVE_BOOTSTRAP_ADMIN=true
CREVE_ADMIN_EMAIL=admin@creve.local
CREVE_ADMIN_PASSWORD=$(openssl rand -hex 16)
CREVE_POSTING_ENABLED=false
CREVE_PUBLIC_CONTENT_DIR=./config/public
CREVE_SOCIAL_URL=
EOF
chmod 600 .env.local
echo '.env.local を作成しました。管理者用のランダムな初期パスワードもこのファイル内です。'
echo 'このファイルをGitやチャットにアップロードしないでください。'
