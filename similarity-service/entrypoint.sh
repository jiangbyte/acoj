#!/bin/bash
set -e

# 4 vCPU / 8 GiB 同机：限制 Go 运行时，避免查重任务把整机内存打满
export GOMAXPROCS="${GOMAXPROCS:-1}"
export GOMEMLIMIT="${GOMEMLIMIT:-384MiB}"

# 启动应用
exec ./similarity-service -f etc/similar.yaml -nacos

# 启动bash
#exec /bin/bash