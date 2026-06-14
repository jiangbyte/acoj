from typing import List

from sqlalchemy import MetaData, Table, and_, func, select
from sqlalchemy.orm import Session

from plugins.plugin_sys.user.models import SysUser
from plugins.plugin_sys.role.models import SysRole
from plugins.plugin_sys.org.models import SysOrg
from plugins.plugin_sys.config.models import SysConfig
from plugins.plugin_sys.notice.models import SysNotice
from plugins.plugin_sys.log.models import SysLog
from sdk.shared.contracts import USER_STATUS_ACTIVE
from sdk.web.exception import BusinessException
from .params import CategoryDistribution, OrgUserDistribution


CLIENT_USER_TABLE = "client_user"
TREND_TABLES = {
    SysUser.__tablename__: SysUser,
    CLIENT_USER_TABLE: CLIENT_USER_TABLE,
}


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
            .where(SysUser.status == USER_STATUS_ACTIVE)
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
        return self._count_rows(CLIENT_USER_TABLE)

    def count_active_client_users(self) -> int:
        return self._count_rows(CLIENT_USER_TABLE, status=USER_STATUS_ACTIVE)

    def client_user_trend(self, months: int = 12) -> List[dict]:
        return self.monthly_trend(CLIENT_USER_TABLE, months)

    def monthly_trend(self, table: str, months: int = 12) -> List[dict]:
        model_or_table = TREND_TABLES.get(table)
        if model_or_table is None:
            raise BusinessException("不支持的统计表", 400)

        if isinstance(model_or_table, str):
            return self._raw_monthly_trend(model_or_table, months)

        month_col = func.date_format(model_or_table.created_at, "%Y-%m").label("month")
        stmt = (
            select(month_col, func.count().label("count"))
            .where(model_or_table.created_at.isnot(None))
            .group_by(month_col)
            .order_by(month_col.asc())
            .limit(months)
        )
        result = self.db.execute(stmt).all()
        return [{"month": row[0], "count": row[1]} for row in result]

    def org_user_distribution_with_names(self) -> List[OrgUserDistribution]:
        stmt = (
            select(SysOrg.name, func.count(SysUser.id).label("count"))
            .outerjoin(SysUser, and_(SysUser.org_id == SysOrg.id))
            .group_by(SysOrg.id, SysOrg.name)
            .order_by(func.count(SysUser.id).desc())
        )
        return [
            OrgUserDistribution(name=row[0] or "未分配", count=row[1])
            for row in self.db.execute(stmt).all()
        ]

    def role_category_distribution_with_counts(self) -> List[CategoryDistribution]:
        stmt = select(SysRole.category, func.count(SysRole.id).label("count")).group_by(SysRole.category)
        return [
            CategoryDistribution(category=row[0], count=row[1])
            for row in self.db.execute(stmt).all()
        ]

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

    def _count_rows(self, table_name: str, *, status: str | None = None) -> int:
        table = self._get_table(table_name)
        stmt = select(func.count()).select_from(table)
        if status is not None and "status" in table.c:
            stmt = stmt.where(table.c.status == status)
        return self.db.execute(stmt).scalar() or 0

    def _raw_monthly_trend(self, table_name: str, months: int) -> List[dict]:
        table = self._get_table(table_name)
        month_col = func.date_format(table.c.created_at, "%Y-%m").label("month")
        stmt = (
            select(month_col, func.count().label("count"))
            .select_from(table)
            .where(table.c.created_at.isnot(None))
            .group_by(month_col)
            .order_by(month_col.asc())
            .limit(months)
        )
        result = self.db.execute(stmt).all()
        return [{"month": row[0], "count": row[1]} for row in result]

    def _get_table(self, table_name: str):
        bind = self.db.get_bind()
        return Table(table_name, MetaData(), autoload_with=bind)
