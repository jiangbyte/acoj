"""Home DAO — mirrors hei-gin plugin-sys/home/service.go queries."""

from datetime import datetime
from typing import List, Optional, Dict, Any
from sqlalchemy import select, and_, func, delete as sa_delete
from sqlalchemy.orm import Session
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
            entity.updated_by = user_id
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

    def find_by_user_id(self, user_id: str) -> List[Dict[str, Any]]:
        """Quick actions for a user, enriched with resource info — mirrors Go findQuickActionsByUserID."""
        actions = list(self.db.execute(
            select(SysQuickAction).where(SysQuickAction.user_id == user_id)
            .order_by(SysQuickAction.sort_code.asc(), SysQuickAction.created_at.asc())
        ).scalars().all())

        if not actions:
            return []

        resource_ids = [a.resource_id for a in actions]
        resources = list(self.db.execute(
            select(SysResource).where(SysResource.id.in_(resource_ids))
        ).scalars().all())
        resource_map = {r.id: r for r in resources}

        result = []
        for a in actions:
            vo = {
                "id": a.id,
                "resource_id": a.resource_id,
                "parent_id": "",
                "type": "",
                "name": "",
                "icon": "",
                "route_path": "",
                "sort_code": a.sort_code or 0,
            }
            r = resource_map.get(a.resource_id)
            if r:
                vo["name"] = r.name
                vo["type"] = r.type
                if r.icon:
                    vo["icon"] = r.icon
                if r.route_path:
                    vo["route_path"] = r.route_path
                if r.parent_id:
                    vo["parent_id"] = r.parent_id
            result.append(vo)
        return result

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

    def get_notices(self, limit: int = 5) -> List[Dict[str, Any]]:
        """Get notices — mirrors Go getNotices: status=ENABLED, category=PLATFORM."""
        rows = self.db.execute(
            select(
                SysNotice.id,
                SysNotice.title,
                SysNotice.level,
                SysNotice.created_at,
            )
            .where(
                SysNotice.status == "ENABLED",
                SysNotice.category == "PLATFORM",
            )
            .order_by(SysNotice.sort_code.asc(), SysNotice.is_top.desc())
            .limit(limit)
        ).all()
        return [
            {
                "id": row[0],
                "title": row[1],
                "level": row[2] if row[2] else "NORMAL",
                "created_at": row[3].strftime("%Y-%m-%d %H:%M:%S") if row[3] else "",
            }
            for row in rows
        ]

    def get_stats(self) -> dict:
        stmt = select(func.count()).select_from(SysUser)
        return {"total_users": self.db.execute(stmt).scalar() or 0}

    def get_available_resources(self, user_id: str) -> List[Dict[str, Any]]:
        """Available resources for quick actions — mirrors Go getAvailableResources."""
        subq = (
            select(SysQuickAction.resource_id)
            .where(SysQuickAction.user_id == user_id)
            .scalar_subquery()
        )
        rows = self.db.execute(
            select(
                SysResource.id,
                SysResource.parent_id,
                SysResource.type,
                SysResource.name,
                SysResource.icon,
                SysResource.route_path,
            )
            .where(
                SysResource.status == "ENABLED",
                SysResource.category.in_(["BACKEND_MENU", "FRONTEND_MENU"]),
                SysResource.id.notin_(subq),
            )
            .order_by(SysResource.sort_code.asc())
        ).all()
        return [
            {
                "id": row[0],
                "parent_id": row[1] if row[1] else "",
                "type": row[2] if row[2] else "",
                "name": row[3] if row[3] else "",
                "icon": row[4] if row[4] else "",
                "route_path": row[5] if row[5] else "",
            }
            for row in rows
        ]
