# ACOJ

![JDK](https://img.shields.io/badge/JDK-21-007396)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1-6DB33F)
![Vue](https://img.shields.io/badge/Vue-3-4FC08D)
![React](https://img.shields.io/badge/React-19-61DAFB)
![MySQL](https://img.shields.io/badge/MySQL-Supported-4479A1)
![Redis](https://img.shields.io/badge/Redis-Supported-DC382D)
![License](https://img.shields.io/badge/License-Apache_2.0-blue)

**ACOJ** 是一套在线评测（OJ）平台 monorepo：在 HEI 脚手架之上扩展题库、提交判题与多执行机调度，同时提供 **Admin**（Vue）与 **Portal**（React）双端，以及统一的 Spring Boot API。判题执行依赖 [SparkSandbox](https://github.com/jiangbyte/SparkSandbox)（只负责编译与运行；AC / WA 等裁决在 ACOJ 侧完成）。

> 协议：[Apache License 2.0](LICENSE)

## 目录

- [功能特性](#功能特性)
- [子工程](#子工程)
- [技术栈](#技术栈)
- [工程结构](#工程结构)
- [设计文档](#设计文档)
- [快速开始](#快速开始)
- [默认账号](#默认账号)
- [相关项目](#相关项目)
- [License](#license)

## 功能特性

API 前缀统一为 `/api/v1/admin/*` 与 `/api/v1/portal/*`，能力按模块划分如下：

| 模块 | 说明 |
| --- | --- |
| 双端账号体系 | ADMIN / PORTAL 独立会话（Sa-Token）；密码 RSA 传输、验证码登录、失败锁定与限流；JustAuth 三方登录（可配置） |
| RBAC 权限 | 账号 / 角色 / 部门 / 用户组 / 岗位；菜单、按钮与 API 资源授权；在线会话踢出 |
| 系统管理 | 字典、动态配置（敏感项加密存储）、Banner、公告 / 通知、意见反馈、弱口令库 |
| 对象存储 | S3 兼容存储（MinIO / RustFS / 阿里云 OSS 等），直链或预签名访问 |
| 运维能力 | 操作审计与告警、登录日志、运营工作台概览、内置任务调度（`sys_job`） |
| 题库 | 题目 CRUD、测例（INLINE / OBJECT）、标签、参考答案、管理端试跑与发布校验 |
| 提交与判题 | 多语言提交入队；RabbitMQ 判题队列；多执行机加权调度、熔断 / 排水 / 租约换机；业务侧裁决 AC / WA 等 |
| 判题节点 | `oj_judge_node` 登记 SparkSandbox；心跳探活、派发审计（`oj_judge_dispatch`） |
| 个人中心 | 公开资料、安全设置、实名认证；做题统计等 OJ 扩展挂到同一体系 |

更完整的功能模块说明见 [docs/功能模块设计.md](docs/功能模块设计.md)。

## 子工程

| 目录 | 说明 |
| --- | --- |
| [**admin**](./admin) | Vue 3 管理端，对接 `/api/v1/admin/*` |
| [**portal**](./portal) | React 门户，对接 `/api/v1/portal/*` |
| [**server**](./server) | Spring Boot 后端（Admin + Portal API，含 `modules/oj`） |

## 技术栈

| 层级 | 技术 |
| --- | --- |
| 后端 | JDK 21 · Spring Boot 4.1 · Maven 多模块 · 虚拟线程 |
| 持久化 | MySQL / PostgreSQL · MyBatis-Plus · Dynamic Datasource |
| 缓存 / 会话 / 队列 | Redis · Redisson · Sa-Token · RabbitMQ（判题） |
| 管理端 | Vue 3 · Vite · TypeScript · Naive UI · UnoCSS |
| 门户 | React 19 · Vite · TypeScript · Ant Design |
| 判题执行 | SparkSandbox（HMAC 调用） |
| 其他 | JustAuth · AWS SDK v2（S3）· Hutool · MapStruct · Knife4j / SpringDoc |

## 工程结构

```text
acoj/
├── admin/                  # 管理端（Vue 3）
├── portal/                 # 门户（React）
├── server/                 # 后端（HEI Boot + OJ）
│   ├── app/admin/          # 可启动应用（Admin + Portal API）
│   ├── common/             # 公共能力
│   ├── modules/            # 业务实现（auth / iam / sys / oj …）
│   └── scripts/            # 数据库脚本、Docker Compose
└── docs/                   # 功能与判题设计文档
```

`server/scripts/` 常用文件：

| 文件 | 用途 |
| --- | --- |
| `hei_boot.sql` | MySQL 全量建表、种子数据与表/列 `COMMENT` |
| `oj_p0*.sql` | OJ 相关增量脚本（字典、菜单、表结构变更等） |
| `docker/docker-compose.yml` | 全栈 Compose（中间件 + 应用） |
| `docker/.env.example` | 部署环境变量模板（复制为 `.env`，**勿提交**） |

## 设计文档

| 文档 | 内容 |
| --- | --- |
| [功能模块设计](docs/功能模块设计.md) | 端到端功能模块与端侧约定 |
| [数据库设计](docs/p0-数据库设计.md) | `oj_*` 表结构、枚举、测例版本化 |
| [判题对接](docs/p0-判题对接.md) | RabbitMQ、调度、熔断换机、业务裁决 |
| [判题运维](docs/p0-判题运维.md) | 拓扑、密钥、SLO、告警与演练 |

## 快速开始

### 环境要求

- JDK **21**、Maven **3.9+**
- Node.js **18+**、pnpm **8+**
- MySQL 8+、Redis；判题需 RabbitMQ 与可用的 SparkSandbox 节点

### 1. 初始化数据库

```bash
mysql -u root -p -e "CREATE DATABASE hei_boot DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mysql -u root -p hei_boot < server/scripts/hei_boot.sql
# 按需执行 server/scripts/oj_p0*.sql 增量
```

### 2. 启动后端

```bash
cd server
mvn -pl app/admin -am spring-boot:run
```

| 项 | 地址 |
| --- | --- |
| API | http://127.0.0.1:8000 |
| 接口文档（Knife4j） | http://127.0.0.1:8000/doc.html |

详情与 Compose 部署见 [server/README.md](server/README.md)。

### 3. 启动前端

```bash
# 管理端 → http://127.0.0.1:5173
cd admin && pnpm install && pnpm dev

# 门户 → http://127.0.0.1:5174
cd portal && pnpm install && pnpm dev
```

默认将 `/api` 代理到 `http://127.0.0.1:8000`。详见 [admin/README.md](admin/README.md) / [portal/README.md](portal/README.md)。

## 默认账号

| 端 | 地址 | 账号 | 密码 | 说明 |
| --- | --- | --- | --- | --- |
| Admin | http://127.0.0.1:5173 | `superadmin` | `123456` | 超级管理员（`*:*:*`） |

> 仅供本地演示。部署后请修改默认密码，并更换配置加密密钥、对象存储与判题节点密钥等敏感项。

## 相关项目

| 项目 | 说明 | 协议 |
| --- | --- | --- |
| [**SparkSandbox**](https://github.com/jiangbyte/SparkSandbox) | 判题沙箱（编译与限资源执行） | — |
| [**hei-boot**](https://github.com/jiangbyte/hei-boot) | Spring Boot 脚手架（本仓库 `server` 同源） | Apache License 2.0 |
| [**hei-admin**](https://github.com/jiangbyte/hei-admin) | Vue 3 管理端脚手架 | Apache License 2.0 |
| [**hei-portal**](https://github.com/jiangbyte/hei-portal) | React 门户脚手架 | Apache License 2.0 |

## License

本项目基于 [Apache License 2.0](LICENSE) 开源。完整条款见 [LICENSE](LICENSE)，版权声明见 [NOTICE](NOTICE)。
