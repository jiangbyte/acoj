from typing import Optional, List
from datetime import datetime
from fastapi import Request
from sqlalchemy.orm import Session
from core.result import page_data
from core.exception import BusinessException
from core.utils.model_utils import strip_system_fields, apply_update
from core.utils.snowflake_utils import generate_id
from core.auth import HeiAuthTool
from .models import SysConfig
from .dao import ConfigDao
from .params import ConfigVO, ConfigPageParam, ConfigListParam


CONFIG_CACHE_PREFIX = "sys-config:"


class ConfigService:
    def __init__(self, db: Session):
        self.dao = ConfigDao(db)

    def _get_current_user_id(self, request: Request) -> Optional[str]:
        try:
            return HeiAuthTool.getLoginIdDefaultNull(request)
        except Exception:
            return None

    def _get_cached_value(self, key: str) -> Optional[str]:
        from core.db.redis import get_client
        client = get_client()
        if client:
            val = client.get(f"{CONFIG_CACHE_PREFIX}{key}")
            if val is not None:
                return val
        return None

    def _set_cached_value(self, key: str, value: str):
        from core.db.redis import get_client
        client = get_client()
        if client:
            client.set(f"{CONFIG_CACHE_PREFIX}{key}", value)

    def _del_cached_value(self, key: str):
        from core.db.redis import get_client
        client = get_client()
        if client:
            client.delete(f"{CONFIG_CACHE_PREFIX}{key}")

    def get_value_by_key(self, key: str) -> Optional[str]:
        cached = self._get_cached_value(key)
        if cached is not None:
            return cached

        entity = self.dao.find_by_key(key)
        if entity:
            self._set_cached_value(key, entity.config_value)
            return entity.config_value
        return None

    def page(self, param: ConfigPageParam) -> dict:
        result = self.dao.find_page(param)
        records = result["records"]
        total = result["total"]
        vo_list = [ConfigVO.model_validate(r).model_dump() for r in records]
        return page_data(records=vo_list, total=total, page=param.current, size=param.size)

    def list_by_category(self, param: ConfigListParam) -> List[dict]:
        entities = self.dao.find_by_category(param.category)
        return [ConfigVO.model_validate(e).model_dump() for e in entities]

    async def create(self, vo: ConfigVO, request: Request):
        now = datetime.now()
        entity = SysConfig(**strip_system_fields(vo.model_dump()))
        entity.id = generate_id()
        entity.is_deleted = "NO"
        entity.created_at = now
        entity.updated_at = now
        entity.created_by = self._get_current_user_id(request)
        self.dao.insert(entity)

    async def modify(self, vo: ConfigVO, request: Request):
        now = datetime.now()
        entity = self.dao.find_by_id(vo.id)
        if not entity:
            raise BusinessException("数据不存在")
        update_data = strip_system_fields(vo.model_dump(exclude_unset=True))
        apply_update(entity, update_data)
        entity.updated_at = now
        entity.updated_by = self._get_current_user_id(request)
        self.dao.update(entity)
        self._del_cached_value(entity.config_key)

    def remove(self, param):
        entities = self.dao.find_by_ids(param.ids)
        for entity in entities:
            self._del_cached_value(entity.config_key)
        self.dao.delete_by_ids(param.ids)

    def detail(self, param) -> Optional[ConfigVO]:
        entity = self.dao.find_by_id(param.id)
        return ConfigVO.model_validate(entity) if entity else None

    async def edit_batch(self, param, request: Request):
        now = datetime.now()
        user_id = self._get_current_user_id(request)
        for vo in param.configs:
            entity = self.dao.find_by_id(vo.id)
            if not entity:
                raise BusinessException(f"配置不存在: {vo.id}")
            update_data = strip_system_fields(vo.model_dump(exclude_unset=True))
            apply_update(entity, update_data)
            entity.updated_at = now
            entity.updated_by = user_id
            self.dao.update(entity)
            self._del_cached_value(entity.config_key)
