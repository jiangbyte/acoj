# HEI FastAPI

HEI FastAPI 是一个通用的快速开发脚手架，面向中后台、门户、权限系统、消息系统和常规业务 API 的快速落地。仓库包含 FastAPI 后端、管理端 `web/admin`、门户端 `web/portal`，并内置账号体系、RBAC、资源菜单、文件存储、消息通知、定时任务、数据库迁移和基础可观测能力。

## 特性

- 后端基于 FastAPI、SQLAlchemy Async、Alembic、Pydantic v2、Redis、Celery 构建。
- API 统一挂载在 `/api/v1` 下，按 admin、portal、internal 三类入口组织。
- 管理端和门户端使用独立账号类型、路由前缀和前端工程。
- 内置 IAM/RBAC：账号、角色、部门、用户组、岗位、资源、权限注册与授权。
- 内置系统能力：字典、Banner、文件上传、S3/MinIO 或本地存储。
- 内置消息能力：站内消息、通知、待办、实时事件。
- 内置后台任务、定时任务、结构化日志、Prometheus 指标和 OpenTelemetry tracing 接入点。
- 数据库迁移只管理结构，初始化超管等业务数据由 seed 脚本处理。

## 技术栈

后端：

- Python 3.11+
- FastAPI
- SQLAlchemy Async
- Alembic
- Pydantic v2 / pydantic-settings
- Redis
- RabbitMQ + Celery
- S3/MinIO 或本地文件存储
- pytest、ruff、mypy

前端：

- pnpm
- Vue 3
- Vite
- TypeScript
- Naive UI / Pro Naive UI
- Pinia
- UnoCSS
- axios、vue-i18n、vue-router

## 快速启动

### 后端

```bash
python -m venv .venv
source .venv/bin/activate
pip install -e ".[dev,postgres]"
cp .env.example .env
python scripts/migrate.py
python scripts/seed_super_admin.py
python scripts/dev.py
```

默认后端地址为 `http://127.0.0.1:8000`。也可以直接启动：

```bash
uvicorn app.main:app --reload
```

后端本地开发至少需要 PostgreSQL、Redis 和 RabbitMQ。FastAPI 启动时会自动拉起 Celery worker 和 beat。S3/MinIO、可观测性按实际功能启用。

安装后也可以使用命令入口：

```bash
hei-fastapi
```

### 管理端

```bash
cd web/admin
pnpm install
pnpm dev
```

默认配置：

```env
VITE_PORT=5173
VITE_HOME_PATH="/dashboard"
VITE_API_URL="http://127.0.0.1:8000"
```

### 门户端

```bash
cd web/portal
pnpm install
pnpm dev
```

默认配置：

```env
VITE_PORT=5163
VITE_HOME_PATH="/home"
VITE_API_URL="http://127.0.0.1:8000"
```

## 常用命令

后端：

```bash
python scripts/dev.py
python scripts/test.py
python scripts/lint.py
python scripts/migrate.py
python scripts/makemigration.py "describe schema change"
python scripts/check_migration.py
python scripts/seed_super_admin.py
```

管理端：

```bash
cd web/admin
pnpm lint
pnpm build
pnpm preview
```

门户端：

```bash
cd web/portal
pnpm lint
pnpm build
pnpm preview
```

## 配置

后端配置使用 pydantic-settings，支持嵌套环境变量，分隔符为 `__`。配置加载顺序为真实环境变量优先，其次读取项目根目录 `.env` 和 `.env.local`。

常用配置项：

- `APP__HOST` / `APP__PORT`：后端监听地址和端口。
- `DB__URL`：数据库连接地址，默认使用 PostgreSQL asyncpg。
- `REDIS__URL`：Redis 连接地址，用于会话、权限注册表和授权缓存。
- `AUTH__TOKEN_NAME`：认证请求头名称，默认 `Authorization`。
- `CORS__ALLOW_ORIGINS`：允许访问后端的前端源。
- `CELERY__BROKER_URL`：RabbitMQ broker 地址。
- `STORAGE__PROVIDER`：文件存储方式，可选 `local`、`minio`、`s3`、`oss`。
- `STORAGE__PUBLIC_PATH`：本地文件公开访问前缀，默认 `/api/v1/files`。
- `SWAGGER__ENABLED`：是否开启接口文档。
- `OBSERVABILITY__ENABLED`：可观测性总开关。

本地轻量开发推荐：

```env
REDIS__URL=redis://127.0.0.1:6379/0
STORAGE__PROVIDER=local
STORAGE__PUBLIC_PATH=/api/v1/files
STORAGE__LOCAL_ROOT=storage
OBSERVABILITY__ENABLED=false
```

MinIO/S3/OSS 可通过 `STORAGE__BASE_URL` 配置公开 CDN/桶域名；为空时后端会返回 `/api/v1/files/...` 或签名访问地址，前端不应直接使用服务器本地文件路径。

前端生产构建使用各自目录下的 `.env.production`。当前 `VITE_API_URL=""`，表示生产环境走同源 `/api/`，由 nginx 反向代理到后端。

