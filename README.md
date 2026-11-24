# ACOJ

![JDK](https://img.shields.io/badge/JDK-21-007396?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-6DB33F?logo=springboot&logoColor=white)
![Go](https://img.shields.io/badge/Go-1.24-00ADD8?logo=go&logoColor=white)
![Vue](https://img.shields.io/badge/Vue-3-4FC08D?logo=vuedotjs&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-Supported-4479A1?logo=mysql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-Supported-DC382D?logo=redis&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-green)
![Version](https://img.shields.io/badge/version-1.0.0-orange)

**ACOJ** is an online judge platform for programming education and algorithm training. Java / Spring Boot powers the business and admin APIs; Go (go-zero) handles judging and code similarity; Vue 3 provides the admin and user frontends.

## Features

| Feature | Description |
| --- | --- |
| Problems & problem sets | Problems, tags, samples / test cases, difficulty levels |
| Multi-language judging | C / C++, Java, Python, Go, and more; sandboxed by `service-judge` |
| Users & permissions | Sign-up / login, roles, leaderboards, submission stats |
| Problem sets / contests | Custom sets, progress tracking, contest-related features |
| Async judging | Judge tasks via RabbitMQ for high-concurrency submissions |
| Plagiarism detection | Standalone `service-similarity` for similarity analysis |
| AI assistance | Optional LLM integration (explanations, suggestions; depends on deployment) |
| Dual frontends | `app-admin` console + `app-pc` user app |

## Tech Stack

| Layer | Technologies |
| --- | --- |
| Business backend | JDK 21 · Spring Boot 3.5 · Spring Cloud Alibaba · Maven |
| Storage / cache | MySQL · MyBatis-Plus · Redis / Redisson · Sa-Token |
| Middleware | Nacos (config & discovery) · RabbitMQ · MinIO |
| Judge / similarity | Go 1.24 · go-zero |
| Frontend | Vue 3 · TypeScript · Vite · Naive UI · Pinia · Monaco Editor |
| API docs | Knife4j |

## Project Structure

```text
acoj/
├── pom.xml                 # Maven aggregator root (revision 1.0.0)
├── service-system/         # Business backend (default port 89)
├── service-judge/          # Go judge service (default port 8888)
├── service-similarity/     # Go similarity service (default port 8882)
├── app-admin/              # Admin frontend (dev port 81)
├── app-pc/                 # User frontend (dev port 80)
├── sql/                    # Database scripts
├── antlrv4/                # Multi-language ANTLR-related resources
├── test/                   # Load tests and test-user generation
└── depoloy/                # Private deployment assets
```

See each module’s README for startup details.

## Prerequisites

| Component | Suggested version |
| --- | --- |
| JDK | 21 |
| Maven | 3.8+ |
| Go | 1.24+ |
| Node.js | 18+ (pnpm recommended) |
| MySQL | 8+ |
| Redis | Any working instance |
| RabbitMQ | Any working instance (management plugin recommended) |
| Nacos | 2.x (config + discovery) |
| MinIO | Enable as required by your config |

The judge host also needs language runtimes (e.g. `g++`, JDK, Python3, Go). See `service-judge/Dockerfile` for a full judge image.

Exact image tags for Docker Compose are listed in `deploy/docker/README.md`.

Published app images (via GitHub Actions → GHCR):

| Image | Registry |
| --- | --- |
| service-system | `ghcr.io/jiangbyte/acoj-service-system` |
| service-judge | `ghcr.io/jiangbyte/acoj-service-judge` |
| service-similarity | `ghcr.io/jiangbyte/acoj-service-similarity` |
| app-pc | `ghcr.io/jiangbyte/acoj-pc` |
| app-admin | `ghcr.io/jiangbyte/acoj-admin` |

```bash
make docker-build TAG=latest   # local build
make docker-push TAG=latest    # push to ghcr.io (needs packages:write)
```

## Quick Start (local)

Recommended order: **infrastructure → business backend → judge / similarity → frontends**.

### 1. Prepare infrastructure

Ensure these are up and reachable:

- MySQL
- Redis
- RabbitMQ
- Nacos (default `localhost:8848`)
- MinIO (if object storage is enabled)

Publish configs in the matching Nacos namespace (local default profile: `dev`):

| DataId | Purpose |
| --- | --- |
| `common.yaml` | Shared config (datasource, Redis, MQ, MinIO, etc.) |
| `service-system.yaml` | Business service config |
| `service-judge.yaml` | Judge runtime config (optional; local yaml alone is possible) |
| `service-similarity.yaml` | Similarity runtime config (optional) |

**local** uses infrastructure on your machine. **dev** runs the code locally against shared remote infrastructure. See **[docs/dev-start.md](docs/dev-start.md)** for startup instructions. Keep real host addresses only in gitignored `*-dev` files and remote Nacos; never commit them to the repository.

### 2. Initialize the database

```bash
mysql -u root -p -e "CREATE DATABASE acoj DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mysql -u root -p acoj < sql/acoj.sql
```

### 3. Start the business backend `service-system`

```bash
# From the repository root
mvn -pl service-system -am clean package -DskipTests
mvn -pl service-system spring-boot:run          # local (default)
mvn -pl service-system spring-boot:run -Pdev    # personal dev
```

Or:

```bash
java -jar service-system/target/service-system-1.0.0.jar
```

| Item | Default |
| --- | --- |
| URL | http://127.0.0.1:89 |
| Maven profile | `local` (default; use `-Pdev` on a personal machine) |
| Nacos configs | `common.yaml` + `service-system.yaml` |

More detail: [docs/dev-start.md](docs/dev-start.md), [service-system/README.md](service-system/README.md).

### 4. Start judge / similarity services

From the repository root, `make` starts against **dev** by default and also accepts `ENV=local`:

```bash
make judge                 # dev
make similarity            # dev

make judge ENV=local
make similarity ENV=local
```

The corresponding `*-dev.yaml` bootstrap files are gitignored. Create them from the checked-in `*.example` files before the first dev run.

Direct `go run` commands are also available:

```bash
# Judge (separate terminal)
cd service-judge
go run main.go -f etc/judge-local.yaml -nacos
# go run main.go -f etc/judge-dev.yaml -nacos

# Similarity (separate terminal, optional)
cd service-similarity
go run main.go -f etc/similar-local.yaml -nacos
# go run main.go -f etc/similar-dev.yaml -nacos
```

| Service | Default port | Docs |
| --- | --- | --- |
| service-judge | 8888 | [service-judge/readme.md](service-judge/readme.md) |
| service-similarity | 8882 | [service-similarity/readme.md](service-similarity/readme.md) |

On local WSL or without full cgroup permissions, set `Sandbox.Mode` to `soft` in the judge bootstrap yaml (or Nacos `service-judge.yaml`).

### 5. Start the frontends

```bash
# Admin → http://localhost:81
cd app-admin
pnpm install
pnpm dev

# User app → http://localhost:80
cd app-pc
pnpm install
pnpm dev
```

Point `VITE_GATEWAY` in `app-admin/.env.dev` and `app-pc/.env.dev` at the business API (locally usually `http://localhost:89`).

See: [app-admin/README.md](app-admin/README.md), [app-pc/README.md](app-pc/README.md).

## Default Ports

| Service | Port |
| --- | --- |
| service-system (business API) | 89 |
| admin (dev) | 81 |
| pc (dev) | 80 |
| service-judge | 8888 |
| service-similarity | 8882 |
| Nacos | 8848 |

## Load Testing & Test Data

`test/` includes test-user generation, batch register/login, and k6 concurrency scripts. See [test/README.md](test/README.md).

## License

This project is open source under the [MIT License](LICENSE).
