from datetime import datetime
from typing import List, Optional, Dict, Any
from sqlalchemy import select, and_, func, delete as sa_delete
from sqlalchemy.orm import Session
from core.enums import StatusEnum
from plugins.plugin_sys.user.models import SysUser
from plugins.plugin_sys.notice.models import SysNotice
from plugins.plugin_sys.resource.models import SysResource
from .models import SysQuickAction


class QuickActionDao:
    def __init__(self, db: Session):
        self.db = db

    # ---- base CRUD ----

    def find_by_id(self, id: str) -> Optional[SysQuickAction]:
        return self.db.execute(select(SysQuickAction).where(SysQuickAction.id == id)).scalar_one_or_none()

    def find_by_ids(self, ids: List[str]) -> List[SysQuickAction]:
        return list(self.db.execute(
            select(SysQuickAction).where(SysQuickAction.id.in_(ids))
        ).scalars().all())

    def insert(self, entity: SysQuickAction, user_id: Optional[str] = None) -> SysQuickAction:
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

    def delete_by_id(self, id: str) -> bool:
        entity = self.find_by_id(id)
        if not entity:
            return False
        self.db.delete(entity)
        self.db.commit()
        return True

    # ---- custom ----

    def find_by_user_id(self, user_id: str) -> List[dict]:
        stmt = (
            select(
                SysQuickAction.id,
                SysQuickAction.resource_id,
                SysQuickAction.sort_code,
                SysResource.name,
                SysResource.icon,
                SysResource.route_path,
            )
            .join(SysResource, and_(
                SysResource.id == SysQuickAction.resource_id,
            ))
            .where(
                SysQuickAction.user_id == user_id,
            )
            .order_by(SysQuickAction.sort_code.asc(), SysQuickAction.created_at.asc())
        )
        result = self.db.execute(stmt).all()
        return [
            {
                "id": row[0],
                "resource_id": row[1],
                "sort_code": row[2] or 0,
                "name": row[3] or "",
                "icon": row[4] or "",
                "route_path": row[5] or "",
            }
            for row in result
        ]

    def find_by_user_and_resource(self, user_id: str, resource_id: str) -> Optional[SysQuickAction]:
        stmt = select(SysQuickAction).where(
            SysQuickAction.user_id == user_id,
            SysQuickAction.resource_id == resource_id,
        )
        return self.db.execute(stmt).scalar_one_or_none()

    def count_quick_actions(self, user_id: str) -> int:
        stmt = select(func.count()).select_from(SysQuickAction).where(
            SysQuickAction.user_id == user_id,
        )
        return self.db.execute(stmt).scalar() or 0

    def get_notices(self, limit: int = 5) -> List[dict]:
        stmt = (
            select(
                SysNotice.id,
                SysNotice.title,
                SysNotice.level,
                SysNotice.created_at,
            )
            .where(
                SysNotice.status == StatusEnum.ENABLED,
            )
            .order_by(SysNotice.is_top.desc(), SysNotice.created_at.desc())
            .limit(limit)
        )
        result = self.db.execute(stmt).all()
        return [
            {"id": row[0], "title": row[1], "level": row[2] or "NORMAL", "created_at": row[3]}
            for row in result
        ]

    def get_stats(self) -> dict:
        stmt = select(func.count()).select_from(SysUser)
        return {"total_users": self.db.execute(stmt).scalar() or 0}

    def get_available_resources(self, user_id: str) -> List[dict]:
        subq = (
            select(SysQuickAction.resource_id)
            .where(
                SysQuickAction.user_id == user_id,
            )
            .scalar_subquery()
        )
        stmt = (
            select(
                SysResource.id,
                SysResource.parent_id,
                SysResource.type,
                SysResource.name,
                SysResource.icon,
                SysResource.route_path,
            )
            .where(
                SysResource.status == StatusEnum.ENABLED,
                SysResource.type.in_(["MENU", "DIRECTORY"]),
                SysResource.id.notin_(subq),
            )
            .order_by(SysResource.sort_code.asc())
            .limit(50)
        )
        result = self.db.execute(stmt).all()
        return [
            {"id": row[0], "parent_id": row[1] or "", "type": row[2] or "", "name": row[3] or "", "icon": row[4] or "", "route_path": row[5] or ""}
            for row in result
        ]
