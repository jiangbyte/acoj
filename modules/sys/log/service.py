from datetime import datetime, timedelta
from typing import List, Dict, Optional
from sqlalchemy import delete as sa_delete
from sqlalchemy.orm import Session
from . import SysLog
from .params import (
    LogVO, LogPageParam,
    LogDeleteByCategoryParam, LogDailyCount, LogCategoryTotal,
    LogBarChartData, LogCategorySeries, LogPieChartData,
)
from .dao import LogDao
from core.pojo import IdParam
from core.result import page_data, PageDataField


class LogService:
    def __init__(self, db: Session):
        self.dao = LogDao(db)

    def page(self, param: LogPageParam) -> dict:
        result = self.dao.find_page(param)
        records = [LogVO.model_validate(r).model_dump() for r in result[PageDataField.RECORDS]]
        return page_data(
            records=records,
            total=result[PageDataField.TOTAL],
            page=param.current,
            size=param.size,
        )

    def detail(self, param: IdParam):
        entity = self.dao.find_by_id(param.id)
        if not entity:
            return None
        return LogVO.model_validate(entity).model_dump()

    def delete_by_category(self, param: LogDeleteByCategoryParam) -> None:
        db = self.dao.db
        stmt = sa_delete(SysLog).where(SysLog.category == param.category)
        db.execute(stmt)
        db.commit()

    # ---- Chart / Statistics ----

    def _last_n_days(self, n: int = 7) -> List[str]:
        today = datetime.now().replace(hour=0, minute=0, second=0, microsecond=0)
        return [(today - timedelta(days=i)).strftime("%Y-%m-%d") for i in range(n - 1, -1, -1)]

    def vis_log_line_chart_data(self) -> LogBarChartData:
        since = datetime.now() - timedelta(days=6)
        since = since.replace(hour=0, minute=0, second=0, microsecond=0)
        rows = self.dao.daily_counts_since(["LOGIN", "LOGOUT"], since)
        days = self._last_n_days(7)

        day_map: Dict[str, Dict[str, int]] = {}
        for r in rows:
            day_str = r["day"].strftime("%Y-%m-%d") if hasattr(r["day"], "strftime") else str(r["day"])
            day_map.setdefault(day_str, {})[r["category"]] = r["count"]

        return LogBarChartData(
            days=days,
            series=[
                LogCategorySeries(name="登录", data=[day_map.get(d, {}).get("LOGIN", 0) for d in days]),
                LogCategorySeries(name="登出", data=[day_map.get(d, {}).get("LOGOUT", 0) for d in days]),
            ],
        )

    def vis_log_pie_chart_data(self) -> LogPieChartData:
        totals = self.dao.count_total_by_category(["LOGIN", "LOGOUT"])
        return LogPieChartData(data=[
            LogCategoryTotal(category="登录", total=totals.get("LOGIN", 0)),
            LogCategoryTotal(category="登出", total=totals.get("LOGOUT", 0)),
        ])

    def op_log_bar_chart_data(self) -> LogBarChartData:
        since = datetime.now() - timedelta(days=6)
        since = since.replace(hour=0, minute=0, second=0, microsecond=0)
        rows = self.dao.daily_counts_since(["OPERATE", "EXCEPTION"], since)
        days = self._last_n_days(7)

        day_map: Dict[str, Dict[str, int]] = {}
        for r in rows:
            day_str = r["day"].strftime("%Y-%m-%d") if hasattr(r["day"], "strftime") else str(r["day"])
            day_map.setdefault(day_str, {})[r["category"]] = r["count"]

        return LogBarChartData(
            days=days,
            series=[
                LogCategorySeries(name="操作", data=[day_map.get(d, {}).get("OPERATE", 0) for d in days]),
                LogCategorySeries(name="异常", data=[day_map.get(d, {}).get("EXCEPTION", 0) for d in days]),
            ],
        )

    def op_log_pie_chart_data(self) -> LogPieChartData:
        totals = self.dao.count_total_by_category(["OPERATE", "EXCEPTION"])
        return LogPieChartData(data=[
            LogCategoryTotal(category="操作", total=totals.get("OPERATE", 0)),
            LogCategoryTotal(category="异常", total=totals.get("EXCEPTION", 0)),
        ])
