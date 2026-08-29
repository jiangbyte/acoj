# oj — Business Backend

Core business service for Astro Code OJ: auth, problems / problem sets / contests, submissions, judge scheduling, similarity tasks, system admin, and optional AI. Listens on port **89** by default; config is loaded from Nacos.

## Role

- Exposes `/api/v1/**` HTTP APIs for `pc` and `admin`
- Publishes judge and similarity tasks to RabbitMQ for Go consumers
- Integrates MySQL, Redis, MinIO, Nacos, and related infrastructure

## Prerequisites

- JDK **21**
- Maven **3.8+**
- Reachable MySQL, Redis, RabbitMQ, Nacos (and MinIO if needed)
- `common.yaml` and `oj.yaml` published in Nacos

## Configuration

Local bootstrap: `src/main/resources/application.yaml`.

| Item | Description |
| --- | --- |
| Port | `89` |
| Application name | `oj` |
| Nacos | Address, namespace, credentials injected via Maven profiles |
| Remote config | `optional:nacos:common.yaml`, `optional:nacos:oj.yaml` |

Maven profiles (root `pom.xml`):

| Profile | Description |
| --- | --- |
| `dev` (default) | Nacos `localhost:8848`, dev namespace |
| `prod` | Production Nacos address and namespace |

Before starting, ensure Nacos datasource, Redis, RabbitMQ, MinIO, and Sa-Token settings match your environment.

## Database

Import the script from the repository root:

```bash
mysql -u root -p -e "CREATE DATABASE astro_code DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mysql -u root -p astro_code < ../sql/astro_code.sql
```

Database name and credentials follow whatever is configured in Nacos.

## Start

From the **repository root**:

```bash
# Build
mvn -pl oj -am clean package -DskipTests

# Dev run (default -Pdev)
mvn -pl oj spring-boot:run

# Production profile
mvn -pl oj spring-boot:run -Pprod
```

Or run the packaged jar:

```bash
java -jar oj/target/oj-1.0.0.jar
```

## Docker

This module includes a `Dockerfile`. Build the jar first:

```bash
# From the repository root
mvn -pl oj -am clean package -DskipTests

cd oj
docker build -t oj:1.0.0 .
docker run -d --name oj -p 89:89 oj:1.0.0
```

The container still needs reachable Nacos / MySQL / Redis / RabbitMQ; fix networking and config accordingly.

> Note: Ensure the jar name in Dockerfile `COPY` matches the one in `ENTRYPOINT` (artifact is typically `oj-1.0.0.jar`).

## Verify

- Service starts without errors and registers in Nacos as `oj`
- Open `http://127.0.0.1:89`
- API docs (Knife4j): usually `http://127.0.0.1:89/doc.html` (if enabled)
- Health: `http://127.0.0.1:89/actuator/health`

## Troubleshooting

1. **Cannot reach Nacos / missing config**  
   Check `nacos.server-addr`, namespace, and credentials in the root `pom.xml` profile, and confirm `common.yaml` / `oj.yaml` exist in Nacos.

2. **Database or Redis connection failure**  
   Use Nacos values; verify middleware host, database name, and passwords.

3. **Submissions never get judge results**  
   Confirm RabbitMQ is healthy and `judge-service` is running and consuming.

## Related Docs

- Overview: [../README.md](../README.md)
- Judge: [../judge-service/Readme.md](../judge-service/Readme.md)
- Similarity: [../similarity-service/Readme.md](../similarity-service/Readme.md)
- User app: [../pc/README.md](../pc/README.md)
- Admin: [../admin/README.md](../admin/README.md)
