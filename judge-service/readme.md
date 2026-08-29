# judge-service — Judge Service

Go (go-zero) judge worker: consumes judge tasks from RabbitMQ, compiles and runs user code in a sandbox, compares test cases, and writes results back. Default port **8888**.

## Role

- Consumes judge messages published by the business backend
- Multi-language compile & run (C/C++, Java, Python, Go, etc.; per actual config)
- Resource limits via cgroup or soft mode
- Optional Nacos registration for service discovery

## Prerequisites

- Go **1.24+**
- Running MySQL, Redis, RabbitMQ, Nacos (same environment as the business side)
- Compilers / runtimes installed on the host, e.g.:
  - `g++` / `gcc`
  - JDK (OpenJDK 17 in the Docker image)
  - `python3`
  - Go

For full judging on Linux / containers, prefer **privileged** mode or writable cgroup v2. On local WSL, use soft sandbox mode.

## Configuration

Local bootstrap: `etc/judge.yaml`.

| Item | Default / notes |
| --- | --- |
| Host / Port | `0.0.0.0:8888` |
| Sandbox.Mode | `auto`: use cgroup if writable, else fall back to `soft`; or force `cgroup` / `soft` |
| Nacos | Address, namespace, `DataId: judge-service.yaml` |

With `-nacos`, runtime config is loaded from Nacos `judge-service.yaml` (MySQL, Redis, RabbitMQ, language commands, etc.). Keep the namespace aligned with `oj`.

Local WSL example:

```yaml
Sandbox:
  Mode: soft
```

## Start

```bash
cd judge-service

# Recommended: local yaml + Nacos runtime config
go run main.go -f etc/judge.yaml -nacos

# Local yaml only (must contain full runtime settings)
go run main.go -f etc/judge.yaml
```

Build a binary:

```bash
go build -o judge-service main.go
./judge-service -f etc/judge.yaml -nacos
```

## Docker

The image includes multi-language runtimes and enables cgroup under privilege (see `entrypoint.sh`).

```bash
cd judge-service

# BuildKit recommended
DOCKER_BUILDKIT=1 docker build -t judge-service:1.0.0 .

docker run -d --privileged --name judge-service \
  -p 8888:8888 \
  judge-service:1.0.0
```

The container must reach Nacos / MySQL / Redis / RabbitMQ. The entrypoint runs:

```text
./judge-service -f etc/judge.yaml -nacos
```

Optional Go runtime limits (defaults in entrypoint):

| Variable | Default |
| --- | --- |
| `GOMAXPROCS` | `2` |
| `GOMEMLIMIT` | `512MiB` |

## Verify

- Process stays up; logs show the consumer started
- Nacos shows instance `judge-service` (if registration is enabled)
- User submissions receive judge results in a reasonable time

## Troubleshooting

1. **Missing memory limit files / cgroup errors**  
   Parent cgroup did not delegate `memory`/`cpu`, or the process lacks privilege. Use `Sandbox.Mode: soft`, or run with `--privileged` and initialize subtree control as in `entrypoint.sh`.

2. **A language fails to compile**  
   Confirm the toolchain is installed on the host/image and language commands in Nacos are correct.

3. **No tasks consumed**  
   Check RabbitMQ connectivity and that queue / routing keys match the `oj` side.

## Related Docs

- Overview: [../README.md](../README.md)
- Business backend: [../oj/README.md](../oj/README.md)
- Similarity: [../similarity-service/Readme.md](../similarity-service/Readme.md)
