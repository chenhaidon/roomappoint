#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

# Basic preflight
if ! command -v docker >/dev/null 2>&1; then
  echo "docker not found" >&2
  exit 1
fi
if ! docker info >/dev/null 2>&1; then
  echo "docker daemon not available" >&2
  exit 1
fi

echo "==> Deploying RoomAppoint via Docker Compose"
echo "Root: $ROOT_DIR"

echo "==> Pull base images (best-effort)"
docker compose pull || true

# Workaround for BuildKit/compose gRPC header issue observed in this environment.
# We build images with classic builder first, then bring stack up.
# This keeps deployment deterministic even if buildkit errors.
echo "==> Build backend image (classic builder)"
DOCKER_BUILDKIT=0 docker build -t springboot-backend -f RoomAppoint.springboot/Dockerfile .

echo "==> Build frontend image (classic builder)"
DOCKER_BUILDKIT=0 docker build -t springboot-frontend-nginx -f RoomAppoint.elementui/Dockerfile .

echo "==> Restart stack"
docker compose up -d

echo "==> Status"
docker compose ps

echo "==> Smoke checks"
set +e
curl -sS -o /dev/null -w "Frontend / -> %{http_code}\n" http://127.0.0.1/
curl -sS -o /dev/null -w "API /api/Room/List -> %{http_code}\n" \
  http://127.0.0.1/api/Room/List -H 'Content-Type: application/json' --data '{"Limit":1,"Page":1}'
set -e

echo "DONE"
