# hei-fastapi

FastAPI 后端工程模板，提供常用的项目组织、认证、权限、存储、任务和测试基础：

- 全链路异步：FastAPI + SQLAlchemy Async + Redis Async
- 模块化单体目录
- 随机字符串 Token + Redis 会话
- 多用户体系：单用户主表 + 管理端/用户端各自扩展表
- RBAC、部门树、用户组、数据权限
- 所有数据模型禁止数据库级外键，关联关系统一在业务代码中显式维护
- 可选开启的可观测性：JSON 日志、Prometheus 指标、OpenTelemetry tracing
- RabbitMQ + Celery 异步任务骨架
- S3(MinIO) 默认文件存储，支持本地存储回退
- 雪花字符串 ID，不使用数据库自增主键
- 时间输入输出统一采用 ISO 8601，响应序列化统一输出 UTC `Z` 后缀
- Alembic 迁移、pytest 测试基础

## Quick Start

```bash
python -m venv .venv
source .venv/bin/activate
pip install -e .[dev,postgres]
cp .env.example .env
alembic upgrade head
uvicorn app.main:app --reload
```

默认数据库为 PostgreSQL 异步驱动 `asyncpg`。测试环境仍使用内存 SQLite，避免本地校验依赖外部数据库服务。

建议启动依赖：

- PostgreSQL
- RabbitMQ
- MinIO 或其他 S3 兼容对象存储
- Redis（可选，轻量部署可关闭）

## Infrastructure Defaults

- 消息队列：RabbitMQ 作为 Celery broker
- 文件存储：S3(MinIO) 默认实现，可切本地存储
- 主键：Snowflake 字符串 ID

## Configuration Profiles

项目没有单独的额外模式配置项。轻量部署与扩展依赖通过现有配置组合表达，避免配置项和运行时代码脱节。

轻量部署模式：

```env
REDIS__ENABLED=false
AUTH__ENABLE_MEMORY_SESSION_FALLBACK=true
STORAGE__PROVIDER=local
OBSERVABILITY__ENABLED=false
```

适用场景：

- 本地快速验证
- 单机开发环境
- 不希望额外依赖 Redis、Prometheus、OTel Collector、MinIO

扩展依赖模式：

```env
REDIS__ENABLED=true
STORAGE__PROVIDER=s3
OBSERVABILITY__ENABLED=true
OBSERVABILITY__METRICS_ENABLED=true
OBSERVABILITY__TRACING_ENABLED=true
OBSERVABILITY__OTLP_ENABLED=true
OBSERVABILITY__OTLP_ENDPOINT=http://127.0.0.1:4318
OBSERVABILITY__DB_OBSERVABILITY_ENABLED=true
OBSERVABILITY__HTTP_CLIENT_OBSERVABILITY_ENABLED=true
OBSERVABILITY__CELERY_OBSERVABILITY_ENABLED=true
```

适用场景：

- 多环境部署
- 需要统一会话存储
- 需要按需启用对象存储、指标、链路追踪或任务观测

## Observability

可观测性默认关闭，只保留基础日志和 `request_id`。

开启示例：

```env
OBSERVABILITY__ENABLED=true
OBSERVABILITY__LOG_JSON=true
OBSERVABILITY__METRICS_ENABLED=true
OBSERVABILITY__TRACING_ENABLED=true
OBSERVABILITY__OTLP_ENABLED=true
OBSERVABILITY__OTLP_ENDPOINT=http://127.0.0.1:4318
```

说明：

- `OBSERVABILITY__METRICS_ENABLED=true` 后应用暴露 `/metrics`
- `OBSERVABILITY__TRACING_ENABLED=true` 后启用本地 tracing
- `OBSERVABILITY__OTLP_ENABLED=true` 且配置 endpoint 后才导出到 collector
- `OBSERVABILITY__DB_OBSERVABILITY_ENABLED=true` 后为数据库访问接入 tracing
- `OBSERVABILITY__HTTP_CLIENT_OBSERVABILITY_ENABLED=true` 后采集出站 HTTP 指标与 tracing
- `OBSERVABILITY__CELERY_OBSERVABILITY_ENABLED=true` 后采集 Celery 任务指标
- 轻量部署可保持全部关闭，不依赖 Prometheus 或 OTel Collector

## Architecture Constraints

- 用户采用单主表 `sys_user` 区分不同用户体系
- 管理端扩展资料与门户端扩展资料分别落在 `user/admin`、`user/portal` 模块
- 所有关联关系只保留业务意义上的 ID 字段，不建立数据库外键
- 创建人、更新人等审计字段采用显式字段存储，不依赖 ORM relationship
- 服务层、仓储层优先使用命令对象或结构化 DTO 传参，减少平铺形参

## API Versioning

项目采用“路径版本 + 目录版本”维护 API：

- 运行时路径版本：当前统一暴露在 `/api/v1/*`
- 代码目录版本：`app/api/v1` 负责 `v1` 的路由聚合与装配
- 业务模块路由保持不带版本号，版本差异只在 API 装配层处理

后续新增版本时，直接新增 `app/api/v2` 并注册到 `/api/v2`，不要在 `v1` 目录内做破坏性变更。

## Layout

- `app/api`: API 版本装配入口
- `app/core`: 配置、安全、日志、异常、统一响应
- `app/platform`: DB/Redis/HTTP/Celery 等基础设施
- `app/modules`: 业务模块
- `migrations`: Alembic
- `tests`: 单元、集成、接口测试
