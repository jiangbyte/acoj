#!/bin/bash

# 一键启动开发环境容器脚本
echo "🚀 正在启动开发环境容器..."

# 定义容器名称数组
containers=("redis-dev" "rabbitmq-dev" "nacos-dev" "mysql8-dev")

# 遍历并启动每个容器
for container in "${containers[@]}"
do
    echo "➡️  启动容器: $container"
    docker start "$container"

    # 检查启动是否成功
    if [ $? -eq 0 ]; then
        echo "✅ $container 启动成功"
    else
        echo "❌ $container 启动失败"
    fi
done

echo ""
echo "🎉 所有容器启动完成！"
echo "📊 当前运行中的容器："
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"