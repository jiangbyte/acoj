#!/bin/bash

# 一键关闭开发环境容器脚本
echo "🛑 正在停止开发环境容器..."

# 定义容器名称数组
containers=("redis-dev" "rabbitmq-dev" "nacos-dev" "mysql8-dev")

# 遍历并停止每个容器
for container in "${containers[@]}"
do
    echo "➡️  停止容器: $container"
    docker stop "$container"

    # 检查停止是否成功
    if [ $? -eq 0 ]; then
        echo "✅ $container 停止成功"
    else
        echo "❌ $container 停止失败（可能原本未运行）"
    fi
done

echo ""
echo "🔚 所有容器已停止"
echo "📊 当前容器状态："
docker ps -a --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | grep -E "(redis-dev|rabbitmq-dev|nacos-dev|mysql8-dev|NAMES)"