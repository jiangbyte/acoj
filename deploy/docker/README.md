# Docker Compose

Single-node stack with infrastructure + OJ backend + judge + similarity.

Nacos configs are taken from `deploy/nacos_config_export.zip` (copied under
`deploy/docker/nacos/DEFAULT_GROUP/`), with hosts rewritten for Compose DNS
(`mysql` / `redis` / `rabbitmq` / `silo`). Infra passwords match the export:
`infra123!`, DB `astro_code`, RabbitMQ `admin`, Silo bucket `astro-code-oj`.

## Image versions

| Image | Tag |
|---|---|
| mysql | `9.2.0` |
| redis | `7.4.2` |
| rabbitmq | `4.0.6-management` |
| nacos/nacos-server | `v2.5.0` |
| service-system base | `bellsoft/liberica-openjdk-rocky:21.0.5` |
| judge / similarity base | `ubuntu:22.04` |
| pc / admin build | `node:22-alpine` |
| pc / admin runtime | `nginx:1.23.1-alpine` |

## Prerequisites

1. Docker Engine 24+ with Compose v2
2. Copy env file:

```bash
cp .env.example .env
```

3. `service-system` image is **multi-stage** (Maven package happens inside Docker).
   No host-side `mvn package` is required for Compose.

   Note: SCA 2025 ships Nacos client 3.x, which does not treat empty/`public`
   the same as Nacos Server 2.5. Compose therefore uses a real namespace ID
   (default `8fee08f3-44ea-4e26-a9b5-530c582330a3`).

## GHCR images

GitHub Actions (`.github/workflows/docker-publish.yml`) publishes:

| Image | Path |
|---|---|
| business API | `ghcr.io/jiangbyte/acoj-service-system` |
| judge | `ghcr.io/jiangbyte/acoj-service-judge` |
| similarity | `ghcr.io/jiangbyte/acoj-service-similarity` |
| PC | `ghcr.io/jiangbyte/acoj-pc` |
| Admin | `ghcr.io/jiangbyte/acoj-admin` |

Triggers: push to `jv_fixed`, version tags `v*`, or manual **workflow_dispatch**.

Local build / push:

```bash
make docker-build TAG=1.0.0
make docker-push TAG=1.0.0
```

Pull into Compose by setting image vars in `.env` (see `.env.example`), then:

```bash
docker compose pull
docker compose up -d
```

Private packages need `docker login ghcr.io` (GitHub PAT with `read:packages`).

## Start

应用镜像统一使用 GHCR 命名（`ghcr.io/jiangbyte/acoj-*`）。本地先构建再启动（`PULL_POLICY=build` 不会拉远程）：

```bash
cp .env.example .env   # 若还没有
docker compose up -d --build
```

确认无问题后再推仓库：

```bash
make docker-push TAG=latest
# 或: docker compose push
```

别人机器拉取已推送镜像时，设 `PULL_POLICY=missing`（或 `always`）后：

```bash
docker login ghcr.io
docker compose pull
docker compose up -d
```

Wait until `nacos-init` / `silo-init` finish and `service-system` becomes healthy:

```bash
docker compose ps
docker compose logs -f service-system
```

`nacos-init` bootstraps the Nacos 2.4+ admin password (default `nacos`/`nacos`),
ensures the Compose namespace exists, then publishes configs under `DEFAULT_GROUP`.

## Endpoints

| Service | URL |
|---|---|
| OJ API | http://localhost:89 |
| Nacos | http://localhost:8848/nacos (nacos/nacos) |
| RabbitMQ UI | http://localhost:15672 (admin/infra123!) |
| Silo Console | http://localhost:9001 (admin/infra123!) |
| Judge | http://localhost:8888 |
| Similarity | http://localhost:8882 |
| PC | http://localhost:80 |
| Admin | http://localhost:81 |

## Config layout

```text
deploy/
  nacos_config_export.zip   # source of truth for Nacos dataIds
  docker/
    nacos/
      .metadata.yml
      DEFAULT_GROUP/
        common.yaml
        service-system.yaml
        service-judge.yaml
        service-similarity.yaml
        spring.ai.alibaba.configurable.prompt
    judge/judge.yaml        # bootstrap (points to nacos)
    similarity/similar.yaml
    scripts/nacos-init.sh
```

Hosts inside the compose network use service DNS names: `mysql`, `redis`, `rabbitmq`, `nacos`, `silo`.

## Common issues

1. **service-system image build fails** — ensure Docker BuildKit is on and the build context is the repo root (`docker compose build service-system`).
2. **service-system cannot load config** — check `docker compose logs nacos-init`; configs come from the zip export under `DEFAULT_GROUP/`.
3. **Judge OOM / host freeze** — lower `JUDGE_GOMEMLIMIT` / disable similarity temporarily: `docker compose stop service-similarity`.
4. **Frontends call wrong API** — rebuild pc/admin with `VITE_GATEWAY=http://localhost:89` (or your server IP).
5. **LLM chat fails** — set `DASHSCOPE_API_KEY` in `.env` (or rely on key already in exported `service-system.yaml`) and recreate `service-system`.
6. **Credential mismatch after changing zip** — keep `.env` / compose defaults in sync with `common.yaml` / service YAMLs, then `docker compose down -v` before restarting.

## Stop / reset

```bash
docker compose down
# wipe data volumes
docker compose down -v
```
