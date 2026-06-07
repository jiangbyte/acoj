from typing import Optional, Dict, Any, List
from datetime import datetime
from sqlalchemy.orm import Session
from sqlalchemy import select, func, delete as sa_delete
from .models import SysPosition
from .params import PositionPageParam


class PositionDao:
    def __init__(self, db: Session):
        self.db = db

    # ---- base CRUD ----

    def find_by_id(self, id: str) -> Optional[SysPosition]:
        return self.db.execute(select(SysPosition).where(SysPosition.id == id)).scalar_one_or_none()

    def find_by_ids(self, ids: List[str]) -> List[SysPosition]:
        return list(self.db.execute(
            select(SysPosition).where(SysPosition.id.in_(ids))
        ).scalars().all())

    def find_all(self) -> List[SysPosition]:
        return list(self.db.execute(select(SysPosition)).scalars().all())

    def insert(self, entity: SysPosition, user_id: Optional[str] = None) -> SysPosition:
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

    def update(self, entity: SysPosition, user_id: Optional[str] = None) -> SysPosition:
        entity.updated_at = datetime.now()
        if user_id is not None:
            entity.updated_by = user_id
        self.db.commit()
        self.db.refresh(entity)
        return entity

    def delete_by_ids(self, ids: List[str]) -> int:
        if not ids:
            return 0
        stmt = sa_delete(SysPosition).where(SysPosition.id.in_(ids))
        affected = self.db.execute(stmt).rowcount
        self.db.commit()
        return affected

    # ---- custom ----

    def find_page_by_filters(self, param: PositionPageParam) -> Dict[str, Any]:
        filters = []
        if param.group_id:
            filters.append(SysPosition.group_id == param.group_id)
        if param.org_id:
            filters.append(SysPosition.org_id == param.org_id)
        if param.keyword:
            filters.append(SysPosition.name.like(f"%{param.keyword}%"))

        current = max(1, param.current)
        size = max(1, param.size)
        offset = (current - 1) * size

        count_stmt = select(func.count()).select_from(SysPosition).where(*filters)
        total = self.db.execute(count_stmt).scalar() or 0

        stmt = select(SysPosition).where(*filters).order_by(SysPosition.sort_code.asc()).offset(offset).limit(size)
        records = list(self.db.execute(stmt).scalars().all())

        return {"records": records, "total": total}
