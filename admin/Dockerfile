FROM node:22-alpine AS base
ENV PNPM_HOME="/pnpm"
ENV PATH="$PNPM_HOME:$PATH"
ENV PNPM_REGISTRY=https://registry.npmmirror.com/
ENV NPM_CONFIG_REGISTRY=https://registry.npmmirror.com/
RUN corepack enable
COPY . /app
WORKDIR /app

FROM base AS builder
RUN --mount=type=cache,id=pnpm,target=/pnpm/store pnpm install --no-frozen-lockfile
RUN pnpm run build

FROM nginx:1.23.1-alpine
WORKDIR /www
COPY --from=builder /app/dist/ .
COPY ./nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 81
