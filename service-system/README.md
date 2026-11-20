# service-system — Business Backend

Core business service for ACOJ: auth, problems / problem sets / contests, submissions, judge scheduling, similarity tasks, system admin, and optional AI. Listens on port **89** by default; config is loaded from Nacos.

## Role

- Exposes `/api/v1/**` HTTP APIs for `app-pc` and `app-admin`
- Publishes judge and similarity tasks to RabbitMQ for Go consumers
- Integrates MySQL, Redis, MinIO, Nacos, and related infrastructure

## Prerequisites

- JDK **21**
- Maven **3.8+**
- Reachable MySQL, Redis, RabbitMQ, Nacos (and MinIO if needed)
- `common.yaml` and `service-system.yaml` published in Nacos

## Configuration

Bootstrap: `application.yaml` + profile overlay (`application-local.yaml` committed; `application-dev.yaml` gitignored — copy from `application-dev.yaml.example`).

| Item | Description |
| --- | --- |
| Port | `89` |
| Application name | `service-system` |
| Nacos | Address, namespace, credentials injected via Maven profiles |
| Remote config | `optional:nacos:common.yaml`, `optional:nacos:service-system.yaml` |

Maven profiles (root `pom.xml`) only set `spring.profiles.active`；Nacos 地址写在 `application-{profile}.yaml`：

| Profile | Description |
| --- | --- |
| `local`（默认） | `application-local.yaml`（可提交） |
| `dev` | `application-dev.yaml`（gitignore，从 example 复制） |
| `prod` / `docker` | 对应 overlay |

启动命令见 [docs/dev-start.md](../docs/dev-start.md)。`local` 连本机；`dev` 连共同远程（Nacos 下发的库/缓存也须是远程地址）。

## Database

Import the script from the repository root:

```bash
mysql -u root -p -e "CREATE DATABASE acoj DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mysql -u root -p acoj < ../sql/acoj.sql
```

Database name and credentials follow whatever is configured in Nacos.

## Start

From the **repository root**:

```bash
# Build
mvn -pl service-system -am clean package -DskipTests

# Local run (default -Plocal, committed config)
mvn -pl service-system spring-boot:run

# Personal machine (needs application-dev.yaml; not in git)
mvn -pl service-system spring-boot:run -Pdev

# Production profile
mvn -pl service-system spring-boot:run -Pprod
```

Or run the packaged jar:

```bash
java -jar service-system/target/service-system-1.0.0.jar
```

## Docker

This module includes a `Dockerfile`. Build the jar first:

```bash
# From the repository root
mvn -pl service-system -am clean package -DskipTests

cd service-system
docker build -t service-system:1.0.0 .
docker run -d --name service-system -p 89:89 service-system:1.0.0
```

The container still needs reachable Nacos / MySQL / Redis / RabbitMQ; fix networking and config accordingly.

> Note: Ensure the jar name in Dockerfile `COPY` matches the one in `ENTRYPOINT` (artifact is typically `service-system-1.0.0.jar`).

## Verify

- Service starts without errors and registers in Nacos as `service-system`
- Open `http://127.0.0.1:89`
- API docs (Knife4j): usually `http://127.0.0.1:89/doc.html` (if enabled)
- Health: `http://127.0.0.1:89/actuator/health`

## Troubleshooting

1. **Cannot reach Nacos / missing config**  
   Check `nacos.server-addr`, namespace, and credentials in the root `pom.xml` profile, and confirm `common.yaml` / `service-system.yaml` exist in Nacos.

2. **Database or Redis connection failure**  
   Use Nacos values; verify middleware host, database name, and passwords.

3. **Submissions never get judge results**  
   Confirm RabbitMQ is healthy and `service-judge` is running and consuming.

## Related Docs

- Overview: [../README.md](../README.md)
- Judge: [../service-judge/readme.md](../service-judge/readme.md)
- Similarity: [../service-similarity/readme.md](../service-similarity/readme.md)
- User app: [../app-pc/README.md](../app-pc/README.md)
- Admin: [../app-admin/README.md](../app-admin/README.md)
