from typing import Optional, List, Dict, Any
from datetime import datetime
from sqlalchemy.orm import Session
from sqlalchemy import select, func, delete as sa_delete
from .models import SysBanner
from .params import BannerPageParam


class BannerDao:
    def __init__(self, db: Session):
        self.db = db

    def find_by_id(self, id: str) -> Optional[SysBanner]:
        return self.db.execute(select(SysBanner).where(SysBanner.id == id)).scalar_one_or_none()

    def find_page(self, param: BannerPageParam) -> Dict[str, Any]:
        current = max(1, param.current)
        size = max(1, param.size)
        if size > 100:
            size = 100
        offset = (current - 1) * size
        stmt = select(SysBanner).order_by(SysBanner.created_at.desc())
        count_stmt = select(func.count()).select_from(stmt.subquery())
        total = self.db.execute(count_stmt).scalar() or 0
        records = list(self.db.execute(stmt.offset(offset).limit(size)).scalars().all())
        return {"records": records, "total": total}

    def insert(self, entity: SysBanner, user_id: Optional[str] = None) -> SysBanner:
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

    def update(self, entity: SysBanner, user_id: Optional[str] = None) -> SysBanner:
        entity.updated_at = datetime.now()
        if user_id is not None:
            entity.updated_by = user_id
        self.db.commit()
        self.db.refresh(entity)
        return entity

    def delete_by_ids(self, ids: List[str]) -> int:
        if not ids:
            return 0
        stmt = sa_delete(SysBanner).where(SysBanner.id.in_(ids))
        affected = self.db.execute(stmt).rowcount
        self.db.commit()
        return affected
