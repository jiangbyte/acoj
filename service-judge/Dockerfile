
# syntax=docker/dockerfile:1.4
FROM debian:13
LABEL authors="Charlie"

# apt install — use Aliyun mirror; ensure lists exist before update
RUN --mount=type=cache,target=/var/cache/apt,sharing=locked \
    sed -i 's|deb.debian.org|mirrors.aliyun.com|g' /etc/apt/sources.list.d/debian.sources && \
    rm -rf /var/lib/apt/lists/* && mkdir -p /var/lib/apt/lists/partial && \
    apt-get -o Acquire::Retries=5 update && \
    apt-get -o Acquire::Retries=5 install -y --no-install-recommends \
    wget ca-certificates gcc g++ build-essential pkg-config libseccomp-dev \
    bubblewrap \
    python3 python3-pip && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

# 设置环境变量
ENV GOPATH=/go \
    PATH=/usr/local/go/bin:$GOPATH/bin:$PATH \
    GOPROXY=https://goproxy.io,https://goproxy.cn,https://proxy.golang.org,direct \
    GOSUMDB=off \
    GO111MODULE=auto

WORKDIR /tmp

# 下载并安装 Go
RUN wget -q https://mirrors.aliyun.com/golang/go1.24.4.linux-amd64.tar.gz && \
    tar -C /usr/local -xzf go1.24.4.linux-amd64.tar.gz && \
    rm go1.24.4.linux-amd64.tar.gz

RUN go version

WORKDIR /app

COPY go.mod go.sum ./

# 使用 Go 模块缓存
RUN --mount=type=cache,target=/go/pkg/mod \
    --mount=type=cache,target=/root/.cache/go-build \
    go mod download

COPY . .

RUN CGO_ENABLED=1 GOOS=linux go build -o service-judge main.go

RUN echo "Go version:" && go version && \
    echo "G++ version:" && g++ --version && \
    echo "Python3 version:" && python3 --version

COPY entrypoint.sh .
RUN chmod +x entrypoint.sh

CMD ["./entrypoint.sh"]


# DOCKER_BUILDKIT=1 docker build -t registry.cn-beijing.aliyuncs.com/jiangbyte/galaxy_oj_judge_service:1.0.0 .
