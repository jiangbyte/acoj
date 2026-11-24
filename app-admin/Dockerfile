# syntax=docker/dockerfile:1.4
FROM node:22-alpine AS base
ENV PNPM_HOME="/pnpm"
ENV PATH="$PNPM_HOME:$PATH"
# 构建参数可覆盖镜像源；默认淘宝源，GHCR CI 可传官方源
ARG NPM_REGISTRY=https://registry.npmmirror.com
RUN corepack enable && corepack prepare pnpm@10.28.2 --activate \
    && printf 'registry=%s\nstore-dir=/pnpm/store\nauto-install-peers=true\n' "$NPM_REGISTRY" > /root/.npmrc
WORKDIR /app
COPY package.json pnpm-lock.yaml pnpm-workspace.yaml ./

FROM base AS deps
RUN --mount=type=cache,id=pnpm-admin,target=/pnpm/store \
    pnpm install --frozen-lockfile

FROM deps AS builder
ARG VITE_GATEWAY=http://localhost:89
ENV VITE_GATEWAY=$VITE_GATEWAY
COPY . .
RUN pnpm run build

FROM nginx:1.23.1-alpine
WORKDIR /www
COPY --from=builder /app/dist/ .
COPY ./nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 81
