#!/bin/sh
set -eu

NACOS_ADDR="${NACOS_ADDR:-nacos:8848}"
NACOS_USER="${NACOS_USER:-nacos}"
NACOS_PASS="${NACOS_PASS:-nacos}"
NACOS_GROUP="${NACOS_GROUP:-DEFAULT_GROUP}"
CONFIG_DIR="${CONFIG_DIR:-/configs/DEFAULT_GROUP}"
# SCA 2025 (nacos-client 3.x) + Nacos 2.5: empty/"public" ID mismatch — use a real namespace ID.
NACOS_NAMESPACE="${NACOS_NAMESPACE:-8fee08f3-44ea-4e26-a9b5-530c582330a3}"
NACOS_NAMESPACE_NAME="${NACOS_NAMESPACE_NAME:-acoj}"

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

# Auth plugin / embedded user store may lag behind readiness
sleep 3

# Nacos 2.4+: no default admin password. Bootstrap once if user missing.
echo "[nacos-init] ensure admin user (${NACOS_USER}) ..."
INIT_JSON="$(curl -sS -X POST "http://${NACOS_ADDR}/nacos/v1/auth/users/admin" \
  --data-urlencode "password=${NACOS_PASS}" || true)"
case "${INIT_JSON}" in
  *'"username"'*)
    echo "[nacos-init] admin initialized"
    ;;
  *"have admin user"*|*"already exists"*|*"exist"*|*"409"*)
    echo "[nacos-init] admin already present (ok)"
    ;;
  *)
    echo "[nacos-init] admin init response: ${INIT_JSON}"
    ;;
esac

echo "[nacos-init] login ..."
TOKEN=""
i=0
while [ "$i" -lt 15 ]; do
  LOGIN_JSON="$(curl -sS -X POST "http://${NACOS_ADDR}/nacos/v1/auth/login" \
    --data-urlencode "username=${NACOS_USER}" \
    --data-urlencode "password=${NACOS_PASS}" || true)"
  TOKEN="$(printf '%s' "${LOGIN_JSON}" | sed -n 's/.*"accessToken"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')"
  if [ -n "${TOKEN}" ]; then
    break
  fi
  i=$((i + 1))
  sleep 2
done

if [ -z "${TOKEN}" ]; then
  echo "[nacos-init] login failed: ${LOGIN_JSON:-empty}"
  exit 1
fi
echo "[nacos-init] login ok"

if [ -n "${NACOS_NAMESPACE}" ]; then
  echo "[nacos-init] ensure namespace ${NACOS_NAMESPACE} (${NACOS_NAMESPACE_NAME}) ..."
  NS_JSON="$(curl -sS -X POST "http://${NACOS_ADDR}/nacos/v1/console/namespaces" \
    -d "accessToken=${TOKEN}" \
    --data-urlencode "customNamespaceId=${NACOS_NAMESPACE}" \
    --data-urlencode "namespaceName=${NACOS_NAMESPACE_NAME}" \
    --data-urlencode "namespaceDesc=ACOJ compose" || true)"
  echo "[nacos-init] namespace response: ${NS_JSON:-ok}"
fi

publish() {
  data_id="$1"
  file="$2"
  cfg_type="$3"
  echo "[nacos-init] publish ${data_id} (type=${cfg_type}, ns=${NACOS_NAMESPACE:-public})"
  if [ ! -f "${file}" ]; then
    echo "[nacos-init] missing file: ${file}"
    exit 1
  fi
  curl -fsS -X POST "http://${NACOS_ADDR}/nacos/v1/cs/configs" \
    -d "dataId=${data_id}" \
    -d "group=${NACOS_GROUP}" \
    -d "type=${cfg_type}" \
    -d "tenant=${NACOS_NAMESPACE}" \
    -d "accessToken=${TOKEN}" \
    --data-urlencode "content@${file}" >/dev/null
}

# Source: deploy/nacos_config_export.zip → DEFAULT_GROUP/*
# Hosts rewritten for compose DNS (mysql/redis/rabbitmq/minio).
publish "common.yaml" "${CONFIG_DIR}/common.yaml" "yaml"
publish "oj.yaml" "${CONFIG_DIR}/oj.yaml" "yaml"
publish "judge-service.yaml" "${CONFIG_DIR}/judge-service.yaml" "yaml"
publish "similarity-service.yaml" "${CONFIG_DIR}/similarity-service.yaml" "yaml"
publish "spring.ai.alibaba.configurable.prompt" "${CONFIG_DIR}/spring.ai.alibaba.configurable.prompt" "json"

echo "[nacos-init] done"
