"""Config service — class-based service with DI-friendly provider."""

from typing import Optional
from datetime import datetime
from fastapi import Depends
from sqlalchemy.orm import Session

from sdk.infra.db import get_db
from sdk.shared.di import ActorContext
from sdk.web.exception import BusinessException
from sdk.web.result import PageDataField, page_data
from sdk.utils import generate_id
from sdk.infra.db.redis import get_client
from .models import SysConfig
from .params import ConfigVO, ConfigPageParam, ConfigListParam, ConfigCategoryEditParam, SysConfigToConfigVO
from .repository import ConfigRepository
import logging

logger = logging.getLogger(__name__)

CONFIG_CACHE_PREFIX = "sys-config:"
# ── Cache helpers (extra, not in Go) ──

async def get_value_by_key(key: str) -> Optional[str]:
    client = get_client()
    if client:
        cached = await client.get(f"{CONFIG_CACHE_PREFIX}{key}")
        if cached is not None:
            return cached
    from sdk.infra.db.mysql import get_db
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


class ConfigService:
    def __init__(self, repository: ConfigRepository):
        self.repository = repository

    @classmethod
    def from_db(cls, db: Session) -> "ConfigService":
        return cls(ConfigRepository(db))

    def page(self, param: ConfigPageParam) -> dict:
        result = self.repository.find_page_by_filters(param)
        records = [SysConfigToConfigVO(r) for r in result.get("records", [])]
        return page_data(records=records, total=result[PageDataField.TOTAL], page=param.current, size=param.size)

    def detail(self, id: str) -> Optional[ConfigVO]:
        if not id:
            return None
        entity = self.repository.find_by_id(id)
        if not entity:
            return None
        return SysConfigToConfigVO(entity)

    def create(self, vo: ConfigVO, actor: Optional[ActorContext] = None) -> None:
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
        if actor and actor.user_id:
            entity.created_by = actor.user_id
            entity.updated_by = actor.user_id
        self.repository.insert(entity)

    def modify(self, vo: ConfigVO, actor: Optional[ActorContext] = None) -> None:
        entity = self.repository.find_by_id(vo.id)
        if not entity:
            raise BusinessException("数据不存在")
        up = {"sort_code": vo.sort_code, "updated_at": datetime.now()}
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
        if actor and actor.user_id:
            up["updated_by"] = actor.user_id
        self.repository.update_by_id(vo.id, up)
        _del_cached_key(entity.config_key)

    def remove(self, ids: list[str]) -> None:
        if not ids:
            return
        entities = self.repository.find_by_ids(ids)
        keys = [e.config_key for e in entities if e.config_key is not None]
        self.repository.delete_by_ids(ids)
        for key in keys:
            _del_cached_key(key)

    def options(self) -> list:
        return [SysConfigToConfigVO(r) for r in self.repository.list_all_ordered()]

    def list_by_category(self, category: str) -> list:
        return [SysConfigToConfigVO(r) for r in self.repository.find_by_category(category)]

    def edit_batch(self, param, actor: Optional[ActorContext] = None) -> None:
        now = datetime.now()
        items: list[tuple[str, dict]] = []
        for item in param.configs:
            up = {"updated_at": now}
            if item.config_key is not None:
                up["config_key"] = item.config_key
            if item.config_value is not None:
                up["config_value"] = item.config_value
            if item.remark is not None:
                up["remark"] = item.remark
            if hasattr(item, "sort_code") and item.sort_code is not None and item.sort_code != 0:
                up["sort_code"] = item.sort_code
            if actor and actor.user_id:
                up["updated_by"] = actor.user_id
            items.append((item.id, up))
        self.repository.update_many_by_ids(items)

    def edit_by_category(self, param: ConfigCategoryEditParam, actor: Optional[ActorContext] = None) -> None:
        up = {"updated_at": datetime.now()}
        if param.config_key is not None:
            up["config_key"] = param.config_key
        if param.config_value is not None:
            up["config_value"] = param.config_value
        if param.remark is not None:
            up["remark"] = param.remark
        if actor and actor.user_id:
            up["updated_by"] = actor.user_id
        self.repository.update_by_category(param.category, up)


def get_config_service(db: Session = Depends(get_db)) -> ConfigService:
    return ConfigService.from_db(db)
