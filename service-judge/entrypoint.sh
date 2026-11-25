#!/bin/bash
set -e

export GOMAXPROCS="${GOMAXPROCS:-2}"
export GOMEMLIMIT="${GOMEMLIMIT:-384MiB}"

# 创建临时cgroup
mkdir /sys/fs/cgroup/temp_group

# 将当前shell进程加入到cgroup中
echo 1 > /sys/fs/cgroup/temp_group/cgroup.procs

# 启用 cpu、memory、io、pids 控制器（pids 用于单次评测进程数上限）
echo "+cpu +memory +io +pids" > /sys/fs/cgroup/cgroup.subtree_control

# 启动应用
exec ./service-judge -f etc/judge-local.yaml -nacos

# 启动bash
#exec /bin/bash