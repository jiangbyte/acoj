# Hei GoFrame

<img width="120" src="https://jiangbyte.github.io/hei-docs/logo.svg">

![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)
![Go](https://img.shields.io/badge/Go-1.25+-orange.svg)
![GoFrame](https://img.shields.io/badge/GoFrame-2.10+-brightgreen.svg)

## 简介

**Hei GoFrame** 是 HEI 快速开发框架的 Go 单体应用版本，基于 GoFrame v2 + MySQL + Redis 构建。提供开箱即用的快速开发解决方案，包含完善的权限管理（RBAC）、数据权限、认证授权等功能模块。框架采用前后端分离架构，支持快速搭建管理系统和 API 服务。

**在线文档**: [https://jiangbyte.github.io/hei-docs/hei-goframe/](https://jiangbyte.github.io/hei-docs/hei-goframe/)

## 技术栈

| 类型 | 技术 |
| --- | --- |
| 核心框架 | Go 1.25+ / GoFrame v2.10 / Goframe |
| ORM | GoFrame ORM (MySQL 8.0+) |
| 数据库 | MySQL 8.0+ |
| 缓存 | Redis 6.0+ (go-redis) |
| 认证授权 | JWT / SM2 国密加密 / bcrypt 密码哈希 |
| 验证码 | base64Captcha |
| 分布式ID | Snowflake ID (bwmarrin/snowflake) |

## 核心特性

- **双端认证体系** — B端（后台管理）和 C端（客户端）独立的 JWT 认证与中间件
- **SM2 国密加密** — 登录密码传输使用国密 SM2 C1C3C2 模式加密
- **bcrypt 密码哈希** — 存储密码使用 bcrypt 加盐哈希
- **RBAC 权限控制** — 用户→角色→权限 + 用户→组→角色→权限 + 用户直授权限，三层模型
- **数据权限** — 支持 ALL / ORG / ORG_AND_BELOW / SELF / CUSTOM_ORG / GROUP / GROUP_AND_BELOW / CUSTOM_GROUP 八种数据权限粒度，多路径按最严策略合并
- **权限自动发现** — 启动时自动扫描权限标识，缓存到 Redis
- **统一响应格式** — `{code, message, data, success, trace_id}` 标准结构
- **全局中间件** — 统一认证、CORS、响应处理中间件
- **验证码** — 内置图形验证码生成与校验
- **雪花ID** — 分布式 Snowflake ID 生成器

## 项目结构

```
hei-goframe
├── api/
│   ├── client/                     # C端 API 定义
│   │   ├── auth/captcha/           # 验证码
│   │   ├── auth/username/          # 用户名登录
│   │   ├── session/                # 会话管理
│   │   └── user/                   # 用户管理
│   ├── public/                     # 公开 API 定义
│   │   └── sm2/                    # SM2 公钥获取
│   └── sys/                        # B端（后台管理）API 定义
│       ├── auth/captcha/           # 验证码
│       ├── auth/username/          # 用户名密码登录
│       ├── banner/                 # Banner 管理
│       ├── config/                 # 配置管理
│       ├── dict/                   # 字典管理（树形结构）
│       ├── file/                   # 文件管理
│       ├── group/                  # 用户组管理
│       ├── home/                   # 首页
│       ├── log/                    # 操作日志
│       ├── notice/                 # 通知管理
│       ├── org/                    # 组织管理
│       ├── permission/             # 权限管理
│       ├── position/               # 职位管理
│       ├── resource/               # 资源管理（菜单/按钮）
│       ├── role/                   # 角色管理（含权限/资源分配）
│       ├── session/                # 会话管理
│       └── user/                   # 用户管理（含角色/组分配）
├── internal/
│   ├── cmd/
│   │   └── cmd.go                  # 应用入口（路由注册、中间件、初始化）
│   ├── consts/
│   │   └── consts.go              # 全局常量定义
│   ├── controller/                 # 控制器层
│   │   ├── client/                 # C端控制器
│   │   ├── public/                 # 公开控制器
│   │   └── sys/                    # B端控制器
│   ├── dao/                        # 数据访问层（GoFrame ORM）
│   ├── model/
│   │   ├── do/                     # 数据模型（Data Object）
│   │   └── entity/                 # 实体模型
│   └── service/                    # 业务逻辑层
│       ├── auth/                   # 认证授权服务
│       ├── captcha/                # 验证码服务
│       ├── client/                 # C端业务服务
│       └── sys/                    # B端业务服务
├── manifest/
│   └── config/
│       └── config.yaml             # 应用配置文件
├── utility/
│   ├── page.go                     # 分页工具
│   ├── response.go                 # 统一响应中间件 & 工具函数
│   ├── sm2.go                      # SM2 国密加解密
│   └── snowflake.go                # 雪花 ID 生成
├── go.mod
├── go.sum
└── main.go                         # 应用入口
```

### 模块结构约定

每个业务模块遵循垂直切片布局：

```
api/<domain>/v1/
├── xxx.go              # API 定义（输入/输出结构体 + 路由注册）
internal/
├── controller/<domain>/v1/
│   └── xxx.go          # 控制器（参数校验、调用 Service）
├── service/<domain>/
│   └── xxx.go          # 业务逻辑层
├── dao/
│   └── xxx.go          # 数据访问层（GoFrame ORM 自动生成）
└── model/
    ├── do/
    │   └── xxx.go      # 数据对象
    └── entity/
        └── xxx.go      # 实体模型
```

## 快速开始

### 环境要求

- Go 1.25+
- MySQL 8.0+
- Redis 6.0+

### 配置

编辑 `manifest/config/config.yaml` 文件：

```yaml
server:
  address: ":8080"

database:
  default:
    link: "mysql:root:123456@tcp(localhost:3306)/hei_data"

redis:
  default:
    address: "localhost:6379"
    password: "123456"

hei:
  sm2:
    privateKey: "your-sm2-private-key"
    publicKey: "your-sm2-public-key"
  jwt:
    secretKey: "your-jwt-secret-key"
```

### 初始化数据库

```bash
# 导入 DDL（请从相关项目获取建表脚本）
mysql -u root -p < scripts/sqls/hei_ddl.sql
```

### 启动服务

```bash
go run main.go
```

服务启动后访问：

- API 文档：<http://localhost:8080/docs>
- 健康检查：<http://localhost:8080/>

## 认证体系

框架提供三套路径规则：

| 路径模式 | 认证方式 | 说明 |
|---------|---------|------|
| `/api/v1/b/*` | Auth 中间件 + JWT 校验 | B端后台管理 |
| `/api/v1/c/*` | Auth 中间件 + Client JWT 校验 | C端客户端 |
| `/api/v1/public/b/*`, `/api/v1/public/c/*` | 无 | 公开接口 |

认证中间件按路径前缀自动分流，CORS 预检请求（OPTIONS）跳过认证。

## API 规范

### 统一响应格式

```json
{
  "code": 200,
  "message": "请求成功",
  "data": {},
  "success": true,
  "trace_id": ""
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
  "trace_id": ""
}
```

## 权限数据链路

```
User ──→ RelUserRole ──→ Role ──→ RelRolePermission ──→ Permission
User ──→ RelUserGroup ──→ Group ──→ RelGroupRole ──→ Role ──→ ...
User ──→ RelUserPermission ──→ Permission (直授)
```

数据范围存储在关系表中（`rel_role_permission`、`rel_user_role` 的 scope 字段），多角色多路径下按最严策略合并（SELF < CUSTOM < ORG_AND_BELOW < ORG < ALL）。

## 相关项目

- **[Hei FastAPI](https://github.com/jiangbyte/hei-fastapi)** — Python FastAPI 单体版本
- **[Hei Boot](https://github.com/jiangbyte/hei-boot)** — Java Spring Boot 单体版本
- **[Hei Cloud](https://github.com/jiangbyte/hei-cloud)** — Java 微服务版本
- **[Hei Admin Vue](https://github.com/jiangbyte/hei-admin-vue)** — Vue3 前端管理后台

## 致谢

- **[GoFrame](https://github.com/gogf/gf)** — 优秀的 Go 语言开发框架

## 开源协议

本项目采用 [MIT License](LICENSE) 开源协议
