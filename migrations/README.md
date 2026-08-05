# 数据库迁移

本目录保存 Alembic 迁移文件。迁移只管理数据库结构，不写入业务种子数据。

字典、系统配置、存储配置等初始化数据见 `scripts/sql/`，可用：

```bash
python scripts/db/load_bootstrap_sql.py
```

详细说明见 [docs/migration.md](../docs/migration.md)。
