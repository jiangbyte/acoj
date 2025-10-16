#cd /mnt/e/MyProjects/astro-code-oj/galaxy-web-app/galaxy-gateway
#docker build -t registry.cn-beijing.aliyuncs.com/jiangbyte/galaxy_oj_gateway:1.0.0 .
#docker push registry.cn-beijing.aliyuncs.com/jiangbyte/galaxy_oj_gateway:1.0.0
#
#cd /mnt/e/MyProjects/astro-code-oj/galaxy-web-app/galaxy-oj
#docker build -t registry.cn-beijing.aliyuncs.com/jiangbyte/galaxy_oj:1.0.0 .
#docker push registry.cn-beijing.aliyuncs.com/jiangbyte/galaxy_oj:1.0.0
#
#cd /mnt/e/MyProjects/astro-code-oj/pc
#docker build -t registry.cn-beijing.aliyuncs.com/jiangbyte/galaxy_oj_pc:1.0.0 .
#docker push registry.cn-beijing.aliyuncs.com/jiangbyte/galaxy_oj_pc:1.0.0
#
#cd /mnt/e/MyProjects/astro-code-oj/admin
#docker build -t registry.cn-beijing.aliyuncs.com/jiangbyte/galaxy_oj_admin:1.0.0 .
#docker push registry.cn-beijing.aliyuncs.com/jiangbyte/galaxy_oj_admin:1.0.0
#
#cd /mnt/e/MyProjects/astro-code-oj/judge-service
#docker build -t registry.cn-beijing.aliyuncs.com/jiangbyte/galaxy_oj_judge_service:1.0.0 .
#docker push registry.cn-beijing.aliyuncs.com/jiangbyte/galaxy_oj_judge_service:1.0.0




#!/bin/bash

# 构建和推送Docker镜像的脚本
set -e  # 遇到错误立即退出

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${GREEN}[INFO]${NC} $(date '+%Y-%m-%d %H:%M:%S') - $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $(date '+%Y-%m-%d %H:%M:%S') - $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $(date '+%Y-%m-%d %H:%M:%S') - $1"
}

# 检查Docker是否运行
check_docker() {
    if ! docker info > /dev/null 2>&1; then
        log_error "Docker未运行，请启动Docker服务"
        exit 1
    fi
}

# 构建和推送单个镜像的函数
build_and_push() {
    local project_dir=$1
    local image_name=$2
    local tag=$3

    log_info "开始处理: $image_name:$tag"

    # 检查目录是否存在
    if [ ! -d "$project_dir" ]; then
        log_error "目录不存在: $project_dir"
        return 1
    fi

    # 切换到项目目录
    cd "$project_dir" || {
        log_error "无法进入目录: $project_dir"
        return 1
    }

    # 检查Dockerfile是否存在
    if [ ! -f "Dockerfile" ]; then
        log_warn "在 $project_dir 中未找到Dockerfile，跳过构建"
        return 0
    fi

    # 构建镜像
    log_info "正在构建镜像: $image_name:$tag"
    if docker build -t "$image_name:$tag" .; then
        log_info "镜像构建成功: $image_name:$tag"
    else
        log_error "镜像构建失败: $image_name:$tag"
        return 1
    fi

    # 推送镜像
    log_info "正在推送镜像: $image_name:$tag"
    if docker push "$image_name:$tag"; then
        log_info "镜像推送成功: $image_name:$tag"
    else
        log_error "镜像推送失败: $image_name:$tag"
        return 1
    fi

    return 0
}

# 主函数
main() {
    log_info "开始构建和推送所有Docker镜像"

    # 检查Docker状态
    check_docker

    # 定义项目列表
    declare -A projects=(
        ["/mnt/e/MyProjects/astro-code-oj/galaxy-web-app/galaxy-gateway"]="registry.cn-beijing.aliyuncs.com/jiangbyte/galaxy_oj_gateway"
        ["/mnt/e/MyProjects/astro-code-oj/galaxy-web-app/galaxy-oj"]="registry.cn-beijing.aliyuncs.com/jiangbyte/galaxy_oj"
        ["/mnt/e/MyProjects/astro-code-oj/pc"]="registry.cn-beijing.aliyuncs.com/jiangbyte/galaxy_oj_pc"
        ["/mnt/e/MyProjects/astro-code-oj/admin"]="registry.cn-beijing.aliyuncs.com/jiangbyte/galaxy_oj_admin"
        ["/mnt/e/MyProjects/astro-code-oj/judge-service"]="registry.cn-beijing.aliyuncs.com/jiangbyte/galaxy_oj_judge_service"
    )

    local tag="1.0.0"
    local failed_projects=()

    # 遍历所有项目进行构建和推送
    for project_dir in "${!projects[@]}"; do
        image_name="${projects[$project_dir]}"

        if build_and_push "$project_dir" "$image_name" "$tag"; then
            log_info "✅ $image_name 处理完成"
        else
            log_error "❌ $image_name 处理失败"
            failed_projects+=("$image_name")
        fi

        echo "----------------------------------------"
    done

    # 输出总结
    if [ ${#failed_projects[@]} -eq 0 ]; then
        log_info "🎉 所有镜像构建和推送成功完成！"
    else
        log_error "以下项目处理失败:"
        for project in "${failed_projects[@]}"; do
            log_error "  - $project"
        done
        exit 1
    fi
}

# 执行主函数
main "$@"