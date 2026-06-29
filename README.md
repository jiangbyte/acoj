# ACOJ

ACOJ 是一个面向在线编程评测场景的全栈项目。当前仓库主要完成了 FastAPI 后端平台底座、管理端前端工程、门户端前端工程骨架、认证鉴权、IAM/RBAC、字典、Banner、文件存储、任务和测试基础。OJ 题库、提交、评测、比赛等核心业务仍处于后续扩展阶段。

## 当前状态

- 后端基于 FastAPI、SQLAlchemy Async、Alembic、Pydantic v2 构建。
- API 统一暴露在 `/api/v1` 下，管理端为 `/api/v1/admin/*`，门户端为 `/api/v1/portal/*`。
- 管理端 API 已接入认证、账号、部门、用户组、角色、资源、岗位、授权、权限注册、文件、用户资料、Banner、字典等模块。
- 门户端 API 已接入认证、门户用户资料、Banner 展示等模块。
- 前端包含 `web/admin` 和 `web/portal` 两个 Vite 应用目录，二者当前各自独立安装依赖、启动和构建。
- `web/admin` 是当前主要管理端工程。
- `web/portal` 按门户端工程保留独立目录、端口和 Dockerfile；当前代码仍是前端基础骨架，尚未真正构建门户业务页面。
- OJ 相关目录已经开始存在模型骨架，但题库、提交、评测、比赛等完整业务 API 和前端页面尚未完成。
- 已提供后端、管理端、门户端 Dockerfile；后端 `.env` 不进入镜像，通过容器运行时环境变量注入。

## 技术栈

后端：

- Python 3.11+
- FastAPI
- SQLAlchemy Async
- Alembic
- Pydantic v2 / pydantic-settings
- Redis，可选
- RabbitMQ + Celery，可选 worker
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
pip install -e .[dev,postgres]
cp .env.example .env
alembic upgrade head
python scripts/dev.py
```

默认后端地址为 `http://127.0.0.1:8000`，默认 API 前缀为 `/api/v1`。也可以直接使用 uvicorn：

```bash
uvicorn app.main:app --reload
```

### 管理端 admin

```bash
cd web/admin
pnpm install
pnpm dev
```

管理端默认端口来自 `web/admin/.env`：

```env
VITE_PORT=5173
VITE_API_URL="http://127.0.0.1:8000"
```

### 门户端 portal

```bash
cd web/portal
pnpm install
pnpm dev
```

门户端默认端口来自 `web/portal/.env`：

```env
VITE_PORT=5183
VITE_API_URL="http://127.0.0.1:8000"
```

说明：`portal` 目前按独立门户端项目维护，但页面和业务还没有真正按门户场景构建完成。

## Docker

### 后端镜像

```bash
docker build -t acoj-backend .
```

运行时通过环境变量或 `.env` 注入配置：

```bash
docker run --env-file .env -p 8000:8000 acoj-backend
```

也可以单独传入变量：

```bash
docker run -e DB__URL="postgresql+asyncpg://postgres:postgres@host.docker.internal:5432/acoj" -p 8000:8000 acoj-backend
```

后端 Dockerfile 默认设置：

- `APP__HOST=0.0.0.0`
- `APP__PORT=8000`
- 安装 `.[postgres]`
- 不复制 `.env` 到镜像
- 不自动执行 Alembic 迁移

### 管理端镜像

```bash
docker build -t acoj-admin web/admin
docker run -p 8081:80 acoj-admin
```

### 门户端镜像

```bash
docker build -t acoj-portal web/portal
docker run -p 8082:80 acoj-portal
```

前端镜像使用 Node 构建静态资源，再由 nginx 托管 `dist/`，并内置 SPA fallback。Vite 的 `VITE_*` 配置会在构建时写入前端产物；如需修改 API 地址，应在构建镜像前调整对应前端目录下的 `.env`。

## 运行依赖

本地轻量开发至少需要 PostgreSQL。其他依赖可以按需关闭或使用回退配置。

建议完整环境：

- PostgreSQL
- Redis，用于会话存储
- RabbitMQ，用于 Celery broker
- MinIO 或其他 S3 兼容对象存储
- Prometheus / OpenTelemetry Collector，可选

## 配置

复制 `.env.example` 后按本地环境调整：

```bash
cp .env.example .env
```

轻量本地开发推荐配置：

