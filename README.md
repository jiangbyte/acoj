# ACOJ

ACOJ 是一个面向在线编程评测场景的全栈项目。当前仓库已经完成后端平台底座、管理端/门户端前端工程、认证鉴权、权限管理、字典、Banner、文件存储、任务和测试基础；题库、提交、比赛等 OJ 核心业务模块仍处于后续扩展阶段。

## 当前能力

- 后端基于 FastAPI、SQLAlchemy Async、Alembic、Pydantic 构建。
- 前端基于 pnpm workspace，包含管理端 `admin`、门户端 `portal` 和共享包 `@hei/shared`。
- 支持管理端/门户端认证入口、随机字符串 Token、Redis 会话和内存会话回退。
- 内置 IAM/RBAC、用户资料、数据字典、Banner、文件上传等通用业务模块。
- 支持 S3/MinIO 对象存储，也可切换到本地文件存储。
- 提供 RabbitMQ + Celery 异步任务骨架。
- 可选启用 JSON 日志、Prometheus metrics、OpenTelemetry tracing。
- 使用 Snowflake 字符串 ID，不依赖数据库自增主键。
- 数据模型不建立数据库级外键，业务关联由代码显式维护。
- API 统一暴露在 `/api/v1`，管理端为 `/api/v1/admin/*`，门户端为 `/api/v1/portal/*`。

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

- pnpm workspace
- Vue 3
- Vite
- TypeScript
- Ant Design Vue
- Pinia
- UnoCSS
- axios、dayjs

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

默认后端地址为 `http://127.0.0.1:8000`，默认 API 前缀为 `/api/v1`。如果开启 Swagger，可访问 FastAPI 默认文档入口。

也可以直接使用 uvicorn：

```bash
uvicorn app.main:app --reload
```

### 前端

```bash
cd web
pnpm install
pnpm dev:admin
```

另开一个终端启动门户端：

```bash
cd web
pnpm dev:portal
```

## 运行依赖

本地轻量开发只需要 PostgreSQL，其他依赖可以按需关闭或使用回退配置。

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
- `APP__API_PREFIX`：API 前缀，默认 `/api/v1`。
- `DB__URL`：数据库连接地址，默认使用 PostgreSQL asyncpg。
- `REDIS__ENABLED`：是否启用 Redis。
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
```

数据库迁移只管理结构，不写入业务种子数据。新增或修改表结构时，先修改
SQLAlchemy model，再执行自动生成：

```bash
python scripts/makemigration.py "add xxx table"
python scripts/check_migration.py
python scripts/migrate.py
```

生成的 `migrations/versions/*.py` 应只包含 `create_table`、`add_column`、
`create_index` 等结构变更，不应包含业务 `insert/update/delete`。本地开发库如
需重建，删除并重新创建 PostgreSQL 数据库后执行 `python scripts/migrate.py`。

前端：

```bash
cd web
pnpm lint
pnpm build
pnpm build:admin
pnpm build:portal
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
  portal/       门户端 Vue 应用
  packages/     前端共享包
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
- `iam`：角色、权限、组织、用户组等权限管理基础。
- `user/admin`：管理端用户资料。
- `user/portal`：门户端用户资料。
- `dict`：系统字典。
- `banner`：Banner 管理和门户端展示。
- `file`：文件上传和存储。

## OJ 业务路线图

后续可以在现有平台底座上扩展 OJ 核心能力：

- 题库管理：题目、标签、难度、题面、样例、测试用例。
- 提交记录：代码提交、语言配置、状态流转、运行结果。
- 评测执行：异步任务分发、编译运行、沙箱隔离、资源限制、结果回写。
- 比赛系统：比赛、报名、榜单、封榜、赛后重测。
- 训练与题单：题单、课程、进度、收藏。
- 题解与讨论：题解发布、评论、审核。

这些模块当前尚未在仓库中实现，README 仅作为后续演进方向记录。

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
