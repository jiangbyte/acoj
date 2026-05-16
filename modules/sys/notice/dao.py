from typing import Optional, List, Dict, Any
from datetime import datetime
from sqlalchemy.orm import Session
from sqlalchemy import select, func, delete as sa_delete
from .models import SysNotice
from .params import NoticePageParam


class NoticeDao:
    def __init__(self, db: Session):
        self.db = db

    def find_by_id(self, id: str) -> Optional[SysNotice]:
        return self.db.execute(select(SysNotice).where(SysNotice.id == id)).scalar_one_or_none()

    def find_page(self, param: NoticePageParam) -> Dict[str, Any]:
        current = max(1, param.current)
        size = max(1, param.size)
        offset = (current - 1) * size
        stmt = select(SysNotice)
        count_stmt = select(func.count()).select_from(stmt.subquery())
        total = self.db.execute(count_stmt).scalar() or 0
        records = list(self.db.execute(stmt.offset(offset).limit(size)).scalars().all())
        return {"records": records, "total": total}

    def insert(self, entity: SysNotice, user_id: Optional[str] = None) -> SysNotice:
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

    def update(self, entity: SysNotice, user_id: Optional[str] = None) -> SysNotice:
        entity.updated_at = datetime.now()
        if user_id is not None:
            entity.updated_by = user_id
        self.db.commit()
        self.db.refresh(entity)
        return entity

    def delete_by_ids(self, ids: List[str]) -> int:
        if not ids:
            return 0
        stmt = sa_delete(SysNotice).where(SysNotice.id.in_(ids))
        affected = self.db.execute(stmt).rowcount
        self.db.commit()
        return affected
