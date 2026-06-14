"""Config service — class-based service with DI-friendly provider."""

import asyncio
from datetime import datetime
from typing import Optional

from fastapi import Depends
from sqlalchemy.ext.asyncio import AsyncSession

from sdk.infra.db.redis import get_client
from sdk.infra.db import get_db, AsyncSessionLocal
from sdk.shared.di import ActorContext
from sdk.utils import generate_id
from sdk.web.exception import BusinessException
from sdk.web.result import map_page_data

from .models import SysConfig
from .params import ConfigBatchEditParam, ConfigCategoryEditParam, ConfigPageParam, ConfigVO
from .repository import ConfigRepository

CONFIG_CACHE_PREFIX = "sys-config:"
# ── Cache helpers (extra, not in Go) ──


def _actor_user_id(actor: Optional[ActorContext]) -> Optional[str]:
    return actor.user_id if actor else None


async def get_value_by_key(key: str) -> Optional[str]:
    client = get_client()
    if client:
        cached = await client.get(f"{CONFIG_CACHE_PREFIX}{key}")
        if cached is not None:
            return cached
    async with AsyncSessionLocal() as db:
        from .repository import ConfigRepository
        entity = await ConfigRepository(db).find_by_key(key)
        if entity:
            if client:
                await client.set(f"{CONFIG_CACHE_PREFIX}{key}", entity.config_value or "")
            return entity.config_value
        return None


def _del_cached_key(key: Optional[str]) -> None:
    if not key:
        return
    try:
        loop = asyncio.get_running_loop()
    except RuntimeError:
        asyncio.run(_async_del_cached_key(key))
        return
    loop.create_task(_async_del_cached_key(key))


async def _async_del_cached_key(key: str) -> None:
    client = get_client()
    if client:
        await client.delete(f"{CONFIG_CACHE_PREFIX}{key}")


class ConfigService:
    def __init__(self, repository: ConfigRepository):
        self.repository = repository

    async def page(self, param: ConfigPageParam) -> dict:
        return map_page_data(
            await self.repository.find_page_by_filters(param),
            ConfigVO.model_validate,
            param.current,
            param.size,
        )

    async def detail(self, id: str) -> Optional[ConfigVO]:
        if not id:
            return None
        entity = await self.repository.find_by_id(id)
        if not entity:
            return None
        return ConfigVO.model_validate(entity)

    async def create(self, vo: ConfigVO, actor: Optional[ActorContext] = None) -> None:
        now = datetime.now()
        actor_user_id = _actor_user_id(actor)
        entity = SysConfig(
            id=generate_id(),
            sort_code=vo.sort_code or 0,
            created_at=now,
            updated_at=now,
            config_key=vo.config_key,
            config_value=vo.config_value,
            remark=vo.remark,
            category=vo.category,
            extra=vo.extra,
        )
        if actor_user_id:
            entity.created_by = actor_user_id
            entity.updated_by = actor_user_id
        await self.repository.insert(entity)

    async def modify(self, vo: ConfigVO, actor: Optional[ActorContext] = None) -> None:
        entity = await self.repository.find_by_id(vo.id)
        if not entity:
            raise BusinessException("数据不存在")
        actor_user_id = _actor_user_id(actor)
        up = {
            "sort_code": vo.sort_code,
            "updated_at": datetime.now(),
            "config_key": vo.config_key,
            "config_value": vo.config_value,
            "remark": vo.remark,
            "category": vo.category,
            "extra": vo.extra,
        }
        if actor_user_id:
            up["updated_by"] = actor_user_id
        await self.repository.update_by_id(vo.id, up)
        _del_cached_key(entity.config_key)

    async def remove(self, ids: list[str]) -> None:
        if not ids:
            return
        entities = await self.repository.find_by_ids(ids)
        keys = [e.config_key for e in entities if e.config_key is not None]
        await self.repository.delete_by_ids(ids)
        for key in keys:
            _del_cached_key(key)

    async def options(self) -> list:
        return [ConfigVO.model_validate(r) for r in await self.repository.list_all_ordered()]

    async def list_by_category(self, category: str) -> list:
        return [ConfigVO.model_validate(r) for r in await self.repository.find_by_category(category)]

    async def edit_batch(self, param: ConfigBatchEditParam, actor: Optional[ActorContext] = None) -> None:
        now = datetime.now()
        actor_user_id = _actor_user_id(actor)
        items: list[tuple[str, dict]] = []
        for item in param.configs:
            up = {"updated_at": now}
            if item.config_key is not None:
                up["config_key"] = item.config_key
            if item.config_value is not None:
                up["config_value"] = item.config_value
            if item.remark is not None:
                up["remark"] = item.remark
            if item.sort_code is not None and item.sort_code != 0:
                up["sort_code"] = item.sort_code
            if actor_user_id:
                up["updated_by"] = actor_user_id
            items.append((item.id, up))
        await self.repository.update_many_by_ids(items)

    async def edit_by_category(self, param: ConfigCategoryEditParam, actor: Optional[ActorContext] = None) -> None:
        up = {"updated_at": datetime.now()}
        if param.config_key is not None:
            up["config_key"] = param.config_key
        if param.config_value is not None:
            up["config_value"] = param.config_value
        if param.remark is not None:
            up["remark"] = param.remark
        actor_user_id = _actor_user_id(actor)
        if actor_user_id:
            up["updated_by"] = actor_user_id
        await self.repository.update_by_category(param.category, up)


def get_config_service(db: AsyncSession = Depends(get_db)) -> ConfigService:
    return ConfigService(ConfigRepository(db))
