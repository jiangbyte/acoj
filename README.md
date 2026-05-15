# Hei Gin

<img width="120" src="https://jiangbyte.github.io/hei-docs/logo.svg">

![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)
![Go](https://img.shields.io/badge/Go-1.25+-brightgreen.svg)
![Gin](https://img.shields.io/badge/Gin-1.12+-blue.svg)
![Ent](https://img.shields.io/badge/Ent-0.14+-orange.svg)

## 简介

**Hei Gin** 是 HEI 快速开发框架的 Go 单体应用版本，基于 Gin + Ent（entgo.io）构建。提供开箱即用的快速开发解决方案，包含完善的权限管理（RBAC）、数据权限、认证授权、文件存储、操作日志等功能模块。框架采用前后端分离架构，支持快速搭建后台管理系统和 API 服务。

**在线文档**: [https://jiangbyte.github.io/hei-docs/hei-gin/](https://jiangbyte.github.io/hei-docs/hei-gin/)

## 技术栈

| 类型 | 技术 |
| --- | --- |
| 核心框架 | Go 1.25+ / Gin 1.12+ |
| ORM | Ent (entgo.io) |
| 数据库 | MySQL 8.0+ |
| 缓存 | Redis 6.0+ (go-redis) |
| 认证授权 | JWT / SM2 国密加密 / bcrypt 密码哈希 |
| Excel处理 | Excelize |
| 分布式ID | Snowflake ID 算法 |

## 核心特性

- **双端认证体系** — B端（后台管理）和 C端（客户端）独立的两套 JWT 认证、权限中间件
- **SM2 国密加密** — 登录密码传输使用国密 SM2 C1C3C2 模式加密
- **bcrypt 密码哈希** — 存储密码使用 bcrypt 加盐哈希
- **RBAC 权限控制** — 用户→角色→权限 + 用户直授权限，双层模型
- **权限自动发现** — 启动时自动扫描 `CheckPermission` 注册的权限并缓存到 Redis
- **权限匹配器** — 支持 `*` 单级和 `**` 多级通配符匹配
- **操作日志** — `SysLog` 中间件自动记录用户操作，支持请求参数和返回结果
- **防重复提交** — `NoRepeat` 中间件防止接口重复调用
- **链路追踪** — 基于 `X-Trace-Id` 的全链路追踪
- **统一验证码** — B端/C端独立的图形验证码服务
- **统一响应格式** — `{code, message, data, success, trace_id}` 标准结构
- **全局异常处理** — Recovery 中间件统一捕获 BusinessException 返回业务错误码
- **Excel 导入导出** — 通用模板下载、数据导出、数据导入
- **雪花ID** — 分布式 Snowflake ID 生成器

## 项目结构

```
hei-gin
├── config/
│   └── config.go                 # 配置结构体（YAML 加载）
├── core/                         # 框架核心
│   ├── app.go                    # 应用工厂 + 核心服务初始化
│   ├── router.go                 # 路由注册总入口
│   ├── health.go                 # 健康检查 GET /
│   ├── auth/
│   │   ├── auth_tool.go          # B端 JWT 认证工具
│   │   ├── client_auth_tool.go   # C端 JWT 认证工具
│   │   ├── permission_tool.go    # 权限查询门面 + CheckPermission 中间件
│   │   ├── permission_matcher.go # 权限匹配器（支持 * / ** 通配符）
│   │   ├── permission_interface.go # 权限查询接口（直授 + 角色双路径）
│   │   ├── permission_scan.go    # 权限自动扫描与缓存
│   ├── constants/
│   │   └── constants.go          # 缓存键常量、系统字段常量
│   ├── db/
│   │   ├── ent.go                # Ent 客户端初始化（MySQL）
│   │   └── redis.go              # Redis 客户端初始化
│   ├── enums/
│   │   └── enums.go              # 枚举定义（登录类型、状态、数据范围等）
│   ├── errors/
│   │   └── business_error.go     # BusinessException
│   ├── log/
│   │   └── decorator.go          # SysLog 操作日志中间件
│   ├── middleware/
│   │   ├── auth.go               # JWT 认证中间件（按路径分流 B/C/Public）
│   │   ├── cors.go               # CORS 中间件
│   │   ├── recovery.go           # 全局异常恢复中间件
│   │   └── trace.go              # 链路追踪中间件（X-Trace-Id）
│   ├── norepeat/
│   │   └── norepeat.go           # NoRepeat 防重复提交中间件
│   ├── result/
│   │   └── result.go             # Success / Failure / Page 响应工具
│   └── utils/
│       ├── crypto.go             # SM2 加解密 + bcrypt 哈希
│       ├── excel.go              # Excel 导出导入
│       ├── ip.go                 # 客户端 IP 提取
│       ├── model.go              # 模型工具（系统字段剥离、更新）
│       └── snowflake.go          # 雪花 ID 生成
├── ent/                          # Ent 生成的 ORM 模型与查询
│   ├── schema/                   # Ent Schema 定义
│   │   ├── sys_banner.go
│   │   ├── sys_config.go
│   │   ├── sys_dict.go
│   │   ├── sys_file.go
│   │   ├── sys_group.go
│   │   ├── sys_log.go
│   │   ├── sys_notice.go
│   │   ├── sys_org.go
│   │   ├── sys_permission.go
│   │   ├── sys_position.go
│   │   ├── sys_resource.go
│   │   ├── sys_role.go
│   │   ├── rel_user_role.go
│   │   ├── rel_user_permission.go
│   │   ├── rel_role_permission.go
│   │   ├── rel_role_resource.go
│   │   └── rel_org_role.go
│   └── ...                        # Ent 生成的 CRUD 代码
├── modules/
│   ├── sys/                       # B端（后台管理）
│   │   ├── auth/                  # 认证模块（验证码 / SM2 公钥 / 登录注册）
│   │   ├── banner/                # Banner 管理
│   │   ├── config/                # 系统配置管理
│   │   ├── dict/                  # 字典管理
│   │   ├── file/                  # 文件管理
│   │   ├── group/                 # 用户组管理
│   │   ├── home/                  # 首页仪表盘
│   │   ├── log/                   # 操作日志查询
│   │   ├── notice/                # 通知管理
│   │   ├── org/                   # 组织管理
│   │   ├── permission/            # 权限管理
│   │   ├── position/              # 职位管理
│   │   ├── resource/              # 资源管理（菜单 / 按钮）
│   │   ├── role/                  # 角色管理（含权限 / 资源分配）
│   │   ├── session/               # 在线会话管理
│   │   ├── analyze/               # 系统分析
│   │   └── user/                  # 用户管理（含角色 / 组分配）
│   ├── client/                    # C端（客户端）
│   │   ├── auth/                  # 认证模块
│   │   ├── session/               # 在线会话管理
│   │   └── user/                  # C端用户管理
│   └── biz/                       # 业务模块（用户自定义）
├── config.yaml                    # 配置文件
├── main.go                        # 应用入口
└── go.mod                         # Go 模块定义
```

### 模块结构约定

每个业务模块遵循垂直切片布局：

```
modules/<domain>/
├── api.go          # Gin 路由注册 + Handler 定义
└── service.go      # 业务逻辑层
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

db:
  host: localhost
  port: 3306
  user: root
  password: "123456"
  database: hei_data

redis:
  host: localhost
  port: 6379
  password: "123456"
  database: 1

jwt:
  secret_key: your-jwt-secret-key
  expire_seconds: 2592000
  token_name: Authorization

sm2:
  private_key: your-sm2-private-key
  public_key: your-sm2-public-key
```

### 运行

```bash
go run main.go
```

服务启动后访问：

- 健康检查：<http://localhost:18885/>
- Swagger 文档：<http://localhost:18885/docs>

## 认证体系

框架提供四套路径规则：

| 路径模式 | 认证方式 | 说明 |
|---------|---------|------|
| `/api/v1/b/*` | AuthTool 认证 + CheckPermission | B端后台管理 |
| `/api/v1/c/*` | ClientAuthTool 认证 + CheckPermission | C端客户端 |
| `/api/v1/public/b/*` | 无认证 | B端公开接口 |
| `/api/v1/public/c/*` | 无认证 | C端公开接口 |

认证中间件按路径前缀自动分流，CORS 预检请求（OPTIONS）跳过认证。

## 中间件参考

### 权限中间件

```go
import "hei-gin/core/auth"

r.GET("/api/v1/sys/banner/page",
    auth.CheckPermission("sys:banner:page"),
    PageHandler,
)
```

### 操作日志

```go
import "hei-gin/core/log"

r.POST("/api/v1/sys/banner/create",
    log.SysLog("添加Banner"),
    auth.CheckPermission("sys:banner:create"),
    CreateHandler,
)
```

### 防重复提交

```go
import "hei-gin/core/norepeat"

r.POST("/api/v1/sys/xxx/create",
    norepeat.NoRepeat(3000), // 3 秒内相同参数禁止重复提交
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

## 相关项目

- **[Hei Boot](https://github.com/jiangbyte/hei-boot)** — Java Spring Boot 单体版本
- **[Hei FastAPI](https://github.com/jiangbyte/hei-fastapi)** — Python FastAPI 单体版本
- **[Hei Admin Vue](https://github.com/jiangbyte/hei-admin-vue)** — Vue3 前端管理后台

## 开源协议

本项目采用 [MIT License](LICENSE) 开源协议
