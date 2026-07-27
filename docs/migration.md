# 数据库迁移

本项目使用 Alembic 管理数据库结构迁移。迁移只负责表、字段、索引、约束等结构变更；超管账号、初始角色等业务数据由 `scripts/seed/seed_super_admin.py` 处理。

## 配置来源

迁移读取后端配置中的 `DB__URL`：

```text
真实环境变量 > .env / .env.local > settings.py 默认值
```

本地开发建议把个人数据库连接写在 `.env.local`，避免修改公共 `.env`。当前项目按 PostgreSQL 生成和验证迁移。

## 常用命令

执行迁移：

```bash
python scripts/db/migrate.py
```

生成结构迁移：

```bash
python scripts/db/makemigration.py "describe schema change"
```

检查当前数据库结构是否和 SQLAlchemy model 一致：

```bash
python scripts/db/check_migration.py
```

初始化超管：

```bash
python scripts/seed/seed_super_admin.py
python scripts/seed/seed_super_admin.py --help
```

## Docker

单机单 Docker：

```bash
docker compose run --rm hei migrate
docker compose up -d --build
```

单机多 Docker 多实例：

```bash
docker compose -f docker-compose.multi.yml up -d --build --scale api=2 --scale worker=2
docker compose -f docker-compose.multi.yml --profile seed run --rm seed
```

多机多节点：

```bash
docker compose -f docker-compose.distributed.yml config | docker stack deploy -c - hei-fastapi
```

后端镜像会复制 `scripts/db` 和 `scripts/seed`，容器入口统一通过 `entrypoint.sh` 参数切换：

```bash
docker run --rm --env-file .env hei-fastapi-backend migrate
docker run --rm --env-file .env hei-fastapi-backend seed
```

## 迁移约束

生成迁移后必须人工检查 `migrations/versions/*.py`，确认只包含结构操作，例如 `op.create_table`、`op.add_column`、`op.alter_column`、`op.create_index`、`op.drop_table`。

不要在 migration 里写业务数据操作，例如 `op.bulk_insert`、业务 `insert/update/delete`、默认管理员、角色、字典、Banner 等初始化数据。需要初始化数据时使用独立 seed 脚本。

如果清空 `migrations/versions` 并重建初始迁移，必须基于空库生成并验证；已有开发库会让 Alembic 误判为当前结构，生成结果不可靠。旧库的 `alembic_version` 会指向已删除 revision，最干净的处理方式是重建数据库后执行新基线迁移。

参见 [migrations/README.md](../migrations/README.md) 获取目录级简要说明。
