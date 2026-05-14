from typing import List
from datetime import datetime, timedelta
from sqlalchemy.orm import Session
from sqlalchemy import select, func, and_
from core.enums import UserStatusEnum
from modules.sys.user.models import SysUser
from modules.sys.role.models import SysRole
from modules.sys.org.models import SysOrg
from modules.sys.config.models import SysConfig
from modules.sys.notice.models import SysNotice
from modules.client.user.models import ClientUser


class AnalyzeDao:
    def __init__(self, db: Session):
        self.db = db

    def count_users(self) -> int:
        stmt = select(func.count()).select_from(SysUser)
        return self.db.execute(stmt).scalar() or 0

    def count_active_users(self) -> int:
        stmt = (
            select(func.count())
            .select_from(SysUser)
            .where(SysUser.status == UserStatusEnum.ACTIVE.value)
        )
        return self.db.execute(stmt).scalar() or 0

    def count_roles(self) -> int:
        stmt = select(func.count()).select_from(SysRole)
        return self.db.execute(stmt).scalar() or 0

    def count_orgs(self) -> int:
        stmt = select(func.count()).select_from(SysOrg)
        return self.db.execute(stmt).scalar() or 0

    def count_configs(self) -> int:
        stmt = select(func.count()).select_from(SysConfig)
        return self.db.execute(stmt).scalar() or 0

    def count_notices(self) -> int:
        stmt = select(func.count()).select_from(SysNotice)
        return self.db.execute(stmt).scalar() or 0

    def user_trend(self, months: int = 12) -> List[dict]:
        month_col = func.date_format(SysUser.created_at, '%Y-%m').label("month")
        stmt = (
            select(month_col, func.count().label("count"))
            .where(SysUser.created_at.isnot(None))
            .group_by(month_col)
            .order_by(month_col.asc())
            .limit(months)
        )
        result = self.db.execute(stmt).all()
        return [{"month": row[0], "count": row[1]} for row in result]

    def org_user_distribution(self) -> List[dict]:
        stmt = (
            select(SysOrg.name, func.count(SysUser.id).label("count"))
            .outerjoin(SysUser, and_(
                SysUser.org_id == SysOrg.id,
            ))
            .group_by(SysOrg.id, SysOrg.name)
            .order_by(func.count(SysUser.id).desc())
        )
        result = self.db.execute(stmt).all()
        return [{"name": row[0], "count": row[1]} for row in result]

    def role_category_distribution(self) -> List[dict]:
        stmt = (
            select(SysRole.category, func.count().label("count"))
            .group_by(SysRole.category)
            .order_by(func.count().desc())
        )
        result = self.db.execute(stmt).all()
        return [{"category": row[0], "count": row[1]} for row in result]

    def count_client_users(self) -> int:
        stmt = select(func.count()).select_from(ClientUser)
        return self.db.execute(stmt).scalar() or 0

    def count_active_client_users(self) -> int:
        stmt = (
            select(func.count())
            .select_from(ClientUser)
            .where(
                ClientUser.status == UserStatusEnum.ACTIVE.value,
            )
        )
        return self.db.execute(stmt).scalar() or 0

    def client_user_trend(self, months: int = 12) -> List[dict]:
        month_col = func.date_format(ClientUser.created_at, '%Y-%m').label("month")
        stmt = (
            select(month_col, func.count().label("count"))
            .where(ClientUser.created_at.isnot(None))
            .group_by(month_col)
            .order_by(month_col.asc())
            .limit(months)
        )
        result = self.db.execute(stmt).all()
        return [{"month": row[0], "count": row[1]} for row in result]

