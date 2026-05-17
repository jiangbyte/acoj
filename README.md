# Hei Gin

<img width="120" src="https://jiangbyte.github.io/hei-docs/logo.svg">

![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)
![Go](https://img.shields.io/badge/Go-1.25+-brightgreen.svg)
![Gin](https://img.shields.io/badge/Gin-1.12+-blue.svg)
![Ent](https://img.shields.io/badge/Ent-0.14+-orange.svg)

## 简介

**Hei Gin** 是 HEI 快速开发框架的 Go 单体应用版本，基于 Gin + Ent（entgo.io）构建。提供开箱即用的快速开发解决方案，包含完善的权限管理（RBAC）、数据权限、认证授权、文件存储（MinIO/S3/Local）、操作日志等功能模块。框架采用前后端分离架构，支持快速搭建后台管理系统和 API 服务。

**在线文档**: [https://jiangbyte.github.io/hei-docs/hei-gin/](https://jiangbyte.github.io/hei-docs/hei-gin/)

## 技术栈

| 类型 | 技术 |
| --- | --- |
| 核心框架 | Go 1.25+ / Gin 1.12+ |
| ORM | Ent (entgo.io) |
| 数据库 | MySQL 8.0+ |
| 缓存 | Redis 6.0+ (go-redis) |
| 认证授权 | JWT / SM2 国密加解密 / SM3 哈希 / bcrypt 密码哈希 |
| 文件存储 | MinIO / S3 (AWS SDK) / Local 本地存储 |
| IP 定位 | IP2Region 离线库 |
| 分布式ID | Snowflake ID (bwmarrin/snowflake) |

## 核心特性

- **双端认证体系** — B端（后台管理）和 C端（客户端）独立的两套 JWT 认证、权限中间件
- **SM2 国密加密** — 登录密码传输使用国密 SM2 C1C3C2 模式加密，支持 C1C2C3 / C1C3C2 双模式解密
- **SM3 哈希** — 支持 SM3 摘要算法，用于操作日志防篡改签名
- **bcrypt 密码哈希** — 存储密码使用 bcrypt 加盐哈希
- **RBAC 权限控制** — 用户→角色→权限 + 用户直授权限，双层模型
- **数据权限（行级）** — 支持 ALL / ORG / ORG_AND_BELOW / SELF / CUSTOM_ORG / GROUP / GROUP_AND_BELOW / CUSTOM_GROUP 等数据范围控制
- **权限自动发现** — 启动时自动扫描 `middleware.HeiCheckPermission` / `auth.RegisterPermission` 注册的权限并缓存到 Redis
- **权限匹配器** — 支持 `*` 单级和 `**` 多级通配符匹配
- **操作日志** — `SysLog` 中间件自动记录用户操作，支持请求参数、UA 解析、IP 归属地、SM3 签名防篡改
- **防重复提交** — `NoRepeat` 中间件防止接口重复调用（基于 Redis）
- **链路追踪** — 基于 `trace_id` 的全链路追踪，支持从请求头读取或自动生成
- **统一验证码** — B端/C端独立的图形验证码服务
- **统一响应格式** — `{code, message, data, success, trace_id}` 标准结构
- **全局异常处理** — Recovery 中间件统一捕获 BusinessException 返回业务错误码
- **抽象文件存储** — 统一的 `FileStorage` 接口，支持本地存储、MinIO、S3 三种后端，切换无需修改业务代码
- **IP 归属地** — 基于 IP2Region 离线库的 IP 地址定位
- **在线会话管理** — B端和 C端独立的会话管理，支持在线用户查看和强制下线
- **雪花ID** — 分布式 Snowflake ID 生成器

## 项目结构

```
hei-gin
├── config/
│   └── config.go                   # 配置结构体（YAML 加载）
├── core/                           # 框架核心
│   ├── app/
│   │   ├── app.go                  # 应用工厂 + 核心服务初始化
│   │   ├── health.go               # 健康检查 GET /
│   │   └── router.go               # 路由注册总入口
│   ├── auth/
│   │   ├── auth_tool.go            # B端 JWT 认证工具
│   │   ├── client_auth_tool.go     # C端 JWT 认证工具
│   │   ├── permission_tool.go      # 权限查询门面（HasPermission / CheckPermission）
│   │   ├── permission_matcher.go   # 权限匹配器（支持 * / ** 通配符）
│   │   ├── permission_interface.go # 权限查询接口定义（直授 + 角色双路径）
│   │   ├── permission_interface_manager.go # 权限接口管理器
│   │   ├── permission_scan.go      # 权限自动扫描与缓存
│   │   ├── middleware/
│   │   │   ├── check_login.go           # B端登录检查中间件
│   │   │   ├── client_check_login.go    # C端登录检查中间件
│   │   │   ├── check_permission.go      # B端权限检查中间件（HeiCheckPermission）
│   │   │   ├── client_check_permission.go # C端权限检查中间件
│   │   │   ├── check_role.go            # B端角色检查中间件
│   │   │   ├── client_check_role.go     # C端角色检查中间件
│   │   │   └── norepeat.go             # NoRepeat 防重复提交中间件
│   │   └── pojo/
│   │       ├── login_user_info.go          # B端登录用户信息
│   │       └── login_client_user_info.go   # C端登录用户信息
│   ├── captcha/
│   │   └── captcha.go              # 图形验证码服务（B端/C端）
│   ├── constants/
│   │   ├── constants.go            # 系统常量（SUPER_ADMIN_CODE）
│   │   ├── cache_keys.go           # Redis 缓存键常量
│   │   └── base_fields.go          # 系统基础字段定义
│   ├── db/
│   │   ├── ent.go                  # Ent 客户端初始化（MySQL）
│   │   └── redis.go                # Redis 客户端初始化
│   ├── enums/
│   │   ├── permission.go           # 登录类型、权限类别、数据范围、检查模式枚举
│   │   ├── status.go               # 状态枚举（启用/禁用/锁定等）
│   │   ├── resource.go             # 资源类型和类别枚举
│   │   └── page_data_field.go      # 分页响应字段枚举
│   ├── exception/
│   │   └── business_error.go       # BusinessException 业务异常
│   ├── log/
│   │   ├── syslog.go               # SysLog 操作日志中间件
│   │   ├── record.go               # RecordAuthLog 认证日志记录
│   │   └── utils.go                # 日志工具（UA 解析、参数提取、SM3 签名）
│   ├── middleware/
│   │   ├── auth_check.go           # JWT 认证中间件（按路径分流 B/C/Public）
│   │   ├── cors.go                 # CORS 中间件
│   │   ├── recovery.go             # 全局异常恢复中间件
│   │   └── trace.go                # 链路追踪中间件（trace_id）
│   ├── pojo/
│   │   ├── datetime_mixin.go       # 时间日期混入结构
│   │   └── id_params.go            # ID 参数结构
│   ├── result/
│   │   └── result.go               # Success / Failure / Page 响应工具
│   ├── storage/
│   │   ├── interface.go            # FileStorage 接口定义
│   │   ├── local.go                # Local 本地文件存储
│   │   ├── minio.go                # MinIO 对象存储
│   │   └── s3.go                   # AWS S3 对象存储
│   └── utils/
│       ├── crypto.go               # SM2 加解密 + SM3 哈希 + bcrypt
│       ├── ip.go                   # 客户端 IP 提取 + IP2Region 城市查询
│       ├── model.go                # 模型工具（系统字段剥离、更新）
│       ├── resolve.go              # 层级路径解析工具
│       ├── snowflake.go            # 雪花 ID 生成
│       ├── trace.go                # 链路追踪 ID 生成
│       └── user_agent.go           # 浏览器 / OS 解析
├── ent/                            # Ent 生成的 ORM 模型与查询
│   ├── schema/                     # Ent Schema 定义
│   │   ├── sysuser.go              # 用户
│   │   ├── sysrole.go              # 角色
│   │   ├── syspermission.go        # 权限
│   │   ├── sysresource.go          # 资源（菜单 / 按钮）
│   │   ├── sysorg.go               # 组织
│   │   ├── sysposition.go          # 职位
│   │   ├── sysgroup.go             # 用户组
│   │   ├── sysmodule.go            # 模块
│   │   ├── sysbanner.go            # Banner
│   │   ├── sysconfig.go            # 系统配置
│   │   ├── sysdict.go              # 字典
│   │   ├── sysfile.go              # 文件
│   │   ├── syslog.go               # 操作日志
│   │   ├── sysnotice.go            # 通知公告
│   │   ├── sysquickaction.go       # 快捷操作
│   │   ├── clientuser.go           # C端用户
│   │   ├── reluserrole.go          # 用户角色关联
│   │   ├── reluserpermission.go    # 用户直授权限关联
│   │   ├── relrolepermission.go    # 角色权限关联
│   │   └── relroleresource.go      # 角色资源关联
│   └── ...                          # Ent 生成的 CRUD 代码
├── modules/
│   ├── sys/                         # B端（后台管理）
│   │   ├── auth/                    # 认证模块（验证码 / SM2 公钥 / 登录注册登出）
│   │   │   ├── route.go             # 子路由聚合
│   │   │   ├── captcha/api/v1/api.go
│   │   │   ├── sm2/api/v1/api.go
│   │   │   └── username/
│   │   │       ├── params.go        # 请求/响应参数
│   │   │       ├── logic.go         # 登录逻辑
│   │   │       └── api/v1/api.go    # 路由注册 + Handler
│   │   ├── banner/                  # Banner 管理
│   │   ├── config/                  # 系统配置管理
│   │   ├── dict/                    # 字典管理
│   │   ├── file/                    # 文件管理（上传/下载）
│   │   ├── group/                   # 用户组管理
│   │   ├── home/                    # 首页仪表盘 + 快捷操作
│   │   ├── log/                     # 操作日志查询 + 图表
│   │   ├── notice/                  # 通知公告管理
│   │   ├── org/                     # 组织管理（树形结构）
│   │   ├── permission/              # 权限管理
│   │   ├── position/                # 职位管理
│   │   ├── resource/                # 资源管理（模块 / 菜单 / 按钮）
│   │   ├── role/                    # 角色管理（含权限 / 资源分配）
│   │   ├── session/                 # 在线会话管理
│   │   ├── analyze/                 # 系统分析仪表盘
│   │   └── user/                    # 用户管理（含角色 / 组分配）
│   └── client/                      # C端（客户端）
│       ├── auth/                    # 认证模块（验证码 / SM2 公钥 / 登录注册登出）
│       │   ├── route.go
│       │   ├── captcha/api/v1/api.go
│       │   ├── sm2/api/v1/api.go
│       │   └── username/
│       │       ├── params.go
│       │       ├── logic.go
│       │       └── api/v1/api.go
│       ├── session/                 # 在线会话管理
│       └── user/                    # C端用户管理
├── config.yaml                      # 配置文件
├── scripts/
│   └── hei_data.sql                 # 数据库初始化脚本
├── main.go                          # 应用入口
└── go.mod                           # Go 模块定义
```

### 模块结构约定

每个业务模块遵循垂直切片布局：

```
modules/<domain>/<module>/
├── params.go          # 请求/响应参数结构体
├── service.go         # 业务逻辑层
└── api/v1/
    └── api.go         # Gin 路由注册 + Handler 函数
```

认证模块因包含多个子模块（captcha / sm2 / username），额外包含 `route.go` 聚合子路由和 `logic.go` 逻辑文件。

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

jwt:
  secret_key: your-jwt-secret-key
  algorithm: HS256
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
```

### 初始化数据库

```bash
# 导入表结构和初始数据
mysql -u root -p hei_data < scripts/hei_data.sql
```

### 运行

```bash
go run main.go
```

服务启动后访问：

- 健康检查：<http://localhost:18885/>

## 认证体系

框架通过路径前缀自动分流认证方式：

| 路径模式 | 认证方式 | 说明 |
|---------|---------|------|
| `/api/v1/b/*` | AuthTool（B端JWT）认证 + CheckPermission | B端后台管理 |
| `/api/v1/c/*` | ClientAuthTool（C端JWT）认证 + CheckPermission | C端客户端 |
| `/api/v1/public/*` | 无认证 | 公开接口 |

认证中间件按路径前缀自动分流，CORS 预检请求（OPTIONS）和静态路径（/favicon.ico 等）跳过认证。

## 中间件参考

### 权限中间件

```go
import "hei-gin/core/auth/middleware"

r.GET("/api/v1/sys/banner/page",
    middleware.HeiCheckPermission([]string{"sys:banner:page"}),
    PageHandler,
)
```

支持 `AND`（默认，全部权限）和 `OR`（任一权限）两种模式：

```go
// OR 模式：满足任一权限即可
middleware.HeiCheckPermission([]string{"sys:user:view", "sys:user:edit"}, "OR")
```

### 操作日志

```go
import "hei-gin/core/log"

r.POST("/api/v1/sys/banner/create",
    log.SysLog("添加Banner"),
    middleware.HeiCheckPermission([]string{"sys:banner:create"}),
    CreateHandler,
)
```

### 防重复提交

```go
import "hei-gin/core/auth/middleware"

r.POST("/api/v1/sys/xxx/create",
    middleware.NoRepeat(3000), // 3 秒内相同参数禁止重复提交
    CreateHandler,
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

权限匹配支持 `*`（单级通配符）和 `**`（多级通配符），例如 `sys:*` 匹配所有 sys 模块权限。

## 文件存储

框架通过统一的 `FileStorage` 接口抽象文件存储后端，支持三种实现，切换仅需修改初始化代码：

| 存储后端 | 实现 |
|---------|------|
| Local 本地存储 | `core/storage/local.go` |
| MinIO 对象存储 | `core/storage/minio.go` |
| AWS S3 对象存储 | `core/storage/s3.go` |

## 相关项目

- **[Hei Boot](https://github.com/jiangbyte/hei-boot)** — Java Spring Boot 单体版本
- **[Hei FastAPI](https://github.com/jiangbyte/hei-fastapi)** — Python FastAPI 单体版本
- **[Hei Admin Vue](https://github.com/jiangbyte/hei-admin-vue)** — Vue3 前端管理后台

## 开源协议

本项目采用 [MIT License](LICENSE) 开源协议
