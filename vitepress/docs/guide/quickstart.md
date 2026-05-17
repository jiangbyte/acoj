# 快速开始

本指南将带你 5 分钟内启动 Hei Gin 项目。

## 环境要求

在开始之前，请确保你的开发环境满足以下要求：

| 依赖 | 版本要求 |
|------|---------|
| Go | 1.25 或更高版本 |
| MySQL | 8.0 或更高版本 |
| Redis | 6.0 或更高版本 |

## 第一步：克隆项目

```bash
git clone <项目仓库地址>
cd hei-gin
```

## 第二步：修改配置

项目根目录下的 `config.yaml` 是核心配置文件。根据你的本地环境修改数据库和 Redis 连接信息：

```yaml
app:
  name: "hei-gin"
  port: 18885
  mode: "dev"

db:
  host: "127.0.0.1"
  port: 3306
  username: "root"
  password: "your-password"
  database: "hei-gin"

redis:
  host: "127.0.0.1"
  port: 6379
  password: ""
  db: 0
```

默认配置中包含了开发环境使用的账号密码，请务必在部署前修改。

## 第三步：创建数据库

在 MySQL 中创建数据库：

```sql
CREATE DATABASE IF NOT EXISTS `hei-gin` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

数据库表结构由 Ent ORM 自动迁移生成，首次启动时会自动创建所有表。

## 第四步：运行项目

在项目根目录执行：

```bash
go run main.go
```

如果依赖缺失，先执行：

```bash
go mod tidy
go mod download
```

启动成功的输出类似：

```
[INFO] 2025/01/15 10:00:00 Starting server on :18885
[INFO] 2025/01/15 10:00:00 Database migration completed
[INFO] 2025/01/15 10:00:00 Redis connected successfully
[INFO] 2025/01/15 10:00:00 Permission scan completed
```

## 第五步：验证启动

服务启动后，访问健康检查接口验证服务是否正常运行：

```bash
curl http://localhost:18885/
```

预期响应：

```json
{
  "code": 200,
  "message": "请求成功",
  "data": "pong",
  "success": true,
  "trace_id": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
}
```

## 默认账号

| 端侧 | 账号 | 密码 |
|------|------|------|
| B 端管理 | admin | admin123（需 SM2 加密传输）|
| C 端用户 | 需自行注册 | 需自行注册 |

> 注意：登录密码需要通过 SM2 公钥加密后传输，具体流程请参考 [认证模块](../features/auth)。

## 下一步

- 了解 [配置文件说明](config) 中的详细配置项
- 阅读 [架构概述](../architecture/overview) 理解框架设计
- 参考 [模块开发](../modules/development) 开始业务功能开发
