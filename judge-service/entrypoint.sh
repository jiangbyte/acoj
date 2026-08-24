#!/bin/bash
set -e

# 4 vCPU / 8 GiB 同机：限制 Go 运行时，给 javac/用户程序和其它容器留内存
export GOMAXPROCS="${GOMAXPROCS:-2}"
export GOMEMLIMIT="${GOMEMLIMIT:-512MiB}"

# 创建临时cgroup
mkdir /sys/fs/cgroup/temp_group

# 将当前shell进程加入到cgroup中
echo 1 > /sys/fs/cgroup/temp_group/cgroup.procs

# 启用cpu、memory和io控制器
echo "+cpu +memory +io" > /sys/fs/cgroup/cgroup.subtree_control

# 启动应用
exec ./judge-service -f etc/judge.yaml -nacos

# 启动bash
#exec /bin/bash