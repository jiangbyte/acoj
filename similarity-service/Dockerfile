# FROM ubuntu:22.04
# LABEL authors="Charlie"

# # 设置环境变量
# ENV GOPATH=/go
# ENV PATH=/usr/local/go/bin:$GOPATH/bin:$PATH

# # 设置 Go 代理
# ENV GOPROXY=https://goproxy.cn,direct
# ENV GOSUMDB=off

# # 设置工作目录
# WORKDIR /tmp

# # 安装必要的工具和清理缓存
# RUN apt-get update && apt-get install -y \
#     wget \
#     && rm -rf /var/lib/apt/lists/*

# # 下载并安装 Go
# RUN wget https://mirrors.aliyun.com/golang/go1.24.4.linux-amd64.tar.gz && \
#     tar -C /usr/local -xzf go1.24.4.linux-amd64.tar.gz && \
#     rm go1.24.4.linux-amd64.tar.gz

# # 验证安装
# RUN go version

# # 设置工作目录
# WORKDIR /

# # 配置 Go 模块代理（国内加速）
# ENV GOPROXY=https://goproxy.cn,direct

# # 设置默认的 Go 模块模式
# ENV GO111MODULE=auto

# # 安装完整的构建工具链
# RUN apt-get update && apt-get install -y \
#     gcc \
#     pkg-config \
#     libseccomp-dev \
#     && rm -rf /var/lib/apt/lists/*

# WORKDIR /app

# # 复制依赖文件
# COPY go.mod go.sum ./

# # 下载依赖
# RUN go mod download

# # 复制源代码
# COPY . .

# # 构建应用（启用 CGO）
# RUN CGO_ENABLED=1 GOOS=linux go build -o judge-service main.go

# # 创建必要的目录
# RUN mkdir -p /tmp/judge

# # ============================================== OJ c/cpp 编译与执行环境 ==============================================
# RUN apt-get update && apt-get install -y \
#     g++ \
#     build-essential \
#     && rm -rf /var/lib/apt/lists/*

# # ============================================== OJ java 编译与执行环境 ==============================================
# RUN apt-get update && apt-get install -y \
#     openjdk-17-jdk \
#     && rm -rf /var/lib/apt/lists/*

# # ============================================== OJ python 编译与执行环境 ==============================================
# RUN apt-get update && apt-get install -y \
#     python3 \
#     python3-pip \
#     && rm -rf /var/lib/apt/lists/*

# # 启动应用
# #CMD ["./judge-service"]

# # 打印各语言的版本信息（可选）
# RUN echo "Go version:" && go version && \
#     echo "G++ version:" && g++ --version && \
#     echo "Java version:" && java -version && \
#     echo "Python3 version:" && python3 --version

# # 复制启动脚本
# COPY entrypoint.sh .

# # 启动应用（通过脚本）
# CMD ["./entrypoint.sh"]








# FROM ubuntu:22.04
# LABEL authors="Charlie"

# # 设置环境变量
# ENV GOPATH=/go \
#     PATH=/usr/local/go/bin:$GOPATH/bin:$PATH \
#     GOPROXY=https://goproxy.cn,direct \
#     GOSUMDB=off \
#     GO111MODULE=auto

# # 设置工作目录
# WORKDIR /tmp

# # 先更新 apt 源并清理，解决网络问题
# RUN apt-get update || true && \
#     apt-get clean && \
#     rm -rf /var/lib/apt/lists/*

# # 分步骤安装包，避免单次安装过多包导致的问题
# RUN apt-get update && apt-get install -y --fix-missing \
#     wget \
#     ca-certificates

# # 安装基础编译工具
# RUN apt-get update && apt-get install -y \
#     gcc \
#     g++ \
#     build-essential \
#     pkg-config \
#     libseccomp-dev

# # 安装语言环境
# RUN apt-get update && apt-get install -y \
#     openjdk-17-jdk \
#     python3 \
#     python3-pip

# # 清理缓存
# RUN apt-get clean && rm -rf /var/lib/apt/lists/*

# # 下载并安装 Go
# RUN wget https://mirrors.aliyun.com/golang/go1.24.4.linux-amd64.tar.gz && \
#     tar -C /usr/local -xzf go1.24.4.linux-amd64.tar.gz && \
#     rm go1.24.4.linux-amd64.tar.gz

# # 验证安装
# RUN go version

# # 设置工作目录
# WORKDIR /app

# # 复制依赖文件
# COPY go.mod go.sum ./

# # 下载依赖
# RUN go mod download

# # 复制源代码
# COPY . .

# # 构建应用（启用 CGO）
# RUN CGO_ENABLED=1 GOOS=linux go build -o judge-service main.go

