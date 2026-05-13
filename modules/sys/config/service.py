from typing import Optional, List
from fastapi import Request
from sqlalchemy.orm import Session
from core.result import page_data, PageDataField
from core.exception import BusinessException
from core.utils.model_utils import strip_system_fields, apply_update
from core.auth import HeiAuthTool
from core.db.redis import get_client
from .models import SysConfig
from .dao import ConfigDao
from .params import ConfigVO, ConfigPageParam, ConfigListParam, ConfigCategoryEditParam


CONFIG_CACHE_PREFIX = "sys-config:"


class ConfigService:
    def __init__(self, db: Session):
        self.dao = ConfigDao(db)

    async def _get_current_user_id(self, request: Request) -> Optional[str]:
        try:
            return await HeiAuthTool.getLoginIdDefaultNull(request)
        except Exception:
            return None

    async def _get_cached_value(self, key: str) -> Optional[str]:
        client = get_client()
        if client:
            val = await client.get(f"{CONFIG_CACHE_PREFIX}{key}")
            if val is not None:
                return val
        return None

    async def _set_cached_value(self, key: str, value: str):
        client = get_client()
        if client:
            await client.set(f"{CONFIG_CACHE_PREFIX}{key}", value)

    async def _del_cached_value(self, key: str):
        client = get_client()
        if client:
            await client.delete(f"{CONFIG_CACHE_PREFIX}{key}")

    async def get_value_by_key(self, key: str) -> Optional[str]:
        cached = await self._get_cached_value(key)
        if cached is not None:
            return cached

        entity = self.dao.find_by_key(key)
        if entity:
            await self._set_cached_value(key, entity.config_value)
            return entity.config_value
        return None

    def page(self, param: ConfigPageParam) -> dict:
        result = self.dao.find_page(param)
        records = result[PageDataField.RECORDS]
        total = result[PageDataField.TOTAL]
        vo_list = [ConfigVO.model_validate(r).model_dump() for r in records]
        return page_data(records=vo_list, total=total, page=param.current, size=param.size)

    def list_by_category(self, param: ConfigListParam) -> List[dict]:
        entities = self.dao.find_by_category(param.category)
        return [ConfigVO.model_validate(e).model_dump() for e in entities]

    async def create(self, vo: ConfigVO, request: Request):
        entity = SysConfig(**strip_system_fields(vo.model_dump()))
        self.dao.insert(entity, user_id=await self._get_current_user_id(request))

    async def modify(self, vo: ConfigVO, request: Request):
        entity = self.dao.find_by_id(vo.id)
        if not entity:
            raise BusinessException("数据不存在")
        update_data = strip_system_fields(vo.model_dump(exclude_unset=True))
        apply_update(entity, update_data)
        self.dao.update(entity, user_id=await self._get_current_user_id(request))
        await self._del_cached_value(entity.config_key)

    async def remove(self, param):
        entities = self.dao.find_by_ids(param.ids)
        for entity in entities:
            await self._del_cached_value(entity.config_key)
        self.dao.delete_by_ids(param.ids)

    def detail(self, param) -> Optional[ConfigVO]:
        entity = self.dao.find_by_id(param.id)
        return ConfigVO.model_validate(entity) if entity else None

    async def edit_batch(self, param, request: Request):
        user_id = await self._get_current_user_id(request)
        for vo in param.configs:
            entity = self.dao.find_by_id(vo.id)
            if not entity:
                raise BusinessException(f"配置不存在: {vo.id}")
            update_data = strip_system_fields(vo.model_dump(exclude_unset=True))
            apply_update(entity, update_data)
            self.dao.update(entity, user_id=user_id)
            await self._del_cached_value(entity.config_key)

    async def edit_by_category(self, param: ConfigCategoryEditParam, request: Request):
        user_id = await self._get_current_user_id(request)
        for vo in param.configs:
            entity = self.dao.find_by_category_and_key(param.category, vo.config_key)
            if not entity:
                raise BusinessException(f"分类 [{param.category}] 下不存在配置: {vo.config_key}")
            entity.config_value = vo.config_value
            self.dao.update(entity, user_id=user_id)
            await self._del_cached_value(entity.config_key)
