"""
Config service — standalone functions mirroring hei-gin's service.go pattern.
Explicit field-by-field construction.
"""

from typing import Optional, List
from datetime import datetime
from sqlalchemy.orm import Session
from sqlalchemy import update as sa_update, select
from fastapi import Request
from .models import SysConfig
from .params import ConfigVO, ConfigPageParam, ConfigListParam, ConfigCategoryEditParam, SysConfigToConfigVO
from .repository import ConfigRepository
from core.utils import generate_id
from core.exception import BusinessException
from core.result import page_data, PageDataField
from core.auth import HeiAuthTool
from core.db.redis import get_client
import logging

logger = logging.getLogger(__name__)

CONFIG_CACHE_PREFIX = "sys-config:"
def page(db: Session, param: ConfigPageParam) -> dict:
    repository = ConfigRepository(db)
    result = repository.find_page_by_filters(param)
    records = [SysConfigToConfigVO(r) for r in result.get("records", [])]
    return page_data(records=records, total=result[PageDataField.TOTAL], page=param.current, size=param.size)


def detail(db: Session, id: str) -> Optional[dict]:
    if not id:
        return None
    entity = ConfigRepository(db).find_by_id(id)
    if not entity:
        return None
    return SysConfigToConfigVO(entity)


def create(db: Session, vo: ConfigVO, user_id: Optional[str] = None) -> None:
    now = datetime.now()
    entity = SysConfig(
        id=generate_id(),
        sort_code=vo.sort_code or 0,
        created_at=now,
        updated_at=now,
    )
    if vo.config_key is not None:
        entity.config_key = vo.config_key
    if vo.config_value is not None:
        entity.config_value = vo.config_value
    if vo.remark is not None:
        entity.remark = vo.remark
    if vo.category is not None:
        entity.category = vo.category
    if vo.extra is not None:
        entity.extra = vo.extra
    if user_id:
        entity.created_by = user_id
        entity.updated_by = user_id
    ConfigRepository(db).insert(entity)


def modify(db: Session, vo: ConfigVO, user_id: Optional[str] = None) -> None:
    repository = ConfigRepository(db)
    entity = repository.find_by_id(vo.id)
    if not entity:
        raise BusinessException("数据不存在")
    now = datetime.now()
    up = {"sort_code": vo.sort_code, "updated_at": now}
    if vo.config_key is not None:
        up["config_key"] = vo.config_key
    if vo.config_value is not None:
        up["config_value"] = vo.config_value
    if vo.remark is not None:
        up["remark"] = vo.remark
    if vo.category is not None:
        up["category"] = vo.category
    if vo.extra is not None:
        up["extra"] = vo.extra
    if user_id:
        up["updated_by"] = user_id
    repository.db.execute(sa_update(SysConfig).where(SysConfig.id == vo.id).values(**up))
    repository.db.commit()
    # Clear cache
    _del_cached_key(entity.config_key)


def remove(db: Session, ids: list) -> None:
    if not ids:
        return
    repository = ConfigRepository(db)
    entities = repository.find_by_ids(ids)
    keys = [e.config_key for e in entities if e.config_key is not None]
    repository.delete_by_ids(ids)
    for k in keys:
        _del_cached_key(k)


def options(db: Session) -> list:
    rows = db.execute(select(SysConfig).order_by(SysConfig.sort_code.asc())).scalars().all()
    return [SysConfigToConfigVO(r) for r in rows]


def list_by_category(db: Session, category: str) -> list:
    rows = db.execute(
        select(SysConfig).where(SysConfig.category == category).order_by(SysConfig.sort_code.asc())
    ).scalars().all()
    return [SysConfigToConfigVO(r) for r in rows]


def edit_batch(db: Session, param, user_id: Optional[str] = None) -> None:
    """Batch edit configs — explicit field handling per item like Go."""
    now = datetime.now()
    repository = ConfigRepository(db)
    for item in param.configs:
        up = {"updated_at": now}
        if item.config_key is not None:
            up["config_key"] = item.config_key
        if item.config_value is not None:
            up["config_value"] = item.config_value
        if item.remark is not None:
            up["remark"] = item.remark
        if hasattr(item, 'sort_code') and item.sort_code is not None and item.sort_code != 0:
            up["sort_code"] = item.sort_code
        if user_id:
            up["updated_by"] = user_id
        repository.db.execute(sa_update(SysConfig).where(SysConfig.id == item.id).values(**up))
    repository.db.commit()


def edit_by_category(db: Session, param: ConfigCategoryEditParam, user_id: Optional[str] = None) -> None:
    """Edit configs by category — sets all configs in category to same values like Go."""
    now = datetime.now()
    up = {"updated_at": now}
    if param.config_key is not None:
        up["config_key"] = param.config_key
    if param.config_value is not None:
        up["config_value"] = param.config_value
    if param.remark is not None:
        up["remark"] = param.remark
    if user_id:
        up["updated_by"] = user_id
    repository = ConfigRepository(db)
    repository.db.execute(sa_update(SysConfig).where(SysConfig.category == param.category).values(**up))
    repository.db.commit()


# ── Cache helpers (extra, not in Go) ──

async def get_value_by_key(key: str) -> Optional[str]:
    client = get_client()
    if client:
        cached = await client.get(f"{CONFIG_CACHE_PREFIX}{key}")
        if cached is not None:
            return cached
    from core.db.mysql import get_db
    db = next(get_db())
    try:
        from .repository import ConfigRepository
        entity = ConfigRepository(db).find_by_key(key)
        if entity:
            if client:
                await client.set(f"{CONFIG_CACHE_PREFIX}{key}", entity.config_value or "")
            return entity.config_value
        return None
    finally:
        db.close()


def _del_cached_key(key: Optional[str]) -> None:
    if not key:
        return
    import asyncio
    try:
        loop = asyncio.get_event_loop()
        if loop.is_running():
            asyncio.ensure_future(_async_del_cached_key(key))
        else:
            loop.run_until_complete(_async_del_cached_key(key))
    except Exception:
        pass


async def _async_del_cached_key(key: str) -> None:
    client = get_client()
    if client:
        await client.delete(f"{CONFIG_CACHE_PREFIX}{key}")


# ═════════════════════════════════════════════════════════════════════
# Backward-compatible class
# ═════════════════════════════════════════════════════════════════════

class ConfigService:
    def __init__(self, db: Session):
        self.db = db
        self.repository = ConfigRepository(db)

    async def _get_user_id(self, request: Optional[Request] = None) -> Optional[str]:
        try:
            return await HeiAuthTool.getLoginIdDefaultNull(request)
        except Exception:
            return None

    def page(self, param: ConfigPageParam) -> dict:
        return page(self.db, param)

    def detail(self, id: str):
        return detail(self.db, id)

    async def create(self, vo: ConfigVO, request: Optional[Request] = None) -> None:
        return create(self.db, vo, await self._get_user_id(request))

    async def modify(self, vo: ConfigVO, request: Optional[Request] = None) -> None:
        return modify(self.db, vo, await self._get_user_id(request))

    def remove(self, ids: list) -> None:
        return remove(self.db, ids)

    def options(self) -> list:
        return options(self.db)

    def list_by_category(self, category: str) -> list:
        return list_by_category(self.db, category)

    async def edit_batch(self, param, request: Optional[Request] = None) -> None:
        return edit_batch(self.db, param, await self._get_user_id(request))

    async def edit_by_category(self, param: ConfigCategoryEditParam, request: Optional[Request] = None) -> None:
        return edit_by_category(self.db, param, await self._get_user_id(request))
