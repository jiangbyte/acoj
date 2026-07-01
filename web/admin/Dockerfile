#FROM node:22-alpine AS build
#FROM docker.xuanyuan.run/library/node:22-alpine AS build
FROM swr.cn-north-4.myhuaweicloud.com/ddn-k8s/docker.io/node:22-alpine AS build

# Docker build 环境通常没有 IPv6，强制 Node.js 优先解析 IPv4（AAAA → A）
ENV NODE_OPTIONS="--dns-result-order=ipv4first"

WORKDIR /app

RUN corepack enable

COPY package.json pnpm-lock.yaml pnpm-workspace.yaml ./
RUN pnpm install --frozen-lockfile

COPY . .
RUN pnpm build

#FROM nginx:1.27-alpine
#FROM docker.xuanyuan.run/library/nginx:1.27-alpine
FROM swr.cn-north-4.myhuaweicloud.com/ddn-k8s/docker.io/nginx:1.27-alpine

COPY --from=build /app/dist /usr/share/nginx/html

# nginx 镜像启动时自动用 envsubst 渲染 templates/*.template（替换 ${BACKEND_URL}）
# 到 /etc/nginx/conf.d/，实现容器启动时指定后端地址，无需重新构建。
COPY nginx/default.conf.template /etc/nginx/templates/default.conf.template

# 后端地址默认值，可在启动时用 -e BACKEND_URL=... 覆盖。
ENV BACKEND_URL="http://127.0.0.1:8000"

EXPOSE 81
