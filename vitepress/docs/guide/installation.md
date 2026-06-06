# 安装配置

本页详细说明 Hei Gin 框架的环境要求和完整安装步骤。

## 环境要求

### Go 语言环境

需要 **Go 1.25 或更高版本**：

```bash
go version
# 输出示例：go version go1.25.0 linux/amd64
```

国内推荐配置 Go 模块代理：

```bash
go env -w GOPROXY=https://goproxy.cn,direct
```

### MySQL 数据库

需要 **MySQL 8.0 或更高版本**，支持 JSON 数据类型和窗口函数。

```sql
CREATE DATABASE IF NOT EXISTS `hei-gin`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

### Redis 缓存

需要 **Redis 6.0 或更高版本**，用于 Token 会话存储、验证码、权限缓存和防重复提交。

```bash
# 使用 Docker
docker run -d --name redis -p 6379:6379 redis:7-alpine
```

## Go Workspace 结构

Hei Gin 使用 Go Workspace 管理多模块：

```
hei-gin/
├── go.work              # Workspace 定义
├── go.mod               # 根模块
├── sdk/go.mod           # SDK 模块
├── api/go.mod           # 接口定义模块
├── plugins/plugin-sys/go.mod    # 系统插件
├── plugins/plugin-client/go.mod # 客户端插件
├── plugins/plugin-im/go.mod     # IM 插件
└── app/go.mod           # 应用模块
```

## 完整安装步骤

### 步骤 1：获取项目

```bash
git clone https://github.com/jiangbyte/hei-gin.git
cd hei-gin
```

### 步骤 2：安装依赖

```bash
go mod tidy
go mod download
```

Go Workspace 会自动处理所有子模块的依赖。

### 步骤 3：配置 config.yaml

复制 `config.example.yaml` 为 `config.yaml`，修改数据库和 Redis 连接信息。

### 步骤 4：DB 迁移（可选）

```bash
go run cmd/migrate/main.go
```

迁移需要通过 `cmd/migrate` 工具手动执行（`app.Run()` 不会自动执行迁移）：

### 步骤 5：启动服务

```bash
go run main.go
```

推荐使用 `air` 热重载：

```bash
go install github.com/air-verse/air@latest
air
```

### 步骤 6：验证

```bash
curl http://localhost:18885/
# 响应：{"message":"hei-gin is running","version":"1.0.0"}
```

## 生产环境部署

### 编译二进制

```bash
go build -o hei-gin main.go
```

### 使用 Systemd（Linux）

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

### 多实例部署

需确保每个实例的 `snowflake.instance` 配置不同值以避免雪花 ID 冲突。IM 模块依赖 Redis 实现跨实例通信。

## 常见问题

**Q: 启动报错 "connect: connection refused"**
A：请确认 MySQL 和 Redis 服务已启动，且 config.yaml 中的连接信息正确。

**Q: go mod tidy 下载失败**
A：配置 Go 模块代理：`go env -w GOPROXY=https://goproxy.cn,direct`。

**Q: 如何添加新的业务模块？**
A：参考 [模块开发](../modules/development) 创建新的 plugin 目录，添加到 go.work，在 app/main.go 中导入。
