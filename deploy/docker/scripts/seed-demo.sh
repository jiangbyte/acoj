#!/usr/bin/env bash
# Seed demo problems/users for judge + similarity testing, then tokenize library samples
# via oj's library MQ consumer (production CodeTokenUtil).
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/../../.." && pwd)"
cd "$ROOT"

MYSQL_ROOT_PASSWORD="${MYSQL_ROOT_PASSWORD:-infra@123}"
MYSQL_DATABASE="${MYSQL_DATABASE:-astro_code}"
RABBITMQ_USER="${RABBITMQ_USER:-admin}"
RABBITMQ_PASSWORD="${RABBITMQ_PASSWORD:-infra@123}"
OJ_URL="${OJ_URL:-http://127.0.0.1:89}"
RMQ_API="${RMQ_API:-http://127.0.0.1:15672}"

if [ -f .env ]; then
  set -a
  # shellcheck disable=SC1091
  . ./.env
  set +a
fi

MYSQL_ROOT_PASSWORD="${MYSQL_ROOT_PASSWORD:-infra@123}"
MYSQL_DATABASE="${MYSQL_DATABASE:-astro_code}"
RABBITMQ_USER="${RABBITMQ_USER:-admin}"
RABBITMQ_PASSWORD="${RABBITMQ_PASSWORD:-infra@123}"

echo "== import SQL =="
docker compose exec -T mysql mysql -uroot -p"${MYSQL_ROOT_PASSWORD}" "${MYSQL_DATABASE}" \
  < deploy/docker/sql/01-judge-demo.sql
docker compose exec -T mysql mysql -uroot -p"${MYSQL_ROOT_PASSWORD}" "${MYSQL_DATABASE}" \
  < deploy/docker/sql/02-similarity-demo.sql

# Drop pre-seeded null-token library rows; MQ consumer will insert tokenized ones.
docker compose exec -T mysql mysql -uroot -p"${MYSQL_ROOT_PASSWORD}" "${MYSQL_DATABASE}" -e \
  "DELETE FROM data_library WHERE problem_id='junit_sim_sum' OR id LIKE 'junit_sim_lib_%';"

publish_one() {
  local user_id="$1"
  local code_file="$2"
  local body
  body="$(CODE="$(cat "$code_file")" USER_ID="$user_id" python3 - <<'PY'
import json, os
payload = {
  "userId": os.environ["USER_ID"],
  "problemId": "junit_sim_sum",
  "moduleType": "PROBLEM",
  "moduleId": "junit_sim_sum",
  "language": "cpp",
  "code": os.environ["CODE"],
}
body = {
  "properties": {
    "content_type": "application/json",
    "headers": {"__TypeId__": "io.charlie.web.modular.task.library.dto.Library"},
  },
  "routing_key": "common.library.routing",
  "payload": json.dumps(payload, ensure_ascii=False),
  "payload_encoding": "string",
}
print(json.dumps(body, ensure_ascii=False))
PY
)"
  curl -fsS -u "${RABBITMQ_USER}:${RABBITMQ_PASSWORD}" \
    -H 'content-type: application/json' \
    -X POST "${RMQ_API}/api/exchanges/%2F/common.library.exchange/publish" \
    -d "${body}" >/dev/null
  echo "[seed] library MQ published for ${user_id}"
}

TMP="$(mktemp -d)"
trap 'rm -rf "$TMP"' EXIT

cat >"$TMP/base.cpp" <<'EOF'
#include <iostream>
using namespace std;
int sumArray(int numbers[], int length) {
    int total = 0;
    for (int i = 0; i < length; i++) {
        total += numbers[i];
    }
    return total;
}
int main() {
    int arr[5] = {1, 2, 3, 4, 5};
    cout << sumArray(arr, 5) << endl;
    return 0;
}
EOF

cat >"$TMP/c02.cpp" <<'EOF'
#include <string>

std::string buildGreeting(const std::string& name) {
    if (name.empty()) {
        return std::string("guest");
    }
    return std::string("hello-") + name;
}

int main() {
    std::string message = buildGreeting("acoj");
    return message.size() > 0 ? 0 : 1;
}
EOF

cat >"$TMP/c03.cpp" <<'EOF'
#include <iostream>
using namespace std;
int accumulateList(int vals[], int n) {
    int result = 0;
    for (int idx = 0; idx < n; idx++) {
        result += vals[idx];
    }
    return result;
}
int main() {
    int data[5] = {1, 2, 3, 4, 5};
    cout << accumulateList(data, 5) << endl;
    return 0;
}
EOF

cat >"$TMP/c04.cpp" <<'EOF'
#include <iostream>
using namespace std;

