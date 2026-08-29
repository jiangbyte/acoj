# Astro Code OJ (AC OJ)

![JDK](https://img.shields.io/badge/JDK-21-007396?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-6DB33F?logo=springboot&logoColor=white)
![Go](https://img.shields.io/badge/Go-1.24-00ADD8?logo=go&logoColor=white)
![Vue](https://img.shields.io/badge/Vue-3-4FC08D?logo=vuedotjs&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-Supported-4479A1?logo=mysql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-Supported-DC382D?logo=redis&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-green)
![Version](https://img.shields.io/badge/version-1.0.0-orange)

**Astro Code OJ (AC OJ)** is an online judge platform for programming education and algorithm training. Java / Spring Boot powers the business and admin APIs; Go (go-zero) handles judging and code similarity; Vue 3 provides the admin and user frontends.

## Features

| Feature | Description |
| --- | --- |
| Problems & problem sets | Problems, tags, samples / test cases, difficulty levels |
| Multi-language judging | C / C++, Java, Python, Go, and more; sandboxed by `judge-service` |
| Users & permissions | Sign-up / login, roles, leaderboards, submission stats |
| Problem sets / contests | Custom sets, progress tracking, contest-related features |
| Async judging | Judge tasks via RabbitMQ for high-concurrency submissions |
| Plagiarism detection | Standalone `similarity-service` for similarity analysis |
| AI assistance | Optional LLM integration (explanations, suggestions; depends on deployment) |
| Dual frontends | `admin` console + `pc` user app |

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
astro-code-oj/
├── pom.xml                 # Maven aggregator root (revision 1.0.0)
├── oj/                     # Business backend (default port 89)
├── judge-service/          # Go judge service (default port 8888)
├── similarity-service/     # Go similarity service (default port 8882)
├── admin/                  # Admin frontend (dev port 81)
├── pc/                     # User frontend (dev port 80)
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
| RabbitMQ | Any working instance |
| Nacos | 2.x (config + discovery) |
| MinIO | Enable as required by your config |

The judge host also needs language runtimes (e.g. `g++`, JDK, Python3, Go). See `judge-service/Dockerfile` for a full judge image.

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
| `oj.yaml` | Business service config |
| `judge-service.yaml` | Judge runtime config (optional; local yaml alone is possible) |
| `similarity-service.yaml` | Similarity runtime config (optional) |

Local `dev` default Nacos namespace ID: `8fee08f3-44ea-4e26-a9b5-530c582330a3` (credentials are in the root `pom.xml` `dev` profile; default `nacos` / `123456`). Adjust for your environment.

### 2. Initialize the database

```bash
mysql -u root -p -e "CREATE DATABASE astro_code DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mysql -u root -p astro_code < sql/astro_code.sql
```

### 3. Start the business backend `oj`

```bash
# From the repository root
mvn -pl oj -am clean package -DskipTests
mvn -pl oj spring-boot:run
```

Or:

```bash
java -jar oj/target/oj-1.0.0.jar
```

| Item | Default |
| --- | --- |
| URL | http://127.0.0.1:89 |
| Maven profile | `dev` (use `prod`: `mvn -Pprod ...`) |
| Nacos configs | `common.yaml` + `oj.yaml` |

More detail: [oj/README.md](oj/README.md).

### 4. Start judge / similarity services

```bash
# Judge (separate terminal)
cd judge-service
go run main.go -f etc/judge.yaml -nacos

# Similarity (separate terminal, optional)
cd similarity-service
go run main.go -f etc/similar.yaml -nacos
```

| Service | Default port | Docs |
| --- | --- | --- |
| judge-service | 8888 | [judge-service/Readme.md](judge-service/Readme.md) |
| similarity-service | 8882 | [similarity-service/Readme.md](similarity-service/Readme.md) |

On local WSL or without full cgroup permissions, set `Sandbox.Mode` to `soft` in `judge-service/etc/judge.yaml`.

### 5. Start the frontends

```bash
# Admin → http://localhost:81
cd admin
pnpm install
pnpm dev

# User app → http://localhost:80
cd pc
pnpm install
pnpm dev
```

Point `VITE_GATEWAY` in `admin/.env.dev` and `pc/.env.dev` at the business API (locally usually `http://localhost:89`).

See: [admin/README.md](admin/README.md), [pc/README.md](pc/README.md).

## Default Ports

| Service | Port |
| --- | --- |
| oj (business API) | 89 |
| admin (dev) | 81 |
| pc (dev) | 80 |
| judge-service | 8888 |
| similarity-service | 8882 |
| Nacos | 8848 |

## Load Testing & Test Data

`test/` includes test-user generation, batch register/login, and k6 concurrency scripts. See [test/README.md](test/README.md).

## License

This project is open source under the [MIT License](LICENSE).
