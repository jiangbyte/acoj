FROM registry.cn-beijing.aliyuncs.com/jiangbyte/ubuntu_golang:1.0.0

# 设置 Go 代理
ENV GOPROXY=https://goproxy.cn,direct
ENV GOSUMDB=off

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

# 启动应用
CMD ["./judge-service"]