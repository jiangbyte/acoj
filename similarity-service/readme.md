# similarity-service — Code Similarity Service

Go (go-zero) similarity worker: consumes plagiarism / similarity tasks, compares code, and writes results back. Default port **8882**. Start only if you need this feature.

## Role

- Consumes similarity analysis tasks from RabbitMQ
- Reads/writes related business data (e.g. code library, similarity task tables)
- Optional Nacos registration to work with the business backend

## Prerequisites

- Go **1.24+**
- Running MySQL, RabbitMQ, Nacos (same environment as `oj`)
- Runtime config prepared in Nacos (`similarity-service.yaml`)

## Configuration

Local bootstrap: `etc/similar.yaml`.

| Item | Default / notes |
| --- | --- |
| Host / Port | `0.0.0.0:8882` |
| Nacos | Address, namespace, `DataId: similarity-service.yaml` |

With `-nacos`, full runtime config (database, MQ, etc.) is loaded from Nacos. Keep the namespace aligned with the business backend.

## Start

```bash
cd similarity-service

# Recommended
go run main.go -f etc/similar.yaml -nacos

# Local yaml only (must include full runtime settings)
go run main.go -f etc/similar.yaml
```

Build a binary:

```bash
go build -o similarity-service main.go
./similarity-service -f etc/similar.yaml -nacos
```

> Always use `-f etc/similar.yaml`. Do not point at the judge service config path by mistake.

## Docker

```bash
cd similarity-service
DOCKER_BUILDKIT=1 docker build -t similarity-service:1.0.0 .

docker run -d --name similarity-service \
  -p 8882:8882 \
  similarity-service:1.0.0
```

The container must reach Nacos / MySQL / RabbitMQ. Resource limits via env (see `entrypoint.sh`):

| Variable | Default |
| --- | --- |
| `GOMAXPROCS` | `1` |
| `GOMEMLIMIT` | `384MiB` |

If the image entrypoint config path differs from local usage, follow the start commands in this README or override the process args at runtime.

## Verify

- Process stays healthy; consumer starts successfully
- Nacos shows `similarity-service` (if registration is enabled)
- Similarity tasks created from admin / business APIs update status correctly

## Troubleshooting

1. **Fails immediately on config load**  
   Confirm `-f` points to `etc/similar.yaml` and the Nacos DataId exists.

2. **Backlog / no consumption**  
   Check RabbitMQ queue names and routing keys against the `oj` similarity publish config.

3. **Database errors**  
   Confirm `sql/astro_code.sql` was imported and similarity tables / datasource settings in Nacos are correct.

## Related Docs

- Overview: [../README.md](../README.md)
- Business backend: [../oj/README.md](../oj/README.md)
- Judge: [../judge-service/Readme.md](../judge-service/Readme.md)
