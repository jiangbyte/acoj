from typing import List
from datetime import datetime, timedelta
from sqlalchemy.orm import Session
from sqlalchemy import select, func, and_
from core.enums import SoftDeleteEnum, UserStatusEnum
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
        stmt = select(func.count()).select_from(SysUser).where(SysUser.is_deleted == SoftDeleteEnum.NO)
        return self.db.execute(stmt).scalar() or 0

    def count_active_users(self) -> int:
        stmt = (
            select(func.count())
            .select_from(SysUser)
            .where(SysUser.is_deleted == SoftDeleteEnum.NO, SysUser.status == UserStatusEnum.ACTIVE.value)
        )
        return self.db.execute(stmt).scalar() or 0

    def count_roles(self) -> int:
        stmt = select(func.count()).select_from(SysRole).where(SysRole.is_deleted == SoftDeleteEnum.NO)
        return self.db.execute(stmt).scalar() or 0

    def count_orgs(self) -> int:
        stmt = select(func.count()).select_from(SysOrg).where(SysOrg.is_deleted == SoftDeleteEnum.NO)
        return self.db.execute(stmt).scalar() or 0

    def count_configs(self) -> int:
        stmt = select(func.count()).select_from(SysConfig).where(SysConfig.is_deleted == SoftDeleteEnum.NO)
        return self.db.execute(stmt).scalar() or 0

    def count_notices(self) -> int:
        stmt = select(func.count()).select_from(SysNotice).where(SysNotice.is_deleted == SoftDeleteEnum.NO)
        return self.db.execute(stmt).scalar() or 0

    def user_trend(self, months: int = 12) -> List[dict]:
        nd = SoftDeleteEnum.NO
        month_col = func.date_format(SysUser.created_at, '%Y-%m').label("month")
        stmt = (
            select(month_col, func.count().label("count"))
            .where(SysUser.is_deleted == nd, SysUser.created_at.isnot(None))
            .group_by(month_col)
            .order_by(month_col.asc())
            .limit(months)
        )
        result = self.db.execute(stmt).all()
        return [{"month": row[0], "count": row[1]} for row in result]

    def org_user_distribution(self) -> List[dict]:
        nd = SoftDeleteEnum.NO
        stmt = (
            select(SysOrg.name, func.count(SysUser.id).label("count"))
            .outerjoin(SysUser, and_(
                SysUser.org_id == SysOrg.id,
                SysUser.is_deleted == nd,
            ))
            .where(SysOrg.is_deleted == nd)
            .group_by(SysOrg.id, SysOrg.name)
            .order_by(func.count(SysUser.id).desc())
        )
        result = self.db.execute(stmt).all()
        return [{"name": row[0], "count": row[1]} for row in result]

    def role_category_distribution(self) -> List[dict]:
        stmt = (
            select(SysRole.category, func.count().label("count"))
            .where(SysRole.is_deleted == SoftDeleteEnum.NO)
            .group_by(SysRole.category)
            .order_by(func.count().desc())
        )
        result = self.db.execute(stmt).all()
        return [{"category": row[0], "count": row[1]} for row in result]

    def get_recent_logins(self, limit: int = 10) -> List[dict]:
        stmt = (
            select(
                SysUser.nickname,
                SysUser.account,
                SysUser.last_login_at,
                SysUser.last_login_ip,
            )
            .where(
                SysUser.is_deleted == SoftDeleteEnum.NO,
                SysUser.last_login_at.isnot(None),
            )
            .order_by(SysUser.last_login_at.desc())
            .limit(limit)
        )
        result = self.db.execute(stmt).all()
        return [
            {"nickname": row[0], "account": row[1], "last_login_at": row[2], "last_login_ip": row[3]}
            for row in result
        ]

    def count_client_users(self) -> int:
        stmt = select(func.count()).select_from(ClientUser).where(ClientUser.is_deleted == SoftDeleteEnum.NO)
        return self.db.execute(stmt).scalar() or 0

    def count_active_client_users(self) -> int:
        stmt = (
            select(func.count())
            .select_from(ClientUser)
            .where(
                ClientUser.is_deleted == SoftDeleteEnum.NO,
                ClientUser.status == UserStatusEnum.ACTIVE.value,
            )
        )
        return self.db.execute(stmt).scalar() or 0

    def client_user_trend(self, months: int = 12) -> List[dict]:
        nd = SoftDeleteEnum.NO
        month_col = func.date_format(ClientUser.created_at, '%Y-%m').label("month")
        stmt = (
            select(month_col, func.count().label("count"))
            .where(ClientUser.is_deleted == nd, ClientUser.created_at.isnot(None))
            .group_by(month_col)
            .order_by(month_col.asc())
            .limit(months)
        )
        result = self.db.execute(stmt).all()
        return [{"month": row[0], "count": row[1]} for row in result]

    def get_recent_client_logins(self, limit: int = 10) -> List[dict]:
        stmt = (
            select(
                ClientUser.nickname,
                ClientUser.account,
                ClientUser.last_login_at,
                ClientUser.last_login_ip,
            )
            .where(
                ClientUser.is_deleted == SoftDeleteEnum.NO,
                ClientUser.last_login_at.isnot(None),
            )
            .order_by(ClientUser.last_login_at.desc())
            .limit(limit)
        )
        result = self.db.execute(stmt).all()
        return [
            {"nickname": row[0], "account": row[1], "last_login_at": row[2], "last_login_ip": row[3]}
            for row in result
        ]
