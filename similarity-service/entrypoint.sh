#!/bin/bash
set -e

# 启动应用
exec ./similarity-service -f etc/judge.yaml -nacos

# 启动bash
#exec /bin/bash