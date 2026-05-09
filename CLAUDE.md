## Architecture

Hei FastAPI is a Python port of the Java-based Hei Boot framework. It uses **FastAPI + SQLAlchemy 2.0 + MySQL + Redis + JWT + SM2 national-standard crypto**.

## Commands

```bash
# use conda environment
conda activate normal

# Install dependencies
pip install -r requirements.txt          # production

# Run server
python main.py                           # defaults to 0.0.0.0:8081
```

### Two auth systems

- **B-end** (back-office / admin): paths under `/api/v1/b/*` or any `/api/v1/<module>` not matching c/public. Uses `HeiAuthTool` + `@HeiCheckPermission("module:action")`.
- **C-end** (client-facing): paths under `/api/v1/c/*`. Uses `HeiClientAuthTool` + `@HeiClientCheckPermission("module:action")`.
- **Public** (no auth): `/api/v1/public/b/*`, `/api/v1/public/c/*`, static paths, OPTIONS preflight.

### Module structure (vertical slices)

Each business domain under `modules/` follows a consistent internal layout (see `modules/sys/banner/` as the most complete example):

```
modules/<domain>/
├── models.py        # SQLAlchemy 2.0 ORM model (Mapped + mapped_column, declarative)
├── params.py        # Pydantic v2 schemas: *VO, *PageParam, *ExportParam, *ImportParam
├── dao.py           # DAO extending BaseDAO from core/db/base_dao.py
├── service.py       # Business logic, calls DAO, raises BusinessException
└── api/v1/api.py    # FastAPI APIRouter with route definitions + @HeiCheckPermission decorators
```

The router must be manually registered in `core/app/router.py`.

### Core layers

| Layer | Key files | Purpose |
|---|---|---|
| App setup | `core/app/setup.py` | Factory: create_app() → lifespan → middleware → CORS → exception handlers → routers |
| Config | `config/settings.py` | Pydantic BaseSettings, env-based via `APP__KEY` convention |
| Database | `core/db/mysql.py`, `core/db/redis.py` | SQLAlchemy async engine + Redis async client |
| Base DAO | `core/db/base_dao.py` | Generic CRUD with soft-delete support (configurable field, enabled globally) |
| Middleware | `core/middleware/auth.py` | JWT auth middleware — dispatches by path pattern (B-end vs C-end vs public) |
| Auth decorators | `core/auth/decorator/` | `@HeiCheckLogin`, `@HeiCheckRole`, `@HeiCheckPermission` |
| Unified response | `core/result/result.py` | `success()`, `failure()`, `page_data()` dict helpers + `Result[T]`, `PageData[T]` models |
| Exception | `core/exception/business_exception.py` | `BusinessException(message, code)` — caught by global exception handler |
| Utils | `core/utils/` | SM2 crypto, Excel export, IP utils, Snowflake ID |

### API response convention

All endpoints return dict responses built by `success()` / `failure()` / `page_data()` helpers:
```python
{"code": 200, "message": "请求成功", "data": ..., "success": true}
```

`@HeiCheckPermission` decorators must go **below** the `@router.*` decorator (closest to the function).

### Soft delete

Controlled via `settings.db.soft_delete_*`. When enabled, `BaseDAO` transparently filters `is_deleted = "NO"` on all queries and sets `is_deleted = "YES"` on delete. The model must have the configured field.

### Startup sequence (lifespan.py)

1. Init Redis
2. Load SM2 keys
3. Init JWT auth tool
4. Register permission interface
5. Init captcha
6. Init username auth with LoginUserApiProvider
7. Serve
8. Shutdown: close Redis, dispose MySQL engine.

### Code Generator (`modules/dev`)

Auto-generates a full CRUD module from a database table. Uses **Jinja2** templates.

**API**: `POST /api/v1/sys/dev/generator/generate`
```json
{"table_name": "sys_banner", "module_path": "sys/banner"}
```

What gets generated (8 files):
- `models.py` — SQLAlchemy 2.0 ORM model with soft-delete and audit fields
- `params.py` — Pydantic VO, PageParam, ExportParam, ImportParam
- `dao.py` — DAO extending BaseDAO with Snowflake ID + timestamps
- `service.py` — Service with page/create/modify/remove/detail/export/download_template/import_data
- `api/v1/api.py` — 8 FastAPI route endpoints with `@HeiCheckPermission`
- `__init__.py`, `api/__init__.py`, `api/v1/__init__.py`

**Post-generation**: manually register the router in `core/app/router.py`:
```python
from modules.<module_path> import router
app.include_router(router)
```
