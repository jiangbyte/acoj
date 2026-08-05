# 数据库迁移

本项目使用 Alembic 管理数据库结构迁移。迁移只负责表、字段、索引、约束等结构变更。

初始化数据（字典、系统配置、存储配置）放在 `scripts/sql/`，通过 `python scripts/db/load_bootstrap_sql.py` 导入。敏感配置值在导出时已脱敏。

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

导出 / 加载 bootstrap SQL：

```bash
python scripts/db/export_bootstrap_sql.py
python scripts/db/load_bootstrap_sql.py
```

容器入口：

```bash
./entrypoint.sh migrate
./entrypoint.sh seed   # 加载 scripts/sql bootstrap
```

## 迁移约束

生成迁移后必须人工检查 `migrations/versions/*.py`，确认只包含结构操作。

不要在 migration 里写业务数据操作。需要初始化数据时使用 `scripts/sql/`。

如果清空 `migrations/versions` 并重建初始迁移，必须基于空库生成并验证；已有开发库会让 Alembic 误判为当前结构。旧库的 `alembic_version` 会指向已删除 revision，最干净的处理方式是重建数据库后执行新基线迁移。

参见 [migrations/README.md](../migrations/README.md)。
