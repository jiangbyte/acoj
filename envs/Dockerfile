FROM ubuntu:latest
LABEL authors="Charlie"

# 设置环境变量
ENV GOPATH=/go
ENV PATH=/usr/local/go/bin:$GOPATH/bin:$PATH

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

# 设置容器默认命令
CMD ["/bin/bash"]