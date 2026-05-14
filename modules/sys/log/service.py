from datetime import datetime, timedelta
from typing import List, Dict
from sqlalchemy import delete as sa_delete
from sqlalchemy.orm import Session
from . import SysLog
from .params import (
    LogVO, LogPageParam, LogExportParam, LogImportParam,
    LogDeleteByCategoryParam, LogDailyCount, LogCategoryTotal,
    LogBarChartData, LogCategorySeries, LogPieChartData,
)
from .dao import LogDao
from core.db.base_service import BaseCrudService


class LogService(BaseCrudService):
    model_class = SysLog
    vo_class = LogVO
    dao_class = LogDao
    page_param_class = LogPageParam
    export_name = "操作日志数据"

    def delete_by_category(self, param: LogDeleteByCategoryParam) -> None:
        db = self.dao.db
        stmt = sa_delete(SysLog).where(SysLog.category == param.category)
        db.execute(stmt)
        db.commit()

    # ---- Chart / Statistics ----

    def _last_n_days(self, n: int = 7) -> List[str]:
        """Return a list of date strings for the last N days (including today)."""
        today = datetime.now().replace(hour=0, minute=0, second=0, microsecond=0)
        return [(today - timedelta(days=i)).strftime("%Y-%m-%d") for i in range(n - 1, -1, -1)]

    def vis_log_line_chart_data(self) -> LogBarChartData:
        """Login/Logout daily counts for the last 7 days (rendered as a line chart)."""
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
        """Login vs Logout total counts."""
        totals = self.dao.count_total_by_category(["LOGIN", "LOGOUT"])
        return LogPieChartData(data=[
            LogCategoryTotal(category="登录", total=totals.get("LOGIN", 0)),
            LogCategoryTotal(category="登出", total=totals.get("LOGOUT", 0)),
        ])

    def op_log_bar_chart_data(self) -> LogBarChartData:
        """Operation/Exception daily counts for the last 7 days."""
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
        """Operation vs Exception total counts."""
        totals = self.dao.count_total_by_category(["OPERATE", "EXCEPTION"])
        return LogPieChartData(data=[
            LogCategoryTotal(category="操作", total=totals.get("OPERATE", 0)),
            LogCategoryTotal(category="异常", total=totals.get("EXCEPTION", 0)),
        ])
