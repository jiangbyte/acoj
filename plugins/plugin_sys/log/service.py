"""Log service — mirrors hei-gin plugin-sys/log/service.go."""

from datetime import date, datetime, timedelta
from typing import Dict, List, Optional

from fastapi import Depends
from sqlalchemy import delete as sa_delete
from sqlalchemy.ext.asyncio import AsyncSession

from sdk.infra.db import get_db
from sdk.shared.di import ActorContext
from sdk.web.exception import BusinessException
from sdk.web.result import map_page_data

from .models import SysLog
from .params import (
    LogVO, LogPageParam,
    LogDeleteByCategoryParam,
    LogBarChartData, LogCategorySeries, LogPieChartData, LogCategoryTotal,
)
from .repository import LogRepository
from sdk.shared.types import IdParam, IdsParam


def _format_day(value: date) -> str:
    return value.strftime("%Y-%m-%d")


def _actor_user_id(actor: Optional[ActorContext]) -> Optional[str]:
    return actor.user_id if actor else None


class LogService:
    def __init__(self, repository_or_db):
        if isinstance(repository_or_db, LogRepository):
            self.repository = repository_or_db
        else:
            self.repository = LogRepository(repository_or_db)
        self.db = self.repository.db

    async def page(self, param: LogPageParam) -> dict:
        """Mirrors Go Page — keyword filters name/op_user/op_ip, order by created_at DESC."""
        return map_page_data(await self.repository.find_page(param), LogVO.model_validate, param.current, param.size)

    async def detail(self, param: IdParam) -> Optional[LogVO]:
        """Mirrors Go Detail — returns VO dict or None."""
        entity = await self.repository.find_by_id(param.id)
        if not entity:
            return None
        return LogVO.model_validate(entity)

    async def delete_by_category(self, param: LogDeleteByCategoryParam) -> None:
        """Mirrors Go DeleteByCategory."""
        db = self.repository.db
        stmt = sa_delete(SysLog).where(SysLog.category == param.category)
        await db.execute(stmt)
        await db.commit()

    # ---- Chart / Statistics ----
    # Mirrors Go's LoginBarChart, LoginPieChart, OpBarChart, OpPieChart

    def _last_n_days(self, n: int = 7) -> List[str]:
        today = datetime.now().replace(hour=0, minute=0, second=0, microsecond=0)
        return [(today - timedelta(days=i)).strftime("%Y-%m-%d") for i in range(n - 1, -1, -1)]

    async def vis_log_line_chart_data(self) -> LogBarChartData:
        """Mirrors Go LoginBarChart — LOGIN/LOGOUT daily trend last 7 days."""
        since = datetime.now() - timedelta(days=6)
        since = since.replace(hour=0, minute=0, second=0, microsecond=0)
        rows = await self.repository.daily_counts_since(["LOGIN", "LOGOUT"], since)
        days = self._last_n_days(7)

        day_map: Dict[str, Dict[str, int]] = {}
        for r in rows:
            day_str = _format_day(r["day"])
            day_map.setdefault(day_str, {})[r["category"]] = r["count"]

        return LogBarChartData(
            days=days,
            series=[
                LogCategorySeries(name="登录", data=[day_map.get(d, {}).get("LOGIN", 0) for d in days]),
                LogCategorySeries(name="登出", data=[day_map.get(d, {}).get("LOGOUT", 0) for d in days]),
            ],
        )

    async def vis_log_pie_chart_data(self) -> LogPieChartData:
        """Mirrors Go LoginPieChart — LOGIN/LOGOUT total proportion."""
        totals = await self.repository.count_total_by_category(["LOGIN", "LOGOUT"])
        return LogPieChartData(data=[
            LogCategoryTotal(category="登录", total=totals.get("LOGIN", 0)),
            LogCategoryTotal(category="登出", total=totals.get("LOGOUT", 0)),
        ])

    async def op_log_bar_chart_data(self) -> LogBarChartData:
        """Mirrors Go OpBarChart — OPERATE/EXCEPTION daily trend last 7 days."""
        since = datetime.now() - timedelta(days=6)
        since = since.replace(hour=0, minute=0, second=0, microsecond=0)
        rows = await self.repository.daily_counts_since(["OPERATE", "EXCEPTION"], since)
        days = self._last_n_days(7)

        day_map: Dict[str, Dict[str, int]] = {}
        for r in rows:
            day_str = _format_day(r["day"])
            day_map.setdefault(day_str, {})[r["category"]] = r["count"]

        return LogBarChartData(
            days=days,
            series=[
                LogCategorySeries(name="操作", data=[day_map.get(d, {}).get("OPERATE", 0) for d in days]),
                LogCategorySeries(name="异常", data=[day_map.get(d, {}).get("EXCEPTION", 0) for d in days]),
            ],
        )

    async def op_log_pie_chart_data(self) -> LogPieChartData:
        """Mirrors Go OpPieChart — OPERATE/EXCEPTION total proportion."""
        totals = await self.repository.count_total_by_category(["OPERATE", "EXCEPTION"])
        return LogPieChartData(data=[
            LogCategoryTotal(category="操作", total=totals.get("OPERATE", 0)),
            LogCategoryTotal(category="异常", total=totals.get("EXCEPTION", 0)),
        ])

    async def create(self, vo: LogVO, actor: Optional[ActorContext] = None) -> None:
        """Mirrors Go Create()."""
        from sdk.utils import generate_id

        now = datetime.now()
        actor_user_id = _actor_user_id(actor)
        entity = SysLog(
            id=generate_id(),
            category=vo.category,
            name=vo.name,
            exe_status=vo.exe_status,
            exe_message=vo.exe_message,
            op_ip=vo.op_ip,
            op_address=vo.op_address,
            created_at=now,
            updated_at=now,
            created_by=actor_user_id,
            updated_by=actor_user_id,
        )
        await self.repository.insert(entity)

    async def modify(self, vo: LogVO, actor: Optional[ActorContext] = None) -> None:
        """Mirrors Go Modify()."""
        entity = await self.repository.find_by_id(vo.id)
        if not entity:
            raise BusinessException("数据不存在")
        now = datetime.now()
        actor_user_id = _actor_user_id(actor)
        up = {
            "updated_at": now,
            "category": vo.category,
            "name": vo.name,
            "exe_status": vo.exe_status,
        }
        if actor_user_id:
            up["updated_by"] = actor_user_id
        from sqlalchemy import update as sa_update

        await self.db.execute(sa_update(SysLog).where(SysLog.id == vo.id).values(**up))
        await self.db.commit()

    async def remove(self, param: IdsParam) -> None:
        """Mirrors Go Remove()."""
        if not param.ids:
            return
        from sqlalchemy import delete as sa_delete
        await self.db.execute(sa_delete(SysLog).where(SysLog.id.in_(param.ids)))
        await self.db.commit()


def get_log_service(db: AsyncSession = Depends(get_db)) -> LogService:
    return LogService(LogRepository(db))
