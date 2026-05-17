# 安装配置

本页详细说明 Hei Gin 框架的环境要求和完整安装步骤。

## 环境要求

### Go 语言环境

Hei Gin 需要 **Go 1.25 或更高版本**。安装步骤如下：

1. **下载 Go**：访问 [Go 官方下载页面](https://go.dev/dl/) 下载对应操作系统的安装包
2. **安装 Go**：按照官方指引完成安装
3. **验证安装**：

```bash
go version
# 输出示例：go version go1.25.0 windows/amd64
```

4. **配置 Go 模块代理**（可选，国内推荐）：

```bash
go env -w GOPROXY=https://goproxy.cn,direct
```

### MySQL 数据库

Hei Gin 使用 **MySQL 8.0 或更高版本**，需要支持 JSON 数据类型和窗口函数。

1. **安装 MySQL**：可从 [MySQL 官方下载](https://dev.mysql.com/downloads/) 获取
2. **创建数据库**：

```sql
CREATE DATABASE IF NOT EXISTS `hei-gin` 
  DEFAULT CHARACTER SET utf8mb4 
  COLLATE utf8mb4_unicode_ci;
```

3. **创建用户并授权**（可选）：

```sql
CREATE USER 'hei'@'%' IDENTIFIED BY 'your-password';
GRANT ALL PRIVILEGES ON `hei-gin`.* TO 'hei'@'%';
FLUSH PRIVILEGES;
```

### Redis 缓存

Hei Gin 需要 **Redis 6.0 或更高版本**，用于：

- JWT 会话存储
- 图形验证码存储
- 权限缓存
- 防重复提交令牌

1. **安装 Redis**：可从 [Redis 官方下载](https://redis.io/download/) 获取
2. **Windows 用户**：Redis 官方不支持 Windows，建议使用 WSL2 或 Docker
3. **使用 Docker**：

```bash
docker run -d --name redis -p 6379:6379 redis:7-alpine
```

4. **验证连接**：

```bash
redis-cli ping
# 输出：PONG
```

## 完整安装步骤

### 步骤 1：获取项目代码

```bash
git clone <项目仓库地址>
cd hei-gin
```

### 步骤 2：安装 Go 依赖

```bash
go mod tidy
go mod download
```

`go mod tidy` 会自动清理无用依赖并添加缺失依赖，`go mod download` 将依赖下载到本地缓存。

### 步骤 3：配置 config.yaml

将项目根目录的 `config.yaml` 修改为你的本地环境配置。详细配置项说明请参考 [配置文件说明](config)。

### 步骤 4：初始化数据库

Hei Gin 使用 Ent ORM 的自动迁移功能。启动时应用会自动创建和更新数据库表结构，无需手动执行 SQL 迁移脚本。

但如果需要手动初始化，可以在 MySQL 中执行项目提供的初始化 SQL 脚本（如果存在）。

### 步骤 5：启动服务

```bash
go run main.go
```

推荐使用 `air` 或 `gowatch` 等热重载工具进行开发：

```bash
# 安装 air
go install github.com/air-verse/air@latest

# 启动热重载
air
```

### 步骤 6：验证部署

访问以下接口验证服务正常运行：

- 健康检查：`GET http://localhost:18885/`
- B 端获取验证码：`GET http://localhost:18885/api/v1/public/b/captcha`
- B 端获取 SM2 公钥：`GET http://localhost:18885/api/v1/public/b/sm2-public-key`

## 生产环境部署

### 编译二进制

```bash
go build -o hei-gin .
```

### 使用 Systemd 管理（Linux）

```ini
[Unit]
Description=Hei Gin Service
After=network.target

[Service]
Type=simple
User=www
WorkingDirectory=/var/www/hei-gin
ExecStart=/var/www/hei-gin/hei-gin
Restart=always
RestartSec=5

[Install]
WantedBy=multi-user.target
```

### 使用 Docker 部署

参考项目根目录下的 Dockerfile：

```dockerfile
FROM golang:1.25-alpine AS builder
WORKDIR /app
COPY . .
RUN go build -o hei-gin .

FROM alpine:3.19
WORKDIR /app
COPY --from=builder /app/hei-gin .
COPY config.yaml .
EXPOSE 18885
CMD ["./hei-gin"]
```

## 常见问题

**Q: 启动报错 "connect: connection refused"**

A：请确认 MySQL 和 Redis 服务已启动，且 config.yaml 中的连接信息正确。

**Q: 数据库迁移失败**

A：确认 MySQL 版本 >= 8.0，且数据库用户有 CREATE TABLE 权限。

**Q: go mod tidy 下载失败**

A：国内用户建议配置 Go 模块代理：`go env -w GOPROXY=https://goproxy.cn,direct`。
