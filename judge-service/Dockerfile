# 构建阶段
FROM golang:1.24-alpine AS builder

# 设置 Go 代理
ENV GOPROXY=https://goproxy.cn,direct
ENV GOSUMDB=off

# 安装构建依赖
RUN apk add --no-cache gcc musl-dev libseccomp-dev git

WORKDIR /app

# 复制依赖文件
COPY go.mod go.sum ./

# 下载依赖
RUN go mod download

# 复制源代码
COPY . .

# 构建应用（启用 CGO）
RUN CGO_ENABLED=1 GOOS=linux go build -o judge-service main.go

# 运行阶段
FROM alpine:latest

# 安装运行时依赖（只需要 libseccomp 运行时库）
RUN apk add --no-cache libseccomp ca-certificates

WORKDIR /root/

# 从构建阶段复制二进制文件
COPY --from=builder /app/judge-service .
COPY --from=builder /app/etc/judge.yaml ./etc/

# 创建必要的目录
RUN mkdir -p /tmp/judge

# 创建非root用户
RUN addgroup -S appgroup && adduser -S appuser -G appgroup \
    && chown -R appuser:appgroup /root/ /tmp/judge

USER appuser

# 启动应用
CMD ["./judge-service"]