from typing import List
from datetime import datetime, timedelta
from sqlalchemy.orm import Session
from sqlalchemy import select, func, text
from core.enums import SoftDeleteEnum
from modules.sys.user.models import SysUser
from modules.sys.role.models import SysRole
from modules.sys.org.models import SysOrg
from modules.sys.config.models import SysConfig
from modules.sys.notice.models import SysNotice


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
            .where(SysUser.is_deleted == SoftDeleteEnum.NO, SysUser.status == 'ACTIVE')
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
        stmt = text(f"""
            SELECT DATE_FORMAT(created_at, '%Y-%m') AS month, COUNT(*) AS count
            FROM sys_user
            WHERE is_deleted = '{nd}' AND created_at IS NOT NULL
            GROUP BY month
            ORDER BY month ASC
            LIMIT :limit
        """)
        result = self.db.execute(stmt, {"limit": months})
        return [{"month": row[0], "count": row[1]} for row in result]

    def org_user_distribution(self) -> List[dict]:
        nd = SoftDeleteEnum.NO
        stmt = text(f"""
            SELECT o.name, COUNT(u.id) AS count
            FROM sys_org o
            LEFT JOIN sys_user u ON u.org_id = o.id AND u.is_deleted = '{nd}'
            WHERE o.is_deleted = '{nd}'
            GROUP BY o.id, o.name
            ORDER BY count DESC
        """)
        result = self.db.execute(stmt)
        return [{"name": row[0], "count": row[1]} for row in result]

    def role_category_distribution(self) -> List[dict]:
        nd = SoftDeleteEnum.NO
        stmt = text(f"""
            SELECT category, COUNT(*) AS count
            FROM sys_role
            WHERE is_deleted = '{nd}'
            GROUP BY category
            ORDER BY count DESC
        """)
        result = self.db.execute(stmt)
        return [{"category": row[0], "count": row[1]} for row in result]

    def get_recent_logins(self, limit: int = 10) -> List[dict]:
        nd = SoftDeleteEnum.NO
        stmt = text(f"""
            SELECT nickname, account, last_login_at, last_login_ip
            FROM sys_user
            WHERE is_deleted = '{nd}' AND last_login_at IS NOT NULL
            ORDER BY last_login_at DESC
            LIMIT :limit
        """)
        result = self.db.execute(stmt, {"limit": limit})
        return [
            {"nickname": row[0], "account": row[1], "last_login_at": row[2], "last_login_ip": row[3]}
            for row in result
        ]