```env
REDIS__ENABLED=false
AUTH__ENABLE_MEMORY_SESSION_FALLBACK=true
STORAGE__PROVIDER=local
OBSERVABILITY__ENABLED=false
```

完整依赖环境示例：

```env
REDIS__ENABLED=true
STORAGE__PROVIDER=s3
OBSERVABILITY__ENABLED=true
OBSERVABILITY__LOG_JSON=true
OBSERVABILITY__METRICS_ENABLED=true
OBSERVABILITY__TRACING_ENABLED=true
OBSERVABILITY__OTLP_ENABLED=true
OBSERVABILITY__OTLP_ENDPOINT=http://127.0.0.1:4318
OBSERVABILITY__DB_OBSERVABILITY_ENABLED=true
OBSERVABILITY__HTTP_CLIENT_OBSERVABILITY_ENABLED=true
OBSERVABILITY__CELERY_OBSERVABILITY_ENABLED=true
```

常用配置项：

- `APP__HOST` / `APP__PORT`：后端监听地址和端口。
- `DB__URL`：数据库连接地址，默认使用 PostgreSQL asyncpg。
- `REDIS__ENABLED` / `REDIS__URL`：是否启用 Redis 以及 Redis 连接地址。
- `AUTH__ENABLE_MEMORY_SESSION_FALLBACK`：Redis 关闭时是否允许内存会话回退。
- `STORAGE__PROVIDER`：文件存储提供方，可选 `s3` 或 `local`。
- `SWAGGER__ENABLED`：是否开启接口文档。
- `OBSERVABILITY__ENABLED`：是否启用可观测性总开关。

## 常用命令

后端：

```bash
python scripts/test.py
python scripts/lint.py
python scripts/migrate.py
python scripts/makemigration.py "message"
python scripts/check_migration.py
```

数据库迁移只管理结构，不写入业务种子数据。新增或修改表结构时，先修改 SQLAlchemy model，再执行自动生成：

```bash
python scripts/makemigration.py "add xxx table"
python scripts/check_migration.py
python scripts/migrate.py
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
scripts/        开发、测试、迁移辅助脚本
tests/          单元测试和接口测试
web/
  admin/        管理端 Vue 应用
  portal/       门户端 Vue 应用骨架
```

## API 组织

项目采用“路径版本 + 目录版本”的 API 组织方式：

- 运行时路径版本：`/api/v1/*`
- 代码目录版本：`app/api/v1`
- 管理端聚合入口：`app/api/v1/admin.py`
- 门户端聚合入口：`app/api/v1/portal.py`

业务模块路由保持不带版本号，版本差异只在 API 装配层处理。后续新增不兼容版本时，应新增 `app/api/v2` 并注册到 `/api/v2`。

## 当前模块

- `auth`：管理端和门户端认证。
- `iam`：账号、部门、用户组、角色、资源、岗位、授权、权限注册等管理端权限能力。
- `user/admin`：管理端用户资料。
- `user/portal`：门户端用户资料。
- `dict`：系统字典。
- `banner`：管理端 Banner 管理和门户端 Banner 展示。
- `file`：文件上传和存储。
- `oj`：OJ 领域模型骨架，完整业务流程仍待实现。

## OJ 业务路线图

后续可以在现有平台底座上扩展 OJ 核心能力：

- 题库管理：题目、标签、难度、题面、样例、测试用例。
- 提交记录：代码提交、语言配置、状态流转、运行结果。
- 评测执行：异步任务分发、编译运行、沙箱隔离、资源限制、结果回写。
- 比赛系统：比赛、报名、榜单、封榜、赛后重测。
- 训练与题单：题单、课程、进度、收藏。
- 题解与讨论：题解发布、评论、审核。

## 设计约束

- 主键统一使用 Snowflake 字符串 ID。
- 不使用数据库级外键，关联关系通过业务 ID 和服务层逻辑维护。
- 创建人、更新人等审计字段使用显式字段存储。
- 服务层、仓储层优先使用命令对象或结构化 DTO 传参。
- 时间输入输出采用 ISO 8601，响应序列化统一输出 UTC `Z` 后缀。
- 可观测性默认关闭，按环境按需启用。

## 测试

后端测试入口：

```bash
python scripts/test.py
```

当前测试覆盖认证、权限、分页、字典、Banner、文件、时间格式、健康检查和可观测性等平台能力。OJ 核心业务模块落地后，应补充题库、提交、评测队列和比赛流程相关测试。
