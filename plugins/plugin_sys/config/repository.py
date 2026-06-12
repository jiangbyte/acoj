from typing import Optional, List, Dict, Any
from datetime import datetime
from sqlalchemy.orm import Session
from sqlalchemy import select, func, delete as sa_delete
from .models import SysConfig
from .params import ConfigPageParam


class ConfigRepository:
    def __init__(self, db: Session):
        self.db = db

    # ---- base CRUD ----

    def find_by_id(self, id: str) -> Optional[SysConfig]:
        return self.db.execute(select(SysConfig).where(SysConfig.id == id)).scalar_one_or_none()

    def find_by_ids(self, ids: List[str]) -> List[SysConfig]:
        return list(self.db.execute(
            select(SysConfig).where(SysConfig.id.in_(ids))
        ).scalars().all())

    def insert(self, entity: SysConfig, user_id: Optional[str] = None) -> SysConfig:
        from core.utils.snowflake_utils import generate_id
        now = datetime.now()
        if not entity.id:
            entity.id = generate_id()
        if entity.created_at is None:
            entity.created_at = now
        entity.updated_at = now
        if user_id is not None and entity.created_by is None:
            entity.created_by = user_id
        self.db.add(entity)
        self.db.commit()
        self.db.refresh(entity)
        return entity

    def update(self, entity: SysConfig, user_id: Optional[str] = None) -> SysConfig:
        entity.updated_at = datetime.now()
        if user_id is not None:
            entity.updated_by = user_id
        self.db.commit()
        self.db.refresh(entity)
        return entity

    def delete_by_ids(self, ids: List[str]) -> int:
        if not ids:
            return 0
        stmt = sa_delete(SysConfig).where(SysConfig.id.in_(ids))
        affected = self.db.execute(stmt).rowcount
        self.db.commit()
        return affected

    # ---- custom ----

    def find_by_key(self, key: str) -> Optional[SysConfig]:
        return self.db.execute(
            select(SysConfig).where(SysConfig.config_key == key)
        ).scalar_one_or_none()

    def find_page_by_filters(self, param: ConfigPageParam) -> Dict[str, Any]:
        filters = []
        if param.category:
            filters.append(SysConfig.category == param.category)
        if param.keyword:
            filters.append(SysConfig.config_key.like(f"%{param.keyword}%"))

        current = max(1, param.current)
        size = max(1, param.size)
        offset = (current - 1) * size

        count_stmt = select(func.count()).select_from(SysConfig).where(*filters)
        total = self.db.execute(count_stmt).scalar() or 0

        stmt = select(SysConfig).where(*filters).offset(offset).limit(size)
        records = list(self.db.execute(stmt).scalars().all())

        return {"records": records, "total": total}

    def find_by_category(self, category: str) -> List[SysConfig]:
        return list(self.db.execute(
            select(SysConfig).where(SysConfig.category == category)
        ).scalars().all())

    def find_by_category_and_key(self, category: str, key: str) -> Optional[SysConfig]:
        return self.db.execute(
            select(SysConfig).where(
                SysConfig.category == category,
                SysConfig.config_key == key,
            )
        ).scalar_one_or_none()

    def find_by_category_and_keys(self, category: str, keys: List[str]) -> Dict[str, SysConfig]:
        rows = self.db.execute(
            select(SysConfig).where(
                SysConfig.category == category,
                SysConfig.config_key.in_(keys),
            )
        ).scalars().all()
        return {r.config_key: r for r in rows}

    def find_by_keys(self, keys: List[str]) -> Dict[str, SysConfig]:
        rows = self.db.execute(
            select(SysConfig).where(SysConfig.config_key.in_(keys))
        ).scalars().all()
        return {r.config_key: r for r in rows}
