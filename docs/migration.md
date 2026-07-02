# 数据库迁移

## 常规生成

表结构变化后先修改 SQLAlchemy model，再生成迁移：

```bash
python scripts/makemigration.py "add message module"
python scripts/check_migration.py
python scripts/migrate.py
```

`scripts/migrate.py` 等价于：

```bash
alembic upgrade head
```

## 清空 versions 后重新生成完整迁移

如果要重建完整初始迁移，直接执行脚本：

```bash
python scripts/rebuild_initial_migration.py --yes
```

脚本会创建临时空库、清空 `migrations/versions/*.py`、生成 `initial schema`、执行迁移校验，最后删除临时库。默认临时库名是 `hei_fastapi_migration_shadow`，连接账号、密码、主机和端口来自当前 `DB__URL`。

常用参数：

```bash
python scripts/rebuild_initial_migration.py --yes -m "initial schema"
python scripts/rebuild_initial_migration.py --yes --shadow-db hei_fastapi_migration_tmp
python scripts/rebuild_initial_migration.py --yes --keep-db
```

如果当前开发库的 `alembic_version` 还指向已删除的旧 revision，不要直接在该库上 autogenerate。重建完整迁移后，开发库通常直接重建库再执行：

```bash
python scripts/migrate.py
```

如果必须保留现有库，需要先人工确认结构已经与新迁移一致，再执行 `alembic stamp head` 只同步版本号。
