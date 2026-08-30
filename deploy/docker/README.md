# Docker Compose (4 vCPU / 8 GiB)

Single-node stack with infrastructure + OJ backend + judge + similarity.

Nacos configs are taken from `deploy/nacos_config_export.zip` (copied under
`deploy/docker/nacos/DEFAULT_GROUP/`), with hosts rewritten for Compose DNS
(`mysql` / `redis` / `rabbitmq` / `minio`). Infra passwords match the export:
`infra@123`, DB `astro_code`, RabbitMQ `admin`, MinIO bucket `astro-code-oj`.

## Image versions

| Image | Tag |
|---|---|
| mysql | `9.2.0` |
| redis | `7.4.2` |
| rabbitmq | `4.0.6-management` |
| nacos/nacos-server | `v2.5.0` |
| oj base | `bellsoft/liberica-openjdk-rocky:21.0.5` |
| judge / similarity base | `ubuntu:22.04` |
| pc / admin build | `node:22-alpine` |
| pc / admin runtime | `nginx:1.23.1-alpine` |

## Memory budget (approx.)

| Service | Limit |
|---|---|
| MySQL | 768M |
| Redis | 128M |
| RabbitMQ | 320M |
| Nacos | 640M |
| MinIO | 256M |
| oj (JVM 1.5G heap) | 2304M |
| judge-service | 1024M |
| similarity-service | 512M |
| **Total peak** | **~6–7 GiB** |

Leave ~1 GiB for OS / page cache.

## Prerequisites

1. Docker Engine 24+ with Compose v2
2. Build OJ jar first (Dockerfile copies `oj/target/oj-1.0.0.jar`).
   Use `-Pdocker` (or `-Pdev`) so the jar uses the Compose Nacos namespace ID
   in `.env` / `NACOS_NAMESPACE` (`nacos:8848`, password `nacos`):

```bash
mvn -pl oj -am package -DskipTests -Pdocker
```

   Note: SCA 2025 ships Nacos client 3.x, which does not treat empty/`public`
   the same as Nacos Server 2.5. Compose therefore uses a real namespace ID
   (default `8fee08f3-44ea-4e26-a9b5-530c582330a3`).

3. Copy env file:

```bash
cp .env.example .env
```

## Start

```bash
# Core stack
docker compose up -d --build

# With PC + Admin frontends
docker compose --profile frontend up -d --build
```

Wait until `nacos-init` / `minio-init` finish and `oj` becomes healthy:

```bash
docker compose ps
docker compose logs -f oj
```

`nacos-init` bootstraps the Nacos 2.4+ admin password (default `nacos`/`nacos`),
ensures the Compose namespace exists, then publishes configs under `DEFAULT_GROUP`.

## Endpoints

| Service | URL |
|---|---|
| OJ API | http://localhost:89 |
| Nacos | http://localhost:8848/nacos (nacos/nacos) |
| RabbitMQ UI | http://localhost:15672 (admin/infra@123) |
| MinIO Console | http://localhost:9001 (admin/infra@123) |
| Judge | http://localhost:8888 |
| Similarity | http://localhost:8882 |
| PC (profile) | http://localhost:80 |
| Admin (profile) | http://localhost:81 |

## Demo data (judge + similarity)

After the stack is healthy:

```bash
bash deploy/docker/scripts/seed-demo.sh
```

This loads:

| What | ID / account |
|---|---|
| Judge user | `junit_judge` / `junit123456` |
| Judge problem | `junit_judge_a_plus_b` (A + B, C++/Java/…) |
| Sim user | `junit_sim` / `junit123456` |
| Sim peers | `junit_sim_base`, `junit_sim_c01`…`c04` / `junit123456` |
| Sim problem | `junit_sim_sum` (array sum samples) |

Login (test captcha bypass `9926`):

```bash
curl -sS -X POST http://localhost:89/api/v1/sys/auth/login \
  -H 'content-type: application/json' \
  -d '{"username":"junit_judge","password":"junit123456","platform":"CLIENT","captchaCode":"9926","uuid":"x"}'
```

Submit A+B (replace `$TOKEN`):

```bash
curl -sS -X POST http://localhost:89/api/v1/data/submit/execute \
  -H "Authorization: $TOKEN" -H 'content-type: application/json' \
  -d '{"judgeTaskId":"demo-1","problemId":"junit_judge_a_plus_b","moduleType":"PROBLEM","moduleId":"junit_judge_a_plus_b","language":"cpp","submitType":true,"code":"#include <iostream>\nint main(){long long a,b;std::cin>>a>>b;std::cout<<a+b<<std::endl;}"}'
```

Similarity batch (BASE vs identical C01 → expect ~1.0):

```bash
# login as junit_sim first, then:
curl -sS -X POST http://localhost:89/api/v1/task/similarity/batch \
  -H "Authorization: $TOKEN" -H 'content-type: application/json' \
  -d '{"moduleType":"PROBLEM","moduleId":"junit_sim_sum","problemId":"junit_sim_sum","language":"cpp","compareMode":"MULTI_BY_MULTI","userIds":["junit_sim_u_base","junit_sim_u_c01"],"minMatchLength":8}'
```

## Soft sandbox (optional)

If the host cannot run `--privileged` / cgroup, edit `docker-compose.yml` judge volume:

```yaml
- ./deploy/docker/judge/judge-soft.yaml:/app/etc/judge.yaml:ro
```

and remove `privileged: true`.

## Config layout

```text
deploy/
  nacos_config_export.zip   # source of truth for Nacos dataIds
  docker/
    nacos/
      .metadata.yml
      DEFAULT_GROUP/
        common.yaml
        oj.yaml
        judge-service.yaml
        similarity-service.yaml
        spring.ai.alibaba.configurable.prompt
    judge/judge.yaml        # bootstrap (points to nacos)
    judge/judge-soft.yaml
    similarity/similar.yaml
    scripts/nacos-init.sh
```

Hosts inside the compose network use service DNS names: `mysql`, `redis`, `rabbitmq`, `nacos`, `minio`.

## Common issues

1. **oj image build fails / jar missing** — run `mvn -pl oj -am package -DskipTests -Pdocker` first.
2. **oj cannot load config** — check `docker compose logs nacos-init`; configs come from the zip export under `DEFAULT_GROUP/`.
3. **Judge OOM / host freeze** — lower `JUDGE_GOMEMLIMIT` / disable similarity temporarily: `docker compose stop similarity-service`.
4. **Frontends call wrong API** — rebuild pc/admin with `VITE_GATEWAY=http://localhost:89` (or your server IP).
5. **LLM chat fails** — set `DASHSCOPE_API_KEY` in `.env` (or rely on key already in exported `oj.yaml`) and recreate `oj`.
6. **Credential mismatch after changing zip** — keep `.env` / compose defaults in sync with `common.yaml` / service YAMLs, then `docker compose down -v` before restarting.

## Stop / reset

```bash
docker compose down
# wipe data volumes
docker compose down -v
```
