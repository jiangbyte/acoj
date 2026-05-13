from datetime import datetime
from typing import List, Optional
from sqlalchemy import select, and_, func
from sqlalchemy.orm import Session
from core.db.base_dao import BaseDAO
from core.enums import SoftDeleteEnum, StatusEnum
from modules.sys.user.models import SysUser
from modules.sys.notice.models import SysNotice
from modules.sys.resource.models import SysResource
from .models import SysQuickAction


class QuickActionDao(BaseDAO):
    def __init__(self, db: Session):
        super().__init__(db, SysQuickAction)

    def find_by_user_id(self, user_id: str) -> List[dict]:
        nd = SoftDeleteEnum.NO
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
                SysResource.is_deleted == nd,
            ))
            .where(
                SysQuickAction.user_id == user_id,
                SysQuickAction.is_deleted == nd,
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
            SysQuickAction.is_deleted == SoftDeleteEnum.NO,
        )
        return self.db.execute(stmt).scalar_one_or_none()

    def count_quick_actions(self, user_id: str) -> int:
        stmt = select(func.count()).select_from(SysQuickAction).where(
            SysQuickAction.user_id == user_id,
            SysQuickAction.is_deleted == SoftDeleteEnum.NO,
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
                SysNotice.is_deleted == SoftDeleteEnum.NO,
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
        stmt = select(func.count()).select_from(SysUser).where(SysUser.is_deleted == SoftDeleteEnum.NO)
        return {"total_users": self.db.execute(stmt).scalar() or 0}

    def get_available_resources(self, user_id: str) -> List[dict]:
        """Get menu-type resources the user can add as quick actions (excluding already added)."""
        nd = SoftDeleteEnum.NO
        subq = (
            select(SysQuickAction.resource_id)
            .where(
                SysQuickAction.user_id == user_id,
                SysQuickAction.is_deleted == nd,
            )
            .scalar_subquery()
        )
        stmt = (
            select(
                SysResource.id,
                SysResource.name,
                SysResource.icon,
                SysResource.route_path,
            )
            .where(
                SysResource.is_deleted == nd,
                SysResource.status == StatusEnum.ENABLED,
                SysResource.type.in_(["MENU", "DIRECTORY"]),
                SysResource.id.notin_(subq),
            )
            .order_by(SysResource.sort_code.asc())
            .limit(50)
        )
        result = self.db.execute(stmt).all()
        return [
            {"id": row[0], "name": row[1] or "", "icon": row[2] or "", "route_path": row[3] or ""}
            for row in result
        ]
