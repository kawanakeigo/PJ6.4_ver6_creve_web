#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
mkdir -p .run

for port in 8080 8081 8082; do
  if lsof -nP -iTCP:"$port" -sTCP:LISTEN >/dev/null 2>&1; then
    echo "Port $port is already in use. Stop the existing application first." >&2
    exit 1
  fi
done

docker compose up -d postgres
for _ in $(seq 1 60); do
  if docker compose exec -T postgres pg_isready -U platform_root -d postgres >/dev/null 2>&1; then break; fi
  sleep 1
done
docker compose exec -T postgres pg_isready -U platform_root -d postgres >/dev/null
./mvnw -B -DskipTests package

start_app() {
  local name="$1" jar="$2"
  SPRING_PROFILES_ACTIVE=dev nohup java -jar "$jar" >".run/$name.log" 2>&1 &
  echo $! >".run/$name.pid"
}

start_app creve creve/target/creve-2.0.0-SNAPSHOT.jar
start_app playpit playpit/target/playpit-2.0.0-SNAPSHOT.jar
start_app nlj nlj-platform-persistence/target/nlj-platform-persistence-2.0.0-SNAPSHOT.jar

cleanup_failed() {
  bash scripts/stop-p0.sh --apps-only >/dev/null 2>&1 || true
}
trap cleanup_failed ERR

for url in http://localhost:8080/ http://localhost:8081/playpit http://localhost:8082/index.html; do
  ok=false
  for _ in $(seq 1 90); do
    if curl -fsS "$url" >/dev/null 2>&1; then ok=true; break; fi
    sleep 1
  done
  if [[ "$ok" != true ]]; then
    echo "Startup check failed: $url" >&2
    exit 1
  fi
done

trap - ERR
printf '%s\n' \
  'CreVe:   http://localhost:8080/' \
  'PLAYPIT: http://localhost:8081/playpit' \
  'NLJ:     http://localhost:8082/index.html'
