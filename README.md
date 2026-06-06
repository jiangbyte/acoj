# acoj

> **acoj** — 基于 Go 的开源在线评测系统（Online Judge）。
> 后端采用 **Gin + GORM** 构建，前端分为管理后台与公开前台，提供完整的判题、竞赛、题库管理能力。

<img width="120" src="vitepress/docs/public/logo.svg">

![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)
![Go](https://img.shields.io/badge/Go-1.25+-brightgreen.svg)
![Gin](https://img.shields.io/badge/Gin-1.12+-blue.svg)
![GORM](https://img.shields.io/badge/GORM-1.25+-blue.svg)

## 项目仓库

| 项目 | 说明 |
|------|------|
| [acoj](https://github.com/jiangbyte/acoj) | 后端服务（当前仓库） |
| [acoj-admin](https://github.com/jiangbyte/acoj-admin) | 管理后台前端（B 端） |
| [acoj-pc](https://github.com/jiangbyte/acoj-pc) | 公开前台前端（C 端） |

## 简介

**acoj** 是一个功能完整的在线评测系统后端，基于 **Gin + GORM** 构建。提供判题引擎（支持 default / SPJ / Interactive 三种模式）、竞赛管理（ACM / OI 规则）、题库管理、题单系统、沙箱连接池等核心 OJ 功能。系统采用插件化架构、前后端分离、双端认证体系。

## 预览

![](./docs/readme/login.png)

![](./docs/readme/dashboard.png)

![](./docs/readme/home.png)

## 技术栈

| 类型 | 技术 |
| --- | --- |
| 核心框架 | Go 1.25+ / Gin 1.12+ |
| ORM | GORM (gorm.io/gorm) |
| 数据库 | MySQL 8.0+ |
| 缓存 | Redis 6.0+ (go-redis) |
| 判题沙箱 | go-judge (gRPC) |
| 认证授权 | Token / SM2 国密加解密 / SM3 哈希 / bcrypt 密码哈希 |
| 文件存储 | MinIO / S3 (AWS SDK) / Local 本地存储 |
| 定时调度 | cron 表达式 (robfig/cron/v3) |
| 分布式 ID | Snowflake ID |
| WebSocket | gorilla/websocket + go-redis List/BRPOP 跨实例 IM |

## OJ 核心特性

- **判题引擎** — 多 Worker 并发判题，支持 default / SPJ / Interactive 三种判题模式
- **沙箱连接池** — 并发安全的 go-judge gRPC 连接池，健康检查自动下线/恢复
- **题库管理** — 题目 CRUD、多级分类标签、测试用例批量管理
- **竞赛系统** — ACM / OI 规则竞赛、排行榜（AC 数降序 / 罚时升序）
- **题单系统** — 题单编排、题目关联排序
- **提交管理** — 提交记录分页查询、重新判题、判题队列
- **插件化架构** — 业务模块以插件形式自注册，自动发现路由/权限/模型
- **双端认证体系** — B 端（后台管理）和 C 端（客户端）独立的两套 Token 认证
- **RBAC 权限控制** — 用户→角色→权限 + 用户直授权限，双层模型
- **数据权限（行级）** — 支持 ALL / ORG / SELF 等 8 种数据范围控制
- **操作日志** — SysLog 中间件自动记录用户操作，SM3 签名防篡改
- **WebSocket IM** — 跨实例即时通讯，支持好友、群聊、会话管理
- **链路追踪** — 基于 trace_id 的全链路追踪
- **抽象文件存储** — 统一的 Engine 接口，三种后端（Local/MinIO/S3）
- **定时调度** — 支持 cron 表达式和固定间隔的后台任务
- **DB 自动迁移** — cmd/migrate 命令行工具自动发现所有模块注册的 Model
- **模块生命周期** — module.Register() 统一管理模块的 Init / Start / Stop
- **事件总线** — 内置内存事件总线，支持模块间解耦通信

## 项目结构

```
acoj/
├── main.go                          # 应用入口
├── go.mod / go.work                 # Go 模块定义
├── config.yaml                      # 配置文件
│
├── sdk/                             # 框架 SDK（核心基础设施）
│   ├── app/                         # 应用工厂
│   ├── auth/                        # 认证与权限系统
│   ├── db/                          # 数据库初始化
│   ├── module/                      # 模块生命周期管理
│   ├── registry/                    # 注册中心
│   └── ...
│
├── plugins/
│   ├── plugin-judge/               # 评测插件（OJ 核心）
│   │   ├── sandbox/                # 沙箱抽象层（go-judge gRPC）
│   │   ├── judge/                  # 判题引擎
│   │   ├── problem/                # 题库管理
│   │   ├── testcase/               # 测试用例
│   │   ├── submission/             # 提交 + 判题队列
│   │   ├── contest/                # 竞赛 + 排行榜
│   │   ├── problemset/             # 题单
│   │   └── tag/                    # 标签
│   ├── plugin-sys/                 # 系统管理
│   ├── plugin-client/              # 客户端
│   └── plugin-im/                  # WebSocket IM
│
├── cmd/migrate/                    # DB 迁移工具
└── docs/                           # 设计文档
```

## 快速开始

### 前置条件

- Go 1.25+
- MySQL 8.0+
- Redis 6.0+
- go-judge（判题沙箱，可选）

### 启动步骤

```bash
# 1. 配置数据库和 Redis
cp config.example.yaml config.yaml
# 编辑 config.yaml 填写数据库连接信息

# 2. 运行数据库迁移
go run cmd/migrate/main.go

# 3. 启动服务
go run main.go
```

服务默认监听 `127.0.0.1:18886`。

## 许可证

[MIT](LICENSE)
