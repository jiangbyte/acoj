# 数据库迁移说明

本项目使用 Alembic 管理数据库结构迁移。迁移只负责表、字段、索引、约束等结构变更，不负责初始化业务数据。

## 执行迁移

本地或部署时执行：

```bash
./scripts/migrate.sh
```

等价命令：

```bash
alembic upgrade head
```

迁移使用后端配置中的 `DB__URL`，当前项目按 PostgreSQL 生成和验证迁移，不使用 SQLite 生成迁移。

配置优先级：

```text
真实环境变量 > .env.local > .env > settings.py 默认值
```

本地开发建议把个人数据库连接写在 `.env.local`，避免修改公共 `.env`。

## 自动生成结构迁移

修改数据库结构时，先改 SQLAlchemy model，再生成迁移：

```bash
./scripts/makemigration.sh "describe schema change"
```

生成后必须检查 `migrations/versions/*.py`，确认只包含结构操作，例如：

- `op.create_table`
- `op.add_column`
- `op.alter_column`
- `op.create_index`
- `op.create_unique_constraint`
- `op.drop_table`
- `op.drop_column`
- `op.drop_index`

禁止在 migration 里写业务数据操作：

- `op.bulk_insert`
- `op.execute` 写入业务数据
- `insert/update/delete` seed 或修复业务数据
- 默认管理员、角色、字典、Banner 等初始化数据

检查无误后执行：

```bash
./scripts/migrate.sh
./scripts/check_migration.sh
```

`check_migration.sh` 用于确认当前数据库结构和 SQLAlchemy model 没有未生成的结构差异。

## 新开发库初始化

如果是空库，直接执行：

```bash
./scripts/migrate.sh
```

如果需要重建本地开发库，先删除并重新创建 PostgreSQL 数据库，再执行：

```bash
./scripts/migrate.sh
```

示例：

```bash
dropdb hei_fastapi
createdb hei_fastapi
./scripts/migrate.sh
```

如果本地没有 `dropdb/createdb` 命令，也可以用数据库管理工具删除并重建 `DB__URL` 指向的数据库。

## 常见问题

`Target database is not up to date`：

当前数据库没有升级到最新 migration。先执行：

```bash
./scripts/migrate.sh
```

然后再生成新的迁移。

生成的 migration 为空：

说明当前 model 和数据库结构没有检测到差异。可以运行：

```bash
./scripts/check_migration.sh
```

确认是否已经一致。

需要初始化数据：

不要写进 Alembic migration。后续应放到独立 seed/dev 脚本、后台管理功能或部署初始化流程中。
