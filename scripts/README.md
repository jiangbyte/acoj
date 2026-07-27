# Scripts

脚本按用途分目录，避免根目录平铺。

| 目录 | 用途 |
| --- | --- |
| `db/` | Alembic 迁移执行、生成、结构检查 |
| `seed/` | 初始化或补齐必要业务数据 |
| `ops/` | 运维、压测、验收辅助脚本 |
| `sql/` | 手工 SQL、备份 SQL、历史初始化 SQL |
| `codegen/ddl_tests/` | 代码生成器的 DDL 测试样例 |

常用命令：

```bash
python scripts/db/migrate.py
python scripts/db/makemigration.py "describe schema change"
python scripts/db/check_migration.py
python scripts/seed/seed_super_admin.py
python scripts/ops/loadtest_http.py --base-url http://127.0.0.1:8000 --path / --requests 1000 --concurrency 50
```
