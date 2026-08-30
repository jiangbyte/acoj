#!/bin/bash
# Pull ACOJ stack images via docker.xuanyuan.run, retag to local names used by compose/Dockerfiles.
set -euo pipefail

pull_tag() {
  src="$1"
  dst="$2"
  echo "===== ${dst} ====="
  docker pull "${src}"
  docker tag "${src}" "${dst}"
  docker rmi "${src}" || true
}

# Infrastructure (docker-compose.yml)
pull_tag docker.xuanyuan.run/library/mysql:9.2.0 mysql:9.2.0
pull_tag docker.xuanyuan.run/library/redis:7.4.2 redis:7.4.2
pull_tag docker.xuanyuan.run/library/rabbitmq:4.0.6-management rabbitmq:4.0.6-management
pull_tag docker.xuanyuan.run/nacos/nacos-server:v2.5.0 nacos/nacos-server:v2.5.0
pull_tag docker.xuanyuan.run/minio/minio:RELEASE.2024-04-18T19-09-19Z minio/minio:RELEASE.2024-04-18T19-09-19Z
pull_tag docker.xuanyuan.run/minio/mc:RELEASE.2024-04-18T16-45-29Z minio/mc:RELEASE.2024-04-18T16-45-29Z
pull_tag docker.xuanyuan.run/curlimages/curl:8.5.0 curlimages/curl:8.5.0

# Build bases (Dockerfiles)
pull_tag docker.xuanyuan.run/bellsoft/liberica-openjdk-rocky:21.0.5 bellsoft/liberica-openjdk-rocky:21.0.5
pull_tag docker.xuanyuan.run/library/ubuntu:22.04 ubuntu:22.04
pull_tag docker.xuanyuan.run/library/node:22-alpine node:22-alpine
pull_tag docker.xuanyuan.run/library/nginx:1.23.1-alpine nginx:1.23.1-alpine

echo "===== DONE ====="
docker images --format "table {{.Repository}}:{{.Tag}}\t{{.Size}}" | grep -E \
  'mysql:9\.2\.0|redis:7\.4\.2|rabbitmq:4\.0\.6|nacos-server:v2\.5\.0|minio:RELEASE\.2024-04-18|mc:RELEASE\.2024-04-18|curl:8\.5\.0|liberica-openjdk-rocky:21\.0\.5|ubuntu:22\.04|node:22-alpine|nginx:1\.23\.1-alpine|REPOSITORY'
