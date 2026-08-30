#!/bin/bash
set -euo pipefail
cd /mnt/e/projects/mine/astro-code-oj

echo "== wait nacos healthy =="
i=0
until docker compose ps nacos --format '{{.Status}}' | grep -q healthy; do
  i=$((i+1))
  if [ "$i" -gt 40 ]; then
    echo "nacos not healthy"
    docker compose logs nacos --tail 40
    exit 1
  fi
  sleep 3
done
docker compose ps nacos

echo "== nacos-init =="
docker compose run --rm nacos-init

echo "== minio-init + oj + similarity =="
docker compose up -d minio-init oj similarity-service

echo "== judge (may fail if image missing) =="
docker compose up -d judge-service || true

echo "== wait oj =="
i=0
until curl -fsS http://127.0.0.1:89/actuator/health >/tmp/oj-health.json 2>/dev/null; do
  i=$((i+1))
  if [ "$i" -gt 36 ]; then
    echo "oj health timeout"
    docker logs acoj-oj --tail 40
    exit 1
  fi
  sleep 5
done

echo "== final status =="
docker compose ps -a --format 'table {{.Name}}\t{{.Status}}\t{{.Ports}}'
echo "--- oj health ---"
cat /tmp/oj-health.json
echo
echo "--- similarity logs ---"
docker logs acoj-similarity --tail 15 2>&1 || true
echo "--- judge logs ---"
docker logs acoj-judge --tail 15 2>&1 || true
