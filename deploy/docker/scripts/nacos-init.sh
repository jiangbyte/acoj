#!/bin/sh
set -eu

NACOS_ADDR="${NACOS_ADDR:-nacos:8848}"
NACOS_USER="${NACOS_USER:-nacos}"
NACOS_PASS="${NACOS_PASS:-nacos}"
NACOS_GROUP="${NACOS_GROUP:-DEFAULT_GROUP}"
CONFIG_DIR="${CONFIG_DIR:-/configs/DEFAULT_GROUP}"

echo "[nacos-init] waiting for Nacos at ${NACOS_ADDR} ..."
i=0
until curl -fsS "http://${NACOS_ADDR}/nacos/v1/console/health/readiness" >/dev/null 2>&1; do
  i=$((i + 1))
  if [ "$i" -gt 60 ]; then
    echo "[nacos-init] Nacos not ready"
    exit 1
  fi
  sleep 2
done

echo "[nacos-init] login ..."
LOGIN_JSON="$(curl -fsS -X POST "http://${NACOS_ADDR}/nacos/v1/auth/login" \
  -d "username=${NACOS_USER}&password=${NACOS_PASS}" || true)"
TOKEN="$(printf '%s' "${LOGIN_JSON}" | sed -n 's/.*"accessToken"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')"

publish() {
  data_id="$1"
  file="$2"
  cfg_type="$3"
  echo "[nacos-init] publish ${data_id} (type=${cfg_type})"
  if [ ! -f "${file}" ]; then
    echo "[nacos-init] missing file: ${file}"
    exit 1
  fi
  if [ -n "${TOKEN}" ]; then
    curl -fsS -X POST "http://${NACOS_ADDR}/nacos/v1/cs/configs" \
      -d "dataId=${data_id}" \
      -d "group=${NACOS_GROUP}" \
      -d "type=${cfg_type}" \
      -d "accessToken=${TOKEN}" \
      --data-urlencode "content@${file}" >/dev/null
  else
    curl -fsS -X POST "http://${NACOS_ADDR}/nacos/v1/cs/configs" \
      -d "dataId=${data_id}" \
      -d "group=${NACOS_GROUP}" \
      -d "type=${cfg_type}" \
      --data-urlencode "content@${file}" >/dev/null
  fi
}

# Source: deploy/nacos_config_export.zip → DEFAULT_GROUP/*
# Hosts rewritten for compose DNS (mysql/redis/rabbitmq/minio).
publish "common.yaml" "${CONFIG_DIR}/common.yaml" "yaml"
publish "oj.yaml" "${CONFIG_DIR}/oj.yaml" "yaml"
publish "judge-service.yaml" "${CONFIG_DIR}/judge-service.yaml" "yaml"
publish "similarity-service.yaml" "${CONFIG_DIR}/similarity-service.yaml" "yaml"
publish "spring.ai.alibaba.configurable.prompt" "${CONFIG_DIR}/spring.ai.alibaba.configurable.prompt" "json"

echo "[nacos-init] done"
