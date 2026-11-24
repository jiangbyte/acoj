#!/bin/bash
set -e

export GOMAXPROCS="${GOMAXPROCS:-1}"
export GOMEMLIMIT="${GOMEMLIMIT:-384MiB}"

# 启动应用
exec ./service-similarity -f etc/similar-local.yaml -nacos

# 启动bash
#exec /bin/bash