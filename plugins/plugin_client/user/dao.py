from typing import Optional, Dict, Any, List
from datetime import datetime
from sqlalchemy import select, or_, func, delete as sa_delete
from sqlalchemy.orm import Session
from .models import ClientUser
from .params import ClientUserPageParam


class ClientUserDao:
    def __init__(self, db: Session):
        self.db = db

    # ---- base CRUD ----

    def find_by_id(self, id: str) -> Optional[ClientUser]:
        return self.db.execute(select(ClientUser).where(ClientUser.id == id)).scalar_one_or_none()

    def find_by_ids(self, ids: List[str]) -> List[ClientUser]:
        return list(self.db.execute(
            select(ClientUser).where(ClientUser.id.in_(ids))
        ).scalars().all())

    def insert(self, entity: ClientUser, user_id: Optional[str] = None) -> ClientUser:
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

    def update(self, entity: ClientUser, user_id: Optional[str] = None) -> ClientUser:
        entity.updated_at = datetime.now()
        if user_id is not None:
            entity.updated_by = user_id
        self.db.commit()
        self.db.refresh(entity)
        return entity

    def delete_by_ids(self, ids: List[str]) -> int:
        if not ids:
            return 0
        stmt = sa_delete(ClientUser).where(ClientUser.id.in_(ids))
        affected = self.db.execute(stmt).rowcount
        self.db.commit()
        return affected

    # ---- custom ----

    def find_page_by_filters(self, param: ClientUserPageParam) -> Dict[str, Any]:
        filters = []
        if param.keyword:
            keyword = f"%{param.keyword}%"
            filters.append(or_(ClientUser.username.ilike(keyword), ClientUser.nickname.ilike(keyword), ClientUser.phone.ilike(keyword), ClientUser.email.ilike(keyword)))
        if param.status:
            filters.append(ClientUser.status == param.status)

        current = max(1, param.current)
        size = max(1, param.size)
        offset = (current - 1) * size

        count_stmt = select(func.count()).select_from(ClientUser).where(*filters)
        total = self.db.execute(count_stmt).scalar() or 0

        stmt = select(ClientUser).where(*filters).order_by(ClientUser.created_at.desc()).offset(offset).limit(size)
        records = list(self.db.execute(stmt).scalars().all())

        return {"records": records, "total": total}

    def find_by_username(self, username: str) -> Optional[ClientUser]:
        return self.db.execute(
            select(ClientUser).where(ClientUser.username == username)
        ).scalar_one_or_none()

    def find_by_email(self, email: str) -> Optional[ClientUser]:
        return self.db.execute(
            select(ClientUser).where(ClientUser.email == email)
        ).scalar_one_or_none()
