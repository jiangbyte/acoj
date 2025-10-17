FROM ubuntu:22.04
LABEL authors="Charlie"

# 设置环境变量
ENV GOPATH=/go
ENV PATH=/usr/local/go/bin:$GOPATH/bin:$PATH

# 设置 Go 代理
ENV GOPROXY=https://goproxy.cn,direct
ENV GOSUMDB=off

# 设置工作目录
WORKDIR /tmp

# 安装必要的工具和清理缓存
RUN apt-get update && apt-get install -y \
    wget \
    && rm -rf /var/lib/apt/lists/*

# 下载并安装 Go
RUN wget https://mirrors.aliyun.com/golang/go1.24.4.linux-amd64.tar.gz && \
    tar -C /usr/local -xzf go1.24.4.linux-amd64.tar.gz && \
    rm go1.24.4.linux-amd64.tar.gz

# 验证安装
RUN go version

# 设置工作目录
WORKDIR /

# 配置 Go 模块代理（国内加速）
ENV GOPROXY=https://goproxy.cn,direct

# 设置默认的 Go 模块模式
ENV GO111MODULE=auto

# 安装完整的构建工具链
RUN apt-get update && apt-get install -y \
    gcc \
    pkg-config \
    libseccomp-dev \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# 复制依赖文件
COPY go.mod go.sum ./

# 下载依赖
RUN go mod download

# 复制源代码
COPY . .

# 构建应用（启用 CGO）
RUN CGO_ENABLED=1 GOOS=linux go build -o judge-service main.go

# 创建必要的目录
RUN mkdir -p /tmp/judge


# c/cpp 编译与执行环境
RUN apt-get update && apt-get install -y \
    g++ \
    build-essential \
    && rm -rf /var/lib/apt/lists/*

# 启动应用
#CMD ["./judge-service"]

# 复制启动脚本
COPY entrypoint.sh .

# 启动应用（通过脚本）
CMD ["./entrypoint.sh"]