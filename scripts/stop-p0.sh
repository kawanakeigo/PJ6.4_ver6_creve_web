#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
for name in creve playpit nlj; do
  pid_file=".run/$name.pid"
  if [[ -f "$pid_file" ]]; then
    pid="$(cat "$pid_file")"
    if kill -0 "$pid" >/dev/null 2>&1; then kill "$pid"; fi
    rm -f "$pid_file"
  fi
done
if [[ "${1:-}" != "--apps-only" ]]; then docker compose stop postgres; fi
