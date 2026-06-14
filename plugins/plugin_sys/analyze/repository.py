from typing import List

from sqlalchemy import MetaData, Table, and_, func, select
from sqlalchemy.ext.asyncio import AsyncSession

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

# Reflected tables are schema-static; cache them so we reflect at most once per
# table for the process lifetime instead of on every request.
_reflected_tables: dict[str, Table] = {}


class AnalyzeRepository:
    def __init__(self, db: AsyncSession):
        self.db = db

    async def count_users(self) -> int:
        stmt = select(func.count()).select_from(SysUser)
        return (await self.db.execute(stmt)).scalar() or 0

    async def count_active_users(self) -> int:
        stmt = (
            select(func.count())
            .select_from(SysUser)
            .where(SysUser.status == USER_STATUS_ACTIVE)
        )
        return (await self.db.execute(stmt)).scalar() or 0

    async def count_roles(self) -> int:
        stmt = select(func.count()).select_from(SysRole)
        return (await self.db.execute(stmt)).scalar() or 0

    async def count_orgs(self) -> int:
        stmt = select(func.count()).select_from(SysOrg)
        return (await self.db.execute(stmt)).scalar() or 0

    async def count_configs(self) -> int:
        stmt = select(func.count()).select_from(SysConfig)
        return (await self.db.execute(stmt)).scalar() or 0

    async def count_notices(self) -> int:
        stmt = select(func.count()).select_from(SysNotice)
        return (await self.db.execute(stmt)).scalar() or 0

    async def user_trend(self, months: int = 12) -> List[dict]:
        month_col = func.date_format(SysUser.created_at, '%Y-%m').label("month")
        stmt = (
            select(month_col, func.count().label("count"))
            .where(SysUser.created_at.isnot(None))
            .group_by(month_col)
            .order_by(month_col.asc())
            .limit(months)
        )
        result = (await self.db.execute(stmt)).all()
        return [{"month": row[0], "count": row[1]} for row in result]

    async def org_user_distribution(self) -> List[dict]:
        stmt = (
            select(SysOrg.name, func.count(SysUser.id).label("count"))
            .outerjoin(SysUser, and_(
                SysUser.org_id == SysOrg.id,
            ))
            .group_by(SysOrg.id, SysOrg.name)
            .order_by(func.count(SysUser.id).desc())
        )
        result = (await self.db.execute(stmt)).all()
        return [{"name": row[0], "count": row[1]} for row in result]

    async def role_category_distribution(self) -> List[dict]:
        stmt = (
            select(SysRole.category, func.count().label("count"))
            .group_by(SysRole.category)
            .order_by(func.count().desc())
        )
        result = (await self.db.execute(stmt)).all()
        return [{"category": row[0], "count": row[1]} for row in result]

    async def count_client_users(self) -> int:
        return await self._count_rows(CLIENT_USER_TABLE)

    async def count_active_client_users(self) -> int:
        return await self._count_rows(CLIENT_USER_TABLE, status=USER_STATUS_ACTIVE)

    async def client_user_trend(self, months: int = 12) -> List[dict]:
        return await self.monthly_trend(CLIENT_USER_TABLE, months)

    async def monthly_trend(self, table: str, months: int = 12) -> List[dict]:
        model_or_table = TREND_TABLES.get(table)
        if model_or_table is None:
            raise BusinessException("不支持的统计表", 400)

        if isinstance(model_or_table, str):
            return await self._raw_monthly_trend(model_or_table, months)

        month_col = func.date_format(model_or_table.created_at, "%Y-%m").label("month")
        stmt = (
            select(month_col, func.count().label("count"))
            .where(model_or_table.created_at.isnot(None))
            .group_by(month_col)
            .order_by(month_col.asc())
            .limit(months)
        )
        result = (await self.db.execute(stmt)).all()
        return [{"month": row[0], "count": row[1]} for row in result]

    async def org_user_distribution_with_names(self) -> List[OrgUserDistribution]:
        stmt = (
            select(SysOrg.name, func.count(SysUser.id).label("count"))
            .outerjoin(SysUser, and_(SysUser.org_id == SysOrg.id))
            .group_by(SysOrg.id, SysOrg.name)
            .order_by(func.count(SysUser.id).desc())
        )
        return [
            OrgUserDistribution(name=row[0] or "未分配", count=row[1])
            for row in (await self.db.execute(stmt)).all()
        ]

    async def role_category_distribution_with_counts(self) -> List[CategoryDistribution]:
        stmt = select(SysRole.category, func.count(SysRole.id).label("count")).group_by(SysRole.category)
        return [
            CategoryDistribution(category=row[0], count=row[1])
            for row in (await self.db.execute(stmt)).all()
        ]

    async def login_stats(self) -> dict:
        total = (await self.db.execute(
            select(func.count(SysLog.id)).where(SysLog.category == "LOGIN")
        )).scalar() or 0
        failed = (await self.db.execute(
            select(func.count(SysLog.id)).where(
                SysLog.category == "LOGIN",
                SysLog.exe_status == "FAIL",
            )
        )).scalar() or 0
        today = (await self.db.execute(
            select(func.count(SysLog.id)).where(
                SysLog.category == "LOGIN",
                func.date(SysLog.op_time) == func.curdate(),
            )
        )).scalar() or 0
        return {"login_total": total, "login_failed": failed, "login_today": today}

    async def log_stats(self) -> dict:
        total = (await self.db.execute(select(func.count(SysLog.id)))).scalar() or 0
        exception_total = (await self.db.execute(
            select(func.count(SysLog.id)).where(SysLog.category == "EXCEPTION")
        )).scalar() or 0
        exception_today = (await self.db.execute(
            select(func.count(SysLog.id)).where(
                SysLog.category == "EXCEPTION",
                func.date(SysLog.op_time) == func.curdate(),
            )
        )).scalar() or 0
        return {
            "log_total": total,
            "log_exception": exception_total,
            "exception_today": exception_today,
        }

    async def _count_rows(self, table_name: str, *, status: str | None = None) -> int:
        table = await self._get_table(table_name)
        stmt = select(func.count()).select_from(table)
        if status is not None and "status" in table.c:
            stmt = stmt.where(table.c.status == status)
        return (await self.db.execute(stmt)).scalar() or 0

    async def _raw_monthly_trend(self, table_name: str, months: int) -> List[dict]:
        table = await self._get_table(table_name)
        month_col = func.date_format(table.c.created_at, "%Y-%m").label("month")
        stmt = (
            select(month_col, func.count().label("count"))
            .select_from(table)
            .where(table.c.created_at.isnot(None))
            .group_by(month_col)
            .order_by(month_col.asc())
            .limit(months)
        )
        result = (await self.db.execute(stmt)).all()
        return [{"month": row[0], "count": row[1]} for row in result]

    async def _get_table(self, table_name: str) -> Table:
        cached = _reflected_tables.get(table_name)
        if cached is not None:
            return cached

        # Reflection is a synchronous SQLAlchemy operation; drive it through the
        # async connection's run_sync so it executes on the underlying sync
        # DBAPI connection without blocking the event loop.
        conn = await self.db.connection()
        table = await conn.run_sync(
            lambda sync_conn: Table(table_name, MetaData(), autoload_with=sync_conn)
        )
        _reflected_tables[table_name] = table
        return table
