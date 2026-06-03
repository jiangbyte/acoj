# Hei Gin

> 本项目是我前段时间在公司内部为负责重构媒体项目独立开发的框架之一，目前已在实际生产中投入使用。该重构框架完全由我主导设计实现，现将其从内部分支中剥离，参考优秀框架，并添加了更多功能，最终以独立仓库的形式开源，不断在优化中，欢迎大家提意见、提issue、提PR。
> 工作繁忙，时间维护不定。

<img width="120" src="vitepress/docs/public/logo.svg">

![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)
![Go](https://img.shields.io/badge/Go-1.25+-brightgreen.svg)
![Gin](https://img.shields.io/badge/Gin-1.12+-blue.svg)
![GORM](https://img.shields.io/badge/GORM-1.25+-blue.svg)

## 简介

**Hei Gin** 是 HEI 快速开发框架的 Go 单体应用版本，基于 Gin + GORM 构建。提供开箱即用的快速开发解决方案，包含完善的权限管理（RBAC）、数据权限、认证授权、文件存储（MinIO/S3/Local）、操作日志等功能模块。框架采用前后端分离架构，支持快速搭建后台管理系统和 API 服务。

**在线文档**: [https://jiangbyte.github.io/hei-gin/](https://jiangbyte.github.io/hei-gin/)

## 技术栈

| 类型 | 技术 |
| --- | --- |
| 核心框架 | Go 1.25+ / Gin 1.12+ |
| ORM | GORM (gorm.io/gorm) |
| 数据库 | MySQL 8.0+ |
| 缓存 | Redis 6.0+ (go-redis) |
| 认证授权 | Token / SM2 国密加解密 / SM3 哈希 / bcrypt 密码哈希 |
| 文件存储 | MinIO / S3 (AWS SDK) / Local 本地存储 |
| 定时调度 | cron 表达式 (robfig/cron/v3) |
| IP 定位 | IP2Region 离线库 |
| 分布式ID | Snowflake ID |

## 核心特性

- **模块化架构** — 模块通过 `init()` 自注册路由、权限、中间件、定时任务、DB 模型、种子数据，**零侵入 core/**
- **双端认证体系** — B 端（后台管理）和 C 端（客户端）独立的两套 Token 认证，通过 `auth.Business` / `auth.Consumer` 统一访问
- **SM2 国密加密** — 登录密码传输使用国密 SM2 C1C3C2 模式加密
- **SM3 哈希** — 操作日志防篡改签名
- **bcrypt 密码哈希** — 存储密码使用 bcrypt 加盐哈希
- **RBAC 权限控制** — 用户→角色→权限 + 用户直授权限，双层模型
- **数据权限（行级）** — 支持 ALL / ORG / ORG_AND_BELOW / SELF / CUSTOM_ORG / GROUP / GROUP_AND_BELOW / CUSTOM_GROUP 等数据范围控制
- **权限自动发现** — 启动时自动扫描注册的权限并缓存到 Redis
- **权限匹配器** — 支持 `*` 单级和 `**` 多级通配符匹配
- **操作日志** — `log.SysLog` 中间件自动记录用户操作，SM3 签名防篡改
- **防重复提交** — `middleware.NoRepeat` 中间件（基于 Redis）
- **链路追踪** — 基于 `trace_id` 的全链路追踪
- **统一验证码** — B 端/C 端独立的图形验证码服务
- **统一响应格式** — `{code, message, data, success, trace_id}` 标准结构
- **抽象文件存储** — 统一的 `FileStorage` 接口，支持分片上传、文件校验，三种后端（Local/MinIO/S3）
- **定时调度** — 支持 cron 表达式和固定间隔的后台任务，优雅关闭
- **DB 自动迁移** — 模块自注册 Model，`cmd/migrate` 自动发现，无需手动维护模型列表
- **种子数据** — 模块自注册种子数据，幂等执行
- **模块生命周期** — `module.Register()` 统一管理模块的 Init / Start / Stop
- **模块级配置** — 通过 `config.C.Raw` 读取模块专属配置，无需修改 config.go
- **在线会话管理** — B 端和 C 端独立的会话管理，支持在线用户查看和强制下线
- **雪花ID** — 分布式 Snowflake ID 生成器

## 项目结构

```
hei-gin
├── config/
│   └── config.go                   # 配置结构体（YAML 加载）
├── core/                           # 框架核心
│   ├── app/
│   │   ├── app.go                  # 应用工厂 + 核心服务初始化
│   │   ├── gen.go                  # go:generate codegen
│   │   ├── health.go               # 健康检查
│   │   ├── modules_gen.go          # 模块自注册入口（自动生成）
│   │   └── router.go               # 路由注册总入口
│   ├── auth/
│   │   ├── auth_tool.go            # Business + Consumer 认证单例
│   │   ├── base_auth.go            # 认证基础实现（Token/Login/Logout/Disable）
│   │   ├── permission_interface.go # 权限查询接口 + 默认实现（直授 + 角色双路径）
│   │   ├── permission_matcher.go   # 权限匹配器（* / ** 通配符）
│   │   ├── permission_scan.go      # 权限自动扫描与 Redis 缓存
│   │   ├── permission_tool.go      # 权限查询门面 + 接口注册器
│   │   ├── module.go               # 生命周期自注册
│   │   ├── middleware/
│   │   │   ├── check_login.go      # 登录检查中间件
│   │   │   ├── check_permission.go # 权限检查中间件
│   │   │   ├── check_role.go       # 角色检查中间件
│   │   │   └── norepeat.go         # 防重复提交中间件
│   │   └── pojo/
│   │       ├── login_user_info.go
│   │       └── login_client_user_info.go
│   ├── captcha/                    # 图形验证码服务
│   │   ├── captcha.go
│   │   └── module.go
│   ├── constants/
│   │   └── constants.go            # 系统常量 + Redis key 定义
│   ├── db/
│   │   ├── gorm.go                 # MySQL 连接 + InitDB/Close
│   │   ├── redis.go                # Redis 连接 + InitRedis/CloseRedis
│   │   └── migrate.go              # Model + Seed 注册器
│   ├── enums/                      # 枚举类型
│   ├── exception/
│   │   └── business_error.go       # BusinessError
│   ├── log/
│   │   ├── syslog.go               # SysLog 操作日志中间件
│   │   ├── record.go               # RecordAuthLog 认证日志
│   │   └── utils.go                # UA 解析 / 参数提取 / 签名
│   ├── middleware/
│   │   ├── auth_check.go           # 路径分流认证中间件
│   │   ├── cors.go                 # CORS 中间件
│   │   ├── recovery.go             # 全局异常恢复
│   │   └── trace.go                # 链路追踪
│   ├── module/
│   │   └── module.go               # Module 接口 + 生命周期注册器
│   ├── pojo/
│   │   ├── datetime_mixin.go       # 时间解析工具
│   │   └── id_params.go            # IdParam / IdsParam
│   ├── registry/
│   │   ├── route.go                # 路由注册器
│   │   ├── perm.go                 # 权限注册器（Perm / ClientPerm）
│   │   └── middleware.go           # 全局中间件注册器
│   ├── result/
│   │   └── result.go               # 统一响应格式
│   ├── scheduler/
│   │   ├── scheduler.go            # 定时调度（cron 表达式）
│   │   └── module.go               # 生命周期自注册
│   ├── storage/
│   │   ├── interface.go            # FileStorage 接口
│   │   ├── chunk.go                # ChunkedUploader 分片上传接口
│   │   ├── factory.go              # 存储后端工厂 + 配置加载
│   │   ├── config.go               # 存储配置结构体
│   │   ├── local.go                # LocalStorage + 分片上传实现
│   │   ├── minio.go                # MinioStorage + 分片上传实现
│   │   └── s3.go                   # S3Storage + 分片上传实现
│   └── utils/                      # 工具函数
│       ├── crypto.go               # SM2/SM3 加解密
│       ├── module.go               # 生命周期自注册
│       └── ...
├── cmd/
│   ├── migrate/
│   │   └── main.go                 # DB 迁移 + 种子数据
│   └── codegen/
│       └── main.go                 # modules_gen.go 自动生成
├── modules/                        # 业务模块
│   ├── sys/                        # B端（后台管理）
│   │   ├── banner/
│   │   ├── config/
│   │   ├── dict/
│   │   ├── file/                   # 文件管理（含分片上传 API）
│   │   ├── log/
│   │   ├── notice/
│   │   ├── org/
│   │   ├── permission/
│   │   ├── resource/
│   │   ├── role/
│   │   ├── session/
│   │   └── user/
│   └── client/                     # C端（客户端）
│       ├── auth/
│       ├── session/
│       └── user/
├── config.yaml                     # 配置文件
├── main.go                         # 应用入口
└── go.mod                          # Go 模块定义
```

### 模块结构约定

每个业务模块遵循垂直切片布局，支持零侵入自注册：

```
modules/<domain>/<module>/
├── model.go           # GORM 模型
├── params.go          # 请求/响应参数
├── service.go         # 业务逻辑
├── migrate.go         # DB 模型 + 种子数据自注册（可选）
├── module.go          # 模块生命周期（可选）
└── api/v1/
    ├── api.go         # 路由 + Handler
    └── register.go    # init() → registry.RegisterRoute()
```

## 快速开始

### 环境要求

- Go 1.25+
- MySQL 8.0+
- Redis 6.0+

### 配置

编辑 `config.yaml` 文件：

```yaml
app:
  name: hei-gin
  version: 1.0.0
  debug: true
  host: 127.0.0.1
  port: 18885
  upload_max_size: 52428800

db:
  host: localhost
  port: 3306
  user: root
  password: "123456"
  database: hei_data
  pool_size: 20
  pool_recycle: 3600

redis:
  host: localhost
  port: 6379
  password: "123456"
  database: 1
  max_connections: 200

token:
  expire_seconds: 2592000
  token_name: Authorization

sm2:
  private_key: your-sm2-private-key
  public_key: your-sm2-public-key

cors:
  allow_origins: ["*"]
  allow_methods: ["*"]
  allow_headers: ["*"]
  allow_credentials: false

snowflake:
  instance: 1

storage:
  default: LOCAL
  local:
    upload_folder: ./uploads
  # minio:
  #   endpoint: localhost:9000
  #   access_key: minioadmin
  #   secret_key: minioadmin
  #   bucket: hei-files
  #   secure: false
  #   region: us-east-1
```

### 初始化数据库

```bash
# 自动迁移（自动建表 + 种子数据）
go run cmd/migrate/main.go

# 跳过种子数据
go run cmd/migrate/main.go -skip-seed
```

### 运行

```bash
go run main.go
```

访问健康检查：<http://localhost:18885/>

## 模块化机制

框架提供零侵入的模块自注册机制，新增模块无需修改任何 `core/` 文件。

### 路由注册

每个模块在 `api/v1/register.go` 中自注册路由：

```go
func init() {
    registry.RegisterRoute(RegisterRoutes)
}

func RegisterRoutes(r *gin.Engine) {
    r.GET("/api/v1/sys/xxx/page", ...)
}
```

### 权限声明

路由注册时通过 `registry.Perm` 自动声明权限：

```go
r.GET("/api/v1/sys/xxx/page",
    registry.Perm("sys:xxx:page", "XXX分页"),
    handler,
)
```

### 模块生命周期

```go
// module.go
type xxxModule struct{ module.NoopModule }
func (m *xxxModule) Name() string { return "xxx" }
func (m *xxxModule) Init() error  { /* 初始化逻辑 */ return nil }
func (m *xxxModule) Stop() error  { /* 清理逻辑 */ return nil }

func init() { module.Register(&xxxModule{}) }
```

### DB 模型 + 种子数据

```go
// migrate.go
func init() {
    db.RegisterModel(&XxxModel{})
    db.RegisterSeed("xxx initial data", seedXxx)
}

func seedXxx() error {
    // 幂等：检查数据是否存在
    var count int64
    db.DB.Model(&XxxModel{}).Where(...).Count(&count)
    if count > 0 { return nil }
    // 插入初始数据
    return nil
}
```

### 定时任务

```go
func init() {
    scheduler.Register("@every 1h", &CleanupTask{})
}
```

### 全局中间件

```go
func init() {
    registry.RegisterMiddleware(func(r *gin.Engine) {
        r.Use(myMiddleware())
    })
}
```

### 模块配置

```yaml
# config.yaml — 自动捕获到 config.C.Raw 中
xxx:
  host: example.com
  port: 8080
```

```go
cfg := config.C.Raw["xxx"].(map[string]any)
host := cfg["host"].(string)
```

### 生成模块注册表

```bash
go generate ./...
# 自动生成 core/app/modules_gen.go
```

## 认证体系

框架通过路径前缀自动分流认证方式：

| 路径模式 | 认证方式 | 说明 |
|---------|---------|------|
| `/api/v1/b/*` 或 `/api/v1/sys/*` | Business 认证 + 权限检查 | B 端后台管理 |
| `/api/v1/c/*` | Consumer 认证 + 权限检查 | C 端客户端 |
| `/api/v1/public/*` | 无认证 | 公开接口 |

认证工具通过 `auth.Business` 和 `auth.Consumer` 两个单例统一访问：

```go
// B 端
auth.Login(c, userID, extra)
auth.GetLoginIDDefaultNull(c)

// C 端
auth.Consumer.Login(c, userID, extra)
auth.Consumer.GetLoginIDDefaultNull(c)
```

## 中间件参考

### 权限中间件

```go
import "hei-gin/core/auth/middleware"

// AND 模式（默认）：需要全部权限
middleware.HeiCheckPermission([]string{"sys:banner:page", "sys:banner:create"})

// OR 模式：满足任一权限即可
middleware.HeiCheckPermission([]string{"sys:user:view", "sys:user:edit"}, "OR")
```

### 操作日志

```go
import "hei-gin/core/log"

r.POST("/api/v1/sys/banner/create",
    log.SysLog("添加Banner"),
    handler,
)
```

### 防重复提交

```go
import "hei-gin/core/auth/middleware"

r.POST("/api/v1/sys/xxx/create",
    middleware.NoRepeat(3000), // 3 秒内相同参数禁止重复提交
    handler,
)
```

## API 规范

### 统一响应格式

```json
{
  "code": 200,
  "message": "请求成功",
  "data": {},
  "success": true,
  "trace_id": "uuid-string"
}
```

### 分页响应

```json
{
  "code": 200,
  "message": "请求成功",
  "data": {
    "records": [],
    "total": 100,
    "page": 1,
    "size": 20,
    "pages": 5
  },
  "success": true,
  "trace_id": "uuid-string"
}
```

## 权限数据链路

```
User ──→ RelUserRole ──→ Role ──→ RelRolePermission ──→ Permission
User ──→ RelUserPermission ──→ Permission (直授)
```

权限匹配支持 `*`（单级通配符）和 `**`（多级通配符）。

## 文件存储

框架通过 `FileStorage` 接口 + `ChunkedUploader` 接口抽象文件存储，三种后端均可通过配置切换。

### 支持的存储后端

| 后端 | 实现 | 分片上传 |
|------|------|---------|
| **Local** | 本地文件系统 | ✅ 临时目录 → 顺序合并 |
| **MinIO** | MinIO 对象存储 | ✅ 原生 Multipart Upload |
| **S3** | AWS S3 | ✅ 原生 Multipart Upload |

### 普通上传

```go
// 上传文件，自动计算 SHA256 校验和
file.Upload(c)
// 客户端可选传入 checksum 参数做完整性校验
```

### 分片上传 API

| 端点 | 说明 |
|------|------|
| `POST /api/v1/sys/file/upload/init` | 初始化分片上传 |
| `POST /api/v1/sys/file/upload/chunk` | 上传单个分片 |
| `POST /api/v1/sys/file/upload/complete` | 完成合并 |
| `POST /api/v1/sys/file/upload/abort` | 取消上传 |

### 切换存储后端

```yaml
# config.yaml
storage:
  default: MINIO
  minio:
    endpoint: localhost:9000
    access_key: minioadmin
    secret_key: minioadmin
    bucket: hei-files
```

业务代码无需任何修改。

## 定时调度

支持 cron 表达式和固定间隔的后台任务，优雅关闭。

```go
type CleanupTask struct{}
func (t *CleanupTask) Name() string { return "cleanup" }
func (t *CleanupTask) Run()         { /* ... */ }

func init() {
    scheduler.Register("0 */5 * * * *", &CleanupTask{})  // 每 5 分钟
    scheduler.RegisterInterval(30*time.Second, &CleanupTask{})  // 30 秒
    scheduler.Register("@daily", &CleanupTask{})          // 每天凌晨
}
```

## DB 迁移

```bash
# 自动迁移所有模块注册的模型 + 种子数据
go run cmd/migrate/main.go

# 仅迁移，跳过种子
go run cmd/migrate/main.go -skip-seed
```

新增模块只需在 `migrate.go` 中注册，`cmd/migrate` 自动发现。

## 相关项目

- **[Hei Boot](https://github.com/jiangbyte/hei-boot)** — Java Spring Boot 单体版本
- **[Hei FastAPI](https://github.com/jiangbyte/hei-fastapi)** — Python FastAPI 单体版本
- **[Hei Admin Vue](https://github.com/jiangbyte/hei-admin-vue)** — Vue3 前端管理后台

## 开源协议

本项目采用 [MIT License](LICENSE) 开源协议
