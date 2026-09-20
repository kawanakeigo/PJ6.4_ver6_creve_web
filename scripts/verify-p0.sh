#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
docker compose up -d postgres
for _ in $(seq 1 60); do
  if docker compose exec -T postgres pg_isready -U platform_root -d postgres >/dev/null 2>&1; then break; fi
  sleep 1
done
docker compose exec -T postgres pg_isready -U platform_root -d postgres >/dev/null
./mvnw -B test
echo "All three project builds and unit tests passed."
