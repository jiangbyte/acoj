# 快速开始

本指南将带你 5 分钟内启动 Hei Gin 项目。

## 环境要求

| 依赖 | 版本要求 |
|------|---------|
| Go | 1.25 或更高版本 |
| MySQL | 8.0 或更高版本 |
| Redis | 6.0 或更高版本 |

## 第一步：克隆项目

```bash
git clone https://github.com/jiangbyte/hei-gin.git
cd hei-gin
```

## 第二步：修改配置

复制 `config.example.yaml` 为 `config.yaml`，修改数据库和 Redis 连接：

```yaml
db:
  host: 127.0.0.1
  port: 3306
  user: root
  password: "your-password"
  database: hei-gin

redis:
  host: 127.0.0.1
  port: 6379
  password: ""
  database: 1
```

## 第三步：创建数据库

```sql
CREATE DATABASE IF NOT EXISTS `hei-gin` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

## 第四步：安装依赖并迁移

```bash
go mod tidy
go run cmd/migrate/main.go
```

## 第五步：启动服务

```bash
go run main.go
```

启动成功的输出类似：

```
[Config] Loading from config.yaml
[Database] MySQL connection verified, max_conns=30, max_lifetime=1h0m0s
[Database] Redis connection verified
[module] registered: auth
[module] init: auth
[module] start: auth
[APP] Server started on :18885
```

## 第六步：验证启动

```bash
curl http://localhost:18885/
```

预期响应：

```json
{
  "message": "hei-gin is running",
  "version": "1.0.0"
}
```

## 默认账号

| 端侧 | 账号 | 密码 |
|------|------|------|
| B 端管理 | admin | admin123（需 SM2 加密传输）|
| C 端用户 | 需自行注册 | 需自行注册 |

> 登录密码需要通过 SM2 公钥加密后传输。

## 下一步

- 了解 [配置文件说明](config)
- 阅读 [架构概述](../architecture/overview)
- 参考 [模块开发](../modules/development)
