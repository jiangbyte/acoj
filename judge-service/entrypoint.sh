#!/bin/bash
set -e

# 创建临时cgroup
mkdir /sys/fs/cgroup/temp_group

# 将当前shell进程加入到cgroup中
echo 1 > /sys/fs/cgroup/temp_group/cgroup.procs

# 启用cpu、memory和io控制器
echo "+cpu +memory +io" > /sys/fs/cgroup/cgroup.subtree_control

# 启动应用
exec ./judge-service

# 启动bash
#exec /bin/bash