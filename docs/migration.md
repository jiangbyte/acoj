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

如果要重建完整初始迁移，先清空迁移版本文件：

```bash
rm -f migrations/versions/*.py
```

建议用临时空库生成完整迁移，避免当前开发库已有表导致只生成增量：

```bash
python - <<'PY'
import asyncio
import asyncpg

async def main():
    conn = await asyncpg.connect(
        user="postgres",
        password="123456",
        host="127.0.0.1",
        port=5432,
        database="postgres",
    )
    await conn.execute("DROP DATABASE IF EXISTS hei_fastapi_migration_shadow")
    await conn.execute("CREATE DATABASE hei_fastapi_migration_shadow")
    await conn.close()

asyncio.run(main())
PY

DB__URL=postgresql+asyncpg://postgres:123456@127.0.0.1:5432/hei_fastapi_migration_shadow \
  alembic revision --autogenerate -m "initial schema"

python - <<'PY'
import asyncio
import asyncpg

async def main():
    conn = await asyncpg.connect(
        user="postgres",
        password="123456",
        host="127.0.0.1",
        port=5432,
        database="postgres",
    )
    await conn.execute("DROP DATABASE IF EXISTS hei_fastapi_migration_shadow")
    await conn.close()

asyncio.run(main())
PY
```

如果当前开发库的 `alembic_version` 还指向已删除的旧 revision，不要直接在该库上 autogenerate。重建完整迁移后，开发库通常直接重建库再执行：

```bash
python scripts/migrate.py
```

如果必须保留现有库，需要先人工确认结构已经与新迁移一致，再执行 `alembic stamp head` 只同步版本号。