## Docker

### 后端镜像

```bash
docker build -t hei-fastapi-backend .
docker run --env-file .env -v hei-fastapi-storage:/app/storage -p 8000:8000 hei-fastapi-backend
```

后端 Dockerfile：

- 使用 Python 3.11 slim。
- 安装 `.[postgres]`。
- 暴露 `8000`。
- 不复制 `.env` 到镜像。
- 容器未显式传存储配置时默认使用 `local`，文件写入 `/app/storage`。
- 使用本地存储时应挂载 `/app/storage` volume，避免容器重建后上传文件丢失。
- 不自动执行 Alembic 迁移。

### 管理端镜像

```bash
docker build -t hei-fastapi-admin web/admin
docker run -e BACKEND_URL="http://host.docker.internal:8000" -p 8081:81 hei-fastapi-admin
```

管理端生产容器内 nginx 监听 `81`。

### 门户端镜像

```bash
docker build -t hei-fastapi-portal web/portal
docker run -e BACKEND_URL="http://host.docker.internal:8000" -p 8082:80 hei-fastapi-portal
```

门户端生产容器内 nginx 监听 `80`。

两个前端镜像都使用 Node 构建 `dist/`，再用 nginx 托管静态资源；nginx 模板会在容器启动时用 `${BACKEND_URL}` 渲染 `/api/` 反向代理地址。

## 项目结构

```text
app/
  api/          API 版本装配入口
  core/         配置、安全、日志、异常、统一响应
  deps/         FastAPI 依赖注入
  middleware/   中间件
  modules/      业务模块
  platform/     DB、Redis、HTTP、Celery、存储等基础设施
  worker/       Celery worker 入口与任务
migrations/     Alembic 数据库迁移
scripts/        开发、测试、迁移和 seed 辅助脚本
tests/          单元测试和接口测试
web/
  admin/        管理端 Vue 应用
  portal/       门户端 Vue 应用
```

## API 组织

API 运行时路径仍按版本暴露，例如 `/api/v1/*`。版本、前缀、路由、模型和任务由模块自己的
`module.py` 声明，应用启动、Alembic 和 Celery 会自动读取这些声明。

模块声明示例：

```python
from app.platform.module import ModuleSpec, RouteSpec

module = ModuleSpec(
    name="example",
    routes=(
        RouteSpec(
            version="v1",
            prefix="/admin",
            tags=("admin",),
            router="app.modules.example.router:router",
        ),
    ),
    models=("app.modules.example.model",),
    startup_hooks=("app.modules.example.lifecycle:startup",),
    shutdown_hooks=("app.modules.example.lifecycle:shutdown",),
)
```

新增 `v2` 接口时，在模块内追加新的 `RouteSpec(version="v2", ...)`，不需要修改全局 API 聚合文件。

MQ 默认不启动消费者，需要配置 `MQ__ENABLED=true`。`MQ__URL` 为空时复用 `CELERY__BROKER_URL`。
模块内可通过 `app.platform.mq.event_producer` 发布消息；如需消费消息，在模块自己的
`startup_hooks/shutdown_hooks` 中使用 `create_blocking_connection` 或 `MQConsumerWorker`
自行声明 exchange、queue、binding、ack、重试等策略。

## 内置模块

- `auth`：管理端/门户端登录、注册、退出、注销。
- `iam`：账号、角色、部门、用户组、岗位、资源、权限注册与授权。
- `user`：admin 用户资料、portal 用户资料、用户中心、公开空间。
- `sys`：字典、Banner、文件。
- `message`：通知、站内消息、待办、实时事件。

## 扩展业务模块

新增业务建议放在 `app/modules/<module_name>` 下，并按现有模块组织：

```text
app/modules/example/
  __init__.py
  module.py
  model.py
  schema.py
  repository.py
  service.py
  router.py
```

典型流程：

1. 在 `model.py` 定义 SQLAlchemy 模型。
2. 在 `schema.py` 定义请求、响应和内部传输对象。
3. 在 `repository.py` 封装数据访问。
4. 在 `service.py` 编排业务逻辑和事务。
5. 在 `router.py` 暴露 FastAPI 路由。
6. 在 `module.py` 声明路由版本、前缀和模型。
7. 如包含 Celery 任务、MQ consumer 或生命周期钩子，也在 `module.py` 中声明。
8. 如包含新表，执行迁移生成命令。

## 数据库迁移

迁移只负责结构变更，不写入业务种子数据。详细说明见 [migrations/README.md](migrations/README.md) 和 [docs/migration.md](docs/migration.md)。

常规流程：

```bash
python scripts/makemigration.py "add xxx table"
python scripts/check_migration.py
python scripts/migrate.py
```

早期开发阶段如需重建完整初始迁移：

```bash
python scripts/rebuild_initial_migration.py --yes
```

初始化超管：

```bash
python scripts/seed_super_admin.py
```

默认账号和密码可通过环境变量或参数覆盖，详见脚本帮助：

```bash
python scripts/seed_super_admin.py --help
```