// Sum all array elements
int sumArray(int numbers[], int length) {
    int total = 0;   // accumulator

    for (int i = 0; i < length; i++) {
            total += numbers[i];
    }

    return total;
}

int main() {
    /* test data */
    int arr[5] = {1, 2, 3, 4, 5};

    cout << sumArray(arr, 5) << endl;
    return 0;
}
EOF

echo "== tokenize similarity library via MQ =="
publish_one "junit_sim_u_base" "$TMP/base.cpp"
publish_one "junit_sim_u_c01" "$TMP/base.cpp"
publish_one "junit_sim_u_c02" "$TMP/c02.cpp"
publish_one "junit_sim_u_c03" "$TMP/c03.cpp"
publish_one "junit_sim_u_c04" "$TMP/c04.cpp"

echo "== wait for tokens =="
cnt=0
i=0
while [ "$i" -lt 30 ]; do
  cnt="$(docker compose exec -T mysql mysql -N -uroot -p"${MYSQL_ROOT_PASSWORD}" "${MYSQL_DATABASE}" \
    -e "SELECT COUNT(*) FROM data_library WHERE problem_id='junit_sim_sum' AND code_token IS NOT NULL;" 2>/dev/null | tr -d '\r' | tail -1)"
  if [ "${cnt:-0}" -ge 5 ] 2>/dev/null; then
    echo "[seed] tokenized library rows=${cnt}"
    break
  fi
  i=$((i + 1))
  sleep 1
done
if [ "${cnt:-0}" -lt 5 ] 2>/dev/null; then
  echo "[seed] warning: expected 5 tokenized libraries, got ${cnt:-0}"
  echo "[seed] check: docker compose logs oj --tail 50"
fi

echo "== smoke: login + A+B submit =="
TOKEN="$(curl -fsS -X POST "${OJ_URL}/api/v1/sys/auth/login" \
  -H 'content-type: application/json' \
  -d '{"username":"junit_judge","password":"junit123456","platform":"CLIENT","captchaCode":"9926","uuid":"seed"}' \
  | python3 -c 'import sys,json; r=json.load(sys.stdin); print(r.get("data") or "")')"
if [ -z "${TOKEN}" ]; then
  echo "[seed] login failed"
  exit 1
fi

JUDGE_TASK_ID="seed-$(date +%s)"
SUBMIT_BODY="$(python3 - <<PY
import json
print(json.dumps({
  "judgeTaskId": "${JUDGE_TASK_ID}",
  "problemId": "junit_judge_a_plus_b",
  "moduleType": "PROBLEM",
  "moduleId": "junit_judge_a_plus_b",
  "language": "cpp",
  "submitType": True,
  "code": """#include <iostream>
int main() {
    long long a, b;
    std::cin >> a >> b;
    std::cout << a + b << std::endl;
    return 0;
}
"""
}))
PY
)"
SUBMIT_JSON="$(curl -fsS -X POST "${OJ_URL}/api/v1/data/submit/execute" \
  -H "content-type: application/json" \
  -H "Authorization: ${TOKEN}" \
  -d "${SUBMIT_BODY}")"
SUBMIT_ID="$(printf '%s' "${SUBMIT_JSON}" | python3 -c 'import sys,json; r=json.load(sys.stdin); print(r.get("data") or "")')"
echo "[seed] submit id=${SUBMIT_ID:-unknown} task=${JUDGE_TASK_ID}"

STATUS=""
i=0
while [ "$i" -lt 40 ]; do
  STATUS="$(docker compose exec -T mysql mysql -N -uroot -p"${MYSQL_ROOT_PASSWORD}" "${MYSQL_DATABASE}" \
    -e "SELECT status FROM data_submit WHERE judge_task_id='${JUDGE_TASK_ID}' LIMIT 1;" 2>/dev/null | tr -d '\r' | tail -1 || true)"
  if [ -n "${STATUS}" ] && [ "${STATUS}" != "PENDING" ] && [ "${STATUS}" != "JUDGING" ]; then
    break
  fi
  i=$((i + 1))
  sleep 2
done
echo "[seed] judge status=${STATUS:-pending}"

echo
echo "Demo data ready."
echo "  Judge user : junit_judge / junit123456   problem=junit_judge_a_plus_b (A+B)"
echo "  Sim user   : junit_sim / junit123456     problem=junit_sim_sum"
echo "  Sim peers  : junit_sim_base / c01 / c02 / c03 / c04  (password junit123456)"
echo "  Login tip  : platform=CLIENT, captchaCode=9926 (test bypass)"
echo "  Docs       : deploy/docker/README.md → Demo data"
