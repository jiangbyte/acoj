# Scripts

| 目录 | 用途 |
| --- | --- |
| `db/` | Alembic 迁移、bootstrap SQL 导出 / 加载 |
| `sql/` | 字典 / 配置（脱敏）/ 存储配置等 bootstrap 数据 |
| `codegen/` | 代码生成器相关脚本与 DDL 测试样例 |

常用命令：

```bash
python scripts/db/migrate.py
python scripts/db/makemigration.py "describe schema change"
python scripts/db/check_migration.py
python scripts/db/export_bootstrap_sql.py
python scripts/db/load_bootstrap_sql.py
```

`scripts/sql/` 当前文件：

- `sys_dict.sql`
- `sys_config.sql`（敏感项已置空）
- `sys_storage_config.sql`（`access_key` / `secret_key` 已置空）
