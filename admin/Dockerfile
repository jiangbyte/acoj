# 使用 Node.js 22 alpine 版本作为基础镜像，命名为 base 阶段
FROM node:22-alpine AS base
# 设置 pnpm 的安装路径环境变量
ENV PNPM_HOME="/pnpm"
# 将 pnpm 路径添加到系统 PATH 环境变量中
ENV PATH="$PNPM_HOME:$PATH"
# 启用 Node.js 的 corepack 来管理包管理器
RUN corepack enable
# 将当前目录所有文件复制到容器的 /app 目录
COPY . /app
# 设置工作目录为 /app
WORKDIR /app

# 创建名为 prod-deps 的新阶段，用于安装生产依赖
FROM base AS prod-deps
# 使用缓存挂载来加速 pnpm 安装，只安装生产依赖，使用冻结的 lockfile
# RUN --mount=type=cache,id=pnpm,target=/pnpm/store pnpm install --prod --frozen-lockfile
RUN pnpm install --prod --frozen-lockfile

# 创建名为 builder 的新阶段，用于构建项目
FROM base AS builder
# 使用缓存挂载安装所有依赖（包括开发依赖），使用冻结的 lockfile
#RUN --mount=type=cache,id=pnpm,target=/pnpm/store pnpm install --frozen-lockfile
RUN pnpm install --frozen-lockfile
# 运行构建命令
RUN pnpm run build

# 使用 nginx alpine 版本作为最终运行环境
FROM nginx:1.23.1-alpine

# 设置工作目录为 /www
WORKDIR /www

# 从 builder 阶段复制构建产物到当前镜像
COPY --from=builder /app/dist/ .

# 复制自定义 nginx 配置文件到容器中
COPY ./nginx.conf /etc/nginx/conf.d/default.conf

# 声明容器运行时监听的端口
EXPOSE 81