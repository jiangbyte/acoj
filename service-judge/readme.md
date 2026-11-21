# service-judge — Judge Service

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

Bootstrap: `etc/judge-local.yaml` (committed) or `etc/judge-dev.yaml` (gitignored; copy from `judge-dev.yaml.example`).

| Item | Default / notes |
| --- | --- |
| Host / Port | `0.0.0.0:8888` |
| Sandbox.Mode | `auto` or `cgroup`: use cgroup v2 if the host delegates it, otherwise cgroup v1 |
| Nacos | Address, namespace, `DataId: service-judge.yaml` |

With `-nacos`, runtime config is loaded from Nacos `service-judge.yaml` (MySQL, Redis, RabbitMQ, language commands, etc.). Keep the namespace aligned with `service-system`.

## Start

```bash
cd service-judge

# Default: committed local bootstrap + Nacos runtime
go run main.go -f etc/judge-local.yaml -nacos

# Personal machine (etc/judge-dev.yaml is gitignored)
go run main.go -f etc/judge-dev.yaml -nacos
```

Build a binary:

```bash
go build -o service-judge main.go
./service-judge -f etc/judge-local.yaml -nacos
```

## Docker

The image includes multi-language runtimes and enables cgroup under privilege (see `entrypoint.sh`).

```bash
cd service-judge

# BuildKit recommended
DOCKER_BUILDKIT=1 docker build -t service-judge:1.0.0 .

docker run -d --privileged --name service-judge \
  -p 8888:8888 \
  service-judge:1.0.0
```

The container must reach Nacos / MySQL / Redis / RabbitMQ. The entrypoint runs:

```text
./service-judge -f etc/judge-local.yaml -nacos
```

Optional Go runtime limits (defaults in entrypoint):

| Variable | Default |
| --- | --- |
| `GOMAXPROCS` | `2` |
| `GOMEMLIMIT` | `512MiB` |

## Verify

- Process stays up; logs show the consumer started
- Nacos shows instance `service-judge` (if registration is enabled)
- User submissions receive judge results in a reasonable time

## Troubleshooting

1. **Missing memory limit files / cgroup errors**  
   Parent cgroup did not delegate `memory`/`cpu`, or the process lacks privilege. Run with `--privileged` and initialize subtree control as in `entrypoint.sh`.

2. **A language fails to compile**  
   Confirm the toolchain is installed on the host/image and language commands in Nacos are correct.

3. **No tasks consumed**  
   Check RabbitMQ connectivity and that queue / routing keys match the `service-system` side.

## Related Docs

- Overview: [../README.md](../README.md)
- Business backend: [../service-system/README.md](../service-system/README.md)
- Similarity: [../service-similarity/readme.md](../service-similarity/readme.md)
