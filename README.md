# Astro Code OJ (AC OJ)

![JDK](https://img.shields.io/badge/JDK-21-007396?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-6DB33F?logo=springboot&logoColor=white)
![Go](https://img.shields.io/badge/Go-1.24-00ADD8?logo=go&logoColor=white)
![Vue](https://img.shields.io/badge/Vue-3-4FC08D?logo=vuedotjs&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-Supported-4479A1?logo=mysql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-Supported-DC382D?logo=redis&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-green)
![Version](https://img.shields.io/badge/version-1.0.0-orange)

**Astro Code OJ (AC OJ)** is an online judge platform for programming education and algorithm training. Java / Spring Cloud powers the business and admin APIs; Go (go-zero) handles judging and code similarity; Vue 3 provides the admin and user frontends. It supports multi-language judging, problem sets and contests, async judge scheduling, and optional AI-assisted features.

> Current version: `1.0.0` · License: [MIT License](LICENSE) · Repo: [jiangbyte/acoj](https://github.com/jiangbyte/acoj)

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Quick Start](#quick-start)
- [Performance Testing](#performance-testing)
- [License](#license)

## Features

| Module | Description |
| --- | --- |
| Problem system | CRUD for problems, tags, samples / test cases, difficulty levels |
| Multi-language judging | C / C++, Java, Python, Go, and more; executed by the Go judge service |
| Users & permissions | Roles, leaderboards, learning progress, and submission stats |
| Problem sets / contests | Custom problem sets, progress tracking, contest-related data models |
| Async judging | RabbitMQ task delivery for high-concurrency submissions |
| Similarity detection | Standalone similarity-service for plagiarism analysis |
| AI assistance | Optional LLM integration for problem explanations and code suggestions (enabled by deployment config) |
| Dual frontends | `admin` console + `pc` user app (Vue 3 / Naive UI / Monaco) |

## Tech Stack

| Layer | Technologies |
| --- | --- |
| Business backend | JDK 21 · Spring Boot 3.2 · Spring Cloud · Maven multi-module |
| Persistence / cache | MySQL · MyBatis-Plus · Redis / Redisson · Sa-Token |
| Middleware | Nacos (config & discovery) · RabbitMQ · MinIO (object storage) |
| Judge / similarity | Go 1.24 · go-zero · ANTLR grammars (`antlrv4/`) |
| Frontend | Vue 3 · TypeScript · Vite · Naive UI · Pinia · Monaco Editor |
| Docs | Knife4j |

## Project Structure

```text
astro-code-oj/
├── pom.xml                 # Maven aggregator root (revision 1.0.0)
├── galaxy-dependencies/    # Dependency BOM
├── galaxy-common/          # Shared framework (base-framework)
├── galaxy-oj/              # Main business service (default port 89)
├── judge-service/          # Go judge service
├── similarity-service/     # Go similarity service
├── admin/                  # Vue 3 admin frontend
├── pc/                     # Vue 3 user frontend
├── sql/                    # Database scripts
├── antlrv4/                # Multi-language ANTLR grammars
├── test/                   # Load testing and user data generation
│   ├── User Generate/      # Generate / register / fill tokens
│   └── ojtest/             # k6 load tests and summary charts
└── depoloy/                # Deployment assets (local/private, gitignored by default)
```

## Quick Start

### Prerequisites

- JDK **21**, Maven **3.8+**
- MySQL **8+**, Redis, RabbitMQ, Nacos
- Go **1.24+** (judge / similarity services)
- Node.js **18+**, pnpm (frontends)

### 1. Initialize the database

Import a script from `sql/` (pick the latest available for your environment, e.g. `sql/astro_code_05.sql`):

```bash
mysql -u root -p -e "CREATE DATABASE astro_code DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mysql -u root -p astro_code < sql/astro_code_05.sql
```

### 2. Configure and start the business backend

`galaxy-oj` loads `astro-code-common.yaml` and `galaxy-oj.yaml` from Nacos (see `galaxy-oj/src/main/resources/application.yaml`). The local default Maven profile is `dev`; the default Nacos address is `localhost:8848`.

```bash
# From the repository root
mvn -pl galaxy-oj -am clean package -DskipTests
mvn -pl galaxy-oj spring-boot:run
```

| Item | Default |
| --- | --- |
| API | http://127.0.0.1:89 |
| Profile | `dev` (see root `pom.xml`) |

### 3. Start judge / similarity services

```bash
# Judge
cd judge-service
go run main.go -f etc/judge.yaml -nacos

# Similarity (separate terminal)
cd similarity-service
go run main.go -f etc/similar.yaml
```

See each service's `etc/*.yaml` for config (adjust Nacos address, namespace, etc. for your environment).

### 4. Start the frontends

```bash
# Admin
cd admin
pnpm install
pnpm dev          # reads VITE_GATEWAY from .env.dev by default

# User app
cd pc
pnpm install
pnpm dev
```

Point `VITE_GATEWAY` in `admin/.env.dev` / `pc/.env.dev` to your local gateway or `galaxy-oj` address (e.g. `http://localhost:89`).

## Performance Testing

The repo includes a k6 load-testing pipeline under `test/`:

| Path | Purpose |
| --- | --- |
| `test/User Generate/user_gen.py` | Generate `测试用户数据_1000个.csv` |
| `test/User Generate/register.py` | Batch registration |
| `test/User Generate/login.py` | Log in and write tokens back to the CSV |
| `test/ojtest/{100..250}/` | Per-concurrency k6 scripts and raw results |
| `test/ojtest/analyze_k6_batch.py` | Aggregate CSVs and trend charts |

Run `run-k6-tests.ps1` in the target concurrency directory (the script locates `optimized-oj-test.js` via `$PSScriptRoot`). For summaries, run the analysis script under `test/ojtest`.

## License

This project is open source under the [MIT License](LICENSE).