# # 打印各语言的版本信息
# RUN echo "Go version:" && go version && \
#     echo "G++ version:" && g++ --version && \
#     echo "Java version:" && java -version && \
#     echo "Python3 version:" && python3 --version

# # 复制启动脚本
# COPY entrypoint.sh .

# # 设置启动脚本执行权限
# RUN chmod +x entrypoint.sh

# # 启动应用（通过脚本）
# CMD ["./entrypoint.sh"]


#
## syntax=docker/dockerfile:1.4
#FROM ubuntu:22.04
#LABEL authors="Charlie"
#
## 启用 BuildKit 缓存挂载（需要 DOCKER_BUILDKIT=1）
#RUN --mount=type=cache,target=/var/cache/apt \
#    --mount=type=cache,target=/var/lib/apt \
#    sed -i 's/archive.ubuntu.com/mirrors.aliyun.com/g' /etc/apt/sources.list && \
#    sed -i 's/security.ubuntu.com/mirrors.aliyun.com/g' /etc/apt/sources.list && \
#    apt-get update && apt-get install -y --no-install-recommends \
#    wget ca-certificates gcc g++ build-essential pkg-config libseccomp-dev \
#    openjdk-17-jdk python3 python3-pip && \
#    apt-get clean
#
## 设置环境变量
#ENV GOPATH=/go \
#    PATH=/usr/local/go/bin:$GOPATH/bin:$PATH \
#    GOPROXY=https://goproxy.cn,direct \
#    GOSUMDB=off \
#    GO111MODULE=auto
#
#WORKDIR /tmp
#
## 下载并安装 Go
#RUN wget -q https://mirrors.aliyun.com/golang/go1.24.4.linux-amd64.tar.gz && \
#    tar -C /usr/local -xzf go1.24.4.linux-amd64.tar.gz && \
#    rm go1.24.4.linux-amd64.tar.gz
#
#RUN go version
#
#WORKDIR /app
#
#COPY go.mod go.sum ./
#
## 使用 Go 模块缓存
#RUN --mount=type=cache,target=/go/pkg/mod \
#    --mount=type=cache,target=/root/.cache/go-build \
#    go mod download
#
#COPY . .
#
#RUN CGO_ENABLED=1 GOOS=linux go build -o similarity-service main.go
#
#RUN echo "Go version:" && go version && \
#    echo "G++ version:" && g++ --version && \
#    echo "Java version:" && java -version && \
#    echo "Python3 version:" && python3 --version
#
#COPY entrypoint.sh .
#RUN chmod +x entrypoint.sh
#
#CMD ["./entrypoint.sh"]


# DOCKER_BUILDKIT=1 docker build -t registry.cn-beijing.aliyuncs.com/jiangbyte/galaxy_oj_judge_service:1.0.0 .






# syntax=docker/dockerfile:1.4
FROM ubuntu:22.04
LABEL authors="Charlie"

# 启用 BuildKit 缓存挂载（需要 DOCKER_BUILDKIT=1）
RUN --mount=type=cache,target=/var/cache/apt \
    --mount=type=cache,target=/var/lib/apt \
    sed -i 's/archive.ubuntu.com/mirrors.aliyun.com/g' /etc/apt/sources.list && \
    sed -i 's/security.ubuntu.com/mirrors.aliyun.com/g' /etc/apt/sources.list && \
    apt-get update && apt-get install -y --no-install-recommends \
    wget ca-certificates pkg-config build-essential gcc && \
    apt-get clean

# 设置环境变量
ENV GOPATH=/go \
    PATH=/usr/local/go/bin:$GOPATH/bin:$PATH \
    GOPROXY=https://goproxy.cn,direct \
    GOSUMDB=off \
    GO111MODULE=auto

WORKDIR /tmp

# 下载并安装 Go
RUN wget https://mirrors.aliyun.com/golang/go1.24.4.linux-amd64.tar.gz && \
    tar -C /usr/local -xzf go1.24.4.linux-amd64.tar.gz && \
    rm go1.24.4.linux-amd64.tar.gz

# 主机文件系统复制Go安装包
# 从构建上下文复制并安装 Go
#COPY go1.24.4.linux-amd64.tar.gz /tmp/
#RUN tar -C /usr/local -xzf go1.24.4.linux-amd64.tar.gz && \
#    rm go1.24.4.linux-amd64.tar.gz

RUN go version

WORKDIR /app

COPY go.mod go.sum ./

# 使用 Go 模块缓存
RUN --mount=type=cache,target=/go/pkg/mod \
    --mount=type=cache,target=/root/.cache/go-build \
    go mod download

COPY . .

RUN CGO_ENABLED=1 GOOS=linux go build -o similarity-service main.go

RUN echo "Go version:" && go version

COPY entrypoint.sh .
RUN chmod +x entrypoint.sh

CMD ["./entrypoint.sh"]