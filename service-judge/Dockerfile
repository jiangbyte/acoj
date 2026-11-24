# syntax=docker/dockerfile:1.4
FROM debian:13
LABEL authors="Charlie"

# 1. 安装判题沙箱与多语言运行时依赖
ARG USE_CN_MIRROR=1
RUN --mount=type=cache,target=/var/cache/apt,sharing=locked \
    if [ "$USE_CN_MIRROR" = "1" ]; then \
      sed -i 's|deb.debian.org|mirrors.aliyun.com|g' /etc/apt/sources.list.d/debian.sources; \
    fi && \
    rm -rf /var/lib/apt/lists/* && mkdir -p /var/lib/apt/lists/partial && \
    apt-get -o Acquire::Retries=5 update && \
    apt-get -o Acquire::Retries=5 install -y --no-install-recommends \
      wget ca-certificates gcc g++ build-essential pkg-config libseccomp-dev \
      bubblewrap \
      python3 python3-pip && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

# 2. 配置 Go 工具链环境
ENV GOPATH=/go \
    PATH=/usr/local/go/bin:$GOPATH/bin:$PATH \
    GOPROXY=https://goproxy.io,https://goproxy.cn,https://proxy.golang.org,direct \
    GOSUMDB=off \
    GO111MODULE=on

WORKDIR /tmp

# 3. 安装 Go 1.24.4
RUN wget -q https://mirrors.aliyun.com/golang/go1.24.4.linux-amd64.tar.gz && \
    tar -C /usr/local -xzf go1.24.4.linux-amd64.tar.gz && \
    rm go1.24.4.linux-amd64.tar.gz && \
    go version

WORKDIR /app

# 4. 下载模块依赖（缓存挂载加速重复构建）
COPY go.mod go.sum ./
RUN --mount=type=cache,target=/go/pkg/mod \
    --mount=type=cache,target=/root/.cache/go-build \
    go mod download

# 5. 编译判题服务二进制
COPY . .
RUN --mount=type=cache,target=/go/pkg/mod \
    --mount=type=cache,target=/root/.cache/go-build \
    CGO_ENABLED=1 GOOS=linux go build -o service-judge main.go

COPY entrypoint.sh .
RUN chmod +x entrypoint.sh

CMD ["./entrypoint.sh"]
