from typing import Optional, List, Dict, Any
from datetime import datetime
from sqlalchemy.orm import Session
from sqlalchemy import select, func, delete
from .models import SysFile
from .params import FilePageParam


class FileDao:
    def __init__(self, db: Session):
        self.db = db

    # ---- base CRUD ----

    def find_by_id(self, id: str) -> Optional[SysFile]:
        return self.db.execute(select(SysFile).where(SysFile.id == id)).scalar_one_or_none()

    def find_by_ids(self, ids: List[str]) -> List[SysFile]:
        return list(self.db.execute(
            select(SysFile).where(SysFile.id.in_(ids))
        ).scalars().all())

    def insert(self, entity: SysFile, user_id: Optional[str] = None) -> SysFile:
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

    def update(self, entity: SysFile, user_id: Optional[str] = None) -> SysFile:
        entity.updated_at = datetime.now()
        if user_id is not None:
            entity.updated_by = user_id
        self.db.commit()
        self.db.refresh(entity)
        return entity

    def delete_by_ids(self, ids: List[str]) -> int:
        if not ids:
            return 0
        stmt = delete(SysFile).where(SysFile.id.in_(ids))
        affected = self.db.execute(stmt).rowcount
        self.db.commit()
        return affected

    # ---- custom ----

    def find_page_by_filters(self, param: FilePageParam) -> Dict[str, Any]:
        filters = []
        if param.engine:
            filters.append(SysFile.engine == param.engine)
        if param.keyword:
            filters.append(SysFile.name.like(f"%{param.keyword}%"))
        if param.date_range_start:
            filters.append(SysFile.created_at >= param.date_range_start)
        if param.date_range_end:
            filters.append(SysFile.created_at <= param.date_range_end)

        current = max(1, param.current)
        size = max(1, param.size)
        offset = (current - 1) * size

        count_stmt = select(func.count()).select_from(SysFile).where(*filters)
        total = self.db.execute(count_stmt).scalar() or 0

        stmt = select(SysFile).where(*filters).order_by(SysFile.created_at.desc()).offset(offset).limit(size)
        records = list(self.db.execute(stmt).scalars().all())

        return {"records": records, "total": total}

    def delete_absolute_by_id(self, entity_id: str) -> bool:
        entity = self.db.get(SysFile, entity_id)
        if not entity:
            return False
        self.db.delete(entity)
        self.db.commit()
        return True

    def delete_absolute_by_ids(self, entity_ids: List[str]) -> int:
        stmt = delete(SysFile).where(SysFile.id.in_(entity_ids))
        result = self.db.execute(stmt)
        self.db.commit()
        return result.rowcount or 0
