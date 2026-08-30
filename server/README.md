# HEI Boot

![JDK](https://img.shields.io/badge/JDK-21-007396?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1-6DB33F?logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Supported-4169E1?logo=postgresql&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-Supported-4479A1?logo=mysql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-Supported-DC382D?logo=redis&logoColor=white)
![License](https://img.shields.io/badge/License-Apache_2.0-blue)

**HEI Boot** 是一套面向中后台场景的 Spring Boot 工程化脚手架：单个应用同时提供 **Admin** 与 **Portal** 双端 API，统一认证、权限、运维与消息能力，并与 [hei-gin](https://github.com/jiangbyte/hei-gin)、[hei-fastapi](https://github.com/jiangbyte/hei-fastapi) 等姊妹后端保持契约一致。

> 协议：[Apache License 2.0](LICENSE)

## 目录

- [功能特性](#功能特性)
- [前端姊妹项目](#前端姊妹项目)
- [技术栈](#技术栈)
- [工程结构](#工程结构)
- [快速开始](#快速开始)
- [默认账号](#默认账号)
- [姊妹项目](#姊妹项目)
- [License](#license)

## 功能特性

API 前缀统一为 `/api/v1/admin/*` 与 `/api/v1/portal/*`，常见中后台能力按模块划分如下：

| 模块 | 说明 |
| --- | --- |
| 双端账号体系 | ADMIN / PORTAL 独立会话（Sa-Token）；密码 RSA 传输、验证码登录、失败锁定与限流；JustAuth 三方登录（可配置） |
| RBAC 权限 | 账号 / 角色 / 部门 / 用户组 / 岗位；菜单、按钮与 API 资源授权；在线会话踢出 |
| 系统管理 | 字典、动态配置（敏感项加密存储）、Banner、公告 / 通知、意见反馈、弱口令库 |
| 对象存储 | S3 兼容存储（MinIO / RustFS / 阿里云 OSS 等），直链或预签名访问 |
| 运维能力 | 操作审计与告警、登录日志、运营工作台概览、内置任务调度（`sys_job`） |
| 代码生成 | 单表 / 树表 / 主子表方案，预览与 ZIP 下载（含菜单权限 SQL） |
| 实名认证 | 工单提交与审核、敏感字段脱敏审计、可扩展第三方 Provider |
| 业务扩展 | `module/biz` 示例模块，可按同样模式横向扩展 |

## 前端姊妹项目

| 项目 | 说明 |
| --- | --- |
| [**hei-admin**](https://github.com/jiangbyte/hei-admin) | Vue 3 管理端，对接 `/api/v1/admin/*` |
| [**hei-portal**](https://github.com/jiangbyte/hei-portal) | React 门户，对接 `/api/v1/portal/*` |
| [**hei-admin-uniapp**](https://github.com/jiangbyte/hei-admin-uniapp) | uni-app 管理端移动端 |

## 技术栈

| 层级 | 技术 |
| --- | --- |
| 后端 | JDK 21 · Spring Boot 4.1 · Maven 多模块 · 虚拟线程 |
| 持久化 | PostgreSQL / MySQL · MyBatis-Plus · Dynamic Datasource |
| 缓存 / 会话 | Redis · Redisson · Sa-Token |
| 文档 | Knife4j / SpringDoc |
| 其他 | JustAuth · AWS SDK v2（S3）· Hutool · MapStruct |

## 工程结构

```text
hei-boot/
├── app/admin/              # 可启动应用（Admin + Portal API）
├── common/                 # 公共能力（web / security / mybatis / redis / oss / job / log …）
├── module-api/             # 模块对外 API 契约
├── module/                 # 业务实现（auth / iam / sys / profile / workspace / biz）
└── scripts/                # 数据库脚本、Docker Compose
```

`scripts/` 目录：

| 文件 | 用途 |
| --- | --- |
| `hei_boot.sql` | MySQL 全量建表、种子数据与表/列 `COMMENT` |
| `docker/docker-compose.yml` | 全栈 Compose（中间件 + 应用，**无 build**） |
| `docker/.env.example` | 部署环境变量模板（复制为 `.env`，**勿提交**） |

## 快速开始

### 环境要求

- JDK **21**、Maven **3.9+**
- MySQL 8+、Redis

### 1. 初始化数据库

**维护原则：** 在 `scripts/hei_boot.sql`（MySQL 8）直接维护表结构、种子数据与表/列 `COMMENT`。

**MySQL 8+（本地运行默认）：**

```bash
mysql -u root -p -e "CREATE DATABASE hei_boot DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mysql -u root -p hei_boot < scripts/hei_boot.sql
```

Compose 首次启动 MySQL 时会自动执行初始化脚本（挂载至 `docker-entrypoint-initdb.d`）。已有数据卷需重建库时可：

```bash
docker compose -f scripts/docker/docker-compose.yml down -v   # 清空卷后重导
docker compose -f scripts/docker/docker-compose.yml up -d
```

Docker Compose（**镜像需预先构建**，Compose 不包含 `build`；密钥通过 `.env` 注入）：

```bash
cd scripts/docker
cp .env.example .env
# 编辑 .env 填写密钥

# 1. 构建并推送镜像（自行 mvn package + docker build / push）
cd ../..   # hei monorepo 根目录
mvn -pl app/admin -am -DskipTests -P'!with-biz' package -f hei-boot/pom.xml
docker build -f hei-boot/app/admin/Dockerfile -t registry.cn-beijing.aliyuncs.com/czbyte/hei-boot:latest hei-boot
docker build -t registry.cn-beijing.aliyuncs.com/czbyte/hei-admin:latest hei-admin
docker build -t registry.cn-beijing.aliyuncs.com/czbyte/hei-portal:latest hei-portal

cd hei-boot/scripts/docker
cp .env.example .env   # 编辑密钥
docker compose pull
docker compose up -d
```

访问：Admin `http://127.0.0.1:5173`、Portal `http://127.0.0.1:5174`、API `http://127.0.0.1:8000`。

`HEI_CONFIG_CRYPTO_KEY` 须与 `scripts/hei_boot.sql` 中 `sys_config` 密文匹配（演示种子与本地 `application-dev.yml` 默认密钥一致）；生产环境请生成新 Fernet 密钥并在管理端重配对象存储等敏感项。

默认端口：API `8000`、Admin `5173`、Portal `5174`、MySQL `3306`、Redis `6379`、MinIO `9000`/`9001`。对象存储在管理端配置 MinIO（容器内 `http://minio:9000`）。

开发配置见 [`app/admin/src/main/resources/application-dev.yml`](app/admin/src/main/resources/application-dev.yml)（`DB_WRITE_*` / Redis / 对象存储）。本地 `dev`/`local` profile 下读写库均指向同一 MySQL（`hei_boot`），只需配置 `DB_WRITE_*`；生产环境可通过 `DB_READ_*` 单独指定只读从库。

审计日志默认保留策略（可通过 `hei.log.audit.*` 或内置任务 `审计日志清理` 参数覆盖，设为 `0` 表示不清理）：

| 类型 | 默认保留 | 配置项 |
| --- | --- | --- |
| 登录/登出 | 180 天 | `hei.log.audit.login-retention-days` |
| 操作审计 | 365 天 | `hei.log.audit.operation-retention-days` |

> 本地 / 演示环境以 SQL 脚本全量初始化；Flyway 默认关闭。`scripts/hei_boot.sql` 已包含全部表/列中文注释。

### 2. 启动后端

```bash
mvn -pl app/admin -am spring-boot:run
```

| 项 | 地址 |
| --- | --- |
| API | http://127.0.0.1:8000 |
| 接口文档（Knife4j） | http://127.0.0.1:8000/doc.html |
| OpenAPI JSON | http://127.0.0.1:8000/v3/api-docs |

> 文档栈：**Spring Boot 4** + **Knife4j 5**（`knife4j-openapi3-boot4-spring-boot-starter`）+ springdoc 生成 OpenAPI。

### 3. 启动前端（可选）

前端为独立仓库，默认将 `/api` 代理到本后端 `http://127.0.0.1:8000`：

```bash
# 管理端 → http://127.0.0.1:5173
git clone https://github.com/jiangbyte/hei-admin.git && cd hei-admin
pnpm install && pnpm dev

# 门户 → http://127.0.0.1:5174
git clone https://github.com/jiangbyte/hei-portal.git && cd hei-portal
pnpm install && pnpm dev
```

详见 [hei-admin](https://github.com/jiangbyte/hei-admin) / [hei-portal](https://github.com/jiangbyte/hei-portal) 各仓库 README。

## 默认账号

| 端 | 前端仓库 | 地址 | 账号 | 密码 | 说明 |
| --- | --- | --- | --- | --- | --- |
| Admin | [hei-admin](https://github.com/jiangbyte/hei-admin) | http://127.0.0.1:5173 | `superadmin` | `123456` | 超级管理员（`*:*:*`） |

> 仅供本地演示。部署后请修改默认密码，并更换配置加密密钥、对象存储凭证等敏感项。更多演示账号与内容种子已写入 `scripts/hei_boot.sql`。

## 姊妹项目

| 项目 | 说明 | 协议 |
| --- | --- | --- |
| [**hei-boot**](https://github.com/jiangbyte/hei-boot) | Spring Boot 脚手架（本仓库） | Apache License 2.0 |
| [**hei-admin**](https://github.com/jiangbyte/hei-admin) | Vue 3 管理端前端 | Apache License 2.0 |
| [**hei-portal**](https://github.com/jiangbyte/hei-portal) | React 门户前端 | Apache License 2.0 |
| [**hei-admin-uniapp**](https://github.com/jiangbyte/hei-admin-uniapp) | uni-app 管理端移动端 | Apache License 2.0 |
| [**hei-gin**](https://github.com/jiangbyte/hei-gin) | Go / Gin 后端 | Apache License 2.0 |
| [**hei-fastapi**](https://github.com/jiangbyte/hei-fastapi) | FastAPI 后端 | Apache License 2.0 |

## License

本项目基于 [Apache License 2.0](LICENSE) 开源。完整条款见 [LICENSE](LICENSE)，版权声明见 [NOTICE](NOTICE).
