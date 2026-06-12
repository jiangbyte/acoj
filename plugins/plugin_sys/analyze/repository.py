from typing import List
from datetime import datetime
from sqlalchemy.orm import Session
from sqlalchemy import select, func, and_, text
from sdk.enums import UserStatusEnum
from plugins.plugin_sys.user.models import SysUser
from plugins.plugin_sys.role.models import SysRole
from plugins.plugin_sys.org.models import SysOrg
from plugins.plugin_sys.config.models import SysConfig
from plugins.plugin_sys.notice.models import SysNotice
from plugins.plugin_client.user.models import ClientUser
from plugins.plugin_sys.log.models import SysLog


class AnalyzeRepository:
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

    def monthly_trend(self, table: str, months: int = 12) -> List[dict]:
        rows = self.db.execute(
            text(
                f"SELECT DATE_FORMAT(created_at, '%Y-%m') AS month, COUNT(*) AS count "
                f"FROM {table} WHERE created_at IS NOT NULL "
                f"GROUP BY month ORDER BY month ASC LIMIT {months}"
            )
        ).fetchall()
        return [{"month": row[0], "count": row[1]} for row in rows]

    def org_user_distribution_with_names(self) -> List[dict]:
        stmt = (
            select(SysOrg.name, func.count(SysUser.id).label("count"))
            .outerjoin(SysUser, and_(SysUser.org_id == SysOrg.id))
            .group_by(SysOrg.id, SysOrg.name)
            .order_by(func.count(SysUser.id).desc())
        )
        return [{"name": row[0] or "未分配", "count": row[1]} for row in self.db.execute(stmt).all()]

    def role_category_distribution_with_counts(self) -> List[dict]:
        stmt = select(SysRole.category, func.count(SysRole.id).label("count")).group_by(SysRole.category)
        return [{"category": row[0], "count": row[1]} for row in self.db.execute(stmt).all()]

    def login_stats(self) -> dict:
        total = self.db.execute(
            select(func.count(SysLog.id)).where(SysLog.category == "LOGIN")
        ).scalar() or 0
        failed = self.db.execute(
            select(func.count(SysLog.id)).where(
                SysLog.category == "LOGIN",
                SysLog.exe_status == "FAIL",
            )
        ).scalar() or 0
        today = self.db.execute(
            select(func.count(SysLog.id)).where(
                SysLog.category == "LOGIN",
                func.date(SysLog.op_time) == func.curdate(),
            )
        ).scalar() or 0
        return {"login_total": total, "login_failed": failed, "login_today": today}

    def log_stats(self) -> dict:
        total = self.db.execute(select(func.count(SysLog.id))).scalar() or 0
        exception_total = self.db.execute(
            select(func.count(SysLog.id)).where(SysLog.category == "EXCEPTION")
        ).scalar() or 0
        exception_today = self.db.execute(
            select(func.count(SysLog.id)).where(
                SysLog.category == "EXCEPTION",
                func.date(SysLog.op_time) == func.curdate(),
            )
        ).scalar() or 0
        return {
            "log_total": total,
            "log_exception": exception_total,
            "exception_today": exception_today,
        }
