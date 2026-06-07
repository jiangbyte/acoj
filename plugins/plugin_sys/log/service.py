"""Log service — mirrors hei-gin plugin-sys/log/service.go."""

from typing import Optional, List, Dict
from fastapi import Request
from datetime import datetime, timedelta
from sqlalchemy import delete as sa_delete
from sqlalchemy.orm import Session
from .models import SysLog
from .params import (
    LogVO, LogPageParam,
    LogDeleteByCategoryParam,
    LogBarChartData, LogCategorySeries, LogPieChartData, LogCategoryTotal,
)
from .dao import LogDao
from core.pojo import IdParam
from core.result import page_data, PageDataField


def _to_vo(entity: SysLog) -> dict:
    """Mirrors Go's entToVO — maps all fields with formatted datetimes."""
    vo = {"id": entity.id}
    if entity.category:
        vo["category"] = entity.category
    if entity.name:
        vo["name"] = entity.name
    if entity.exe_status:
        vo["exe_status"] = entity.exe_status
    if entity.exe_message:
        vo["exe_message"] = entity.exe_message
    if entity.op_ip:
        vo["op_ip"] = entity.op_ip
    if entity.op_address:
        vo["op_address"] = entity.op_address
    if entity.op_browser:
        vo["op_browser"] = entity.op_browser
    if entity.op_os:
        vo["op_os"] = entity.op_os
    if entity.class_name:
        vo["class_name"] = entity.class_name
    if entity.method_name:
        vo["method_name"] = entity.method_name
    if entity.req_method:
        vo["req_method"] = entity.req_method
    if entity.req_url:
        vo["req_url"] = entity.req_url
    if entity.param_json:
        vo["param_json"] = entity.param_json
    if entity.result_json:
        vo["result_json"] = entity.result_json
    if entity.trace_id:
        vo["trace_id"] = entity.trace_id
    if entity.op_user:
        vo["op_user"] = entity.op_user
    if entity.sign_data:
        vo["sign_data"] = entity.sign_data
    if entity.created_by:
        vo["created_by"] = entity.created_by
    if entity.updated_by:
        vo["updated_by"] = entity.updated_by
    if entity.op_time:
        vo["op_time"] = entity.op_time.strftime("%Y-%m-%d %H:%M:%S")
    if entity.created_at:
        vo["created_at"] = entity.created_at.strftime("%Y-%m-%d %H:%M:%S")
    if entity.updated_at:
        vo["updated_at"] = entity.updated_at.strftime("%Y-%m-%d %H:%M:%S")
    return vo


class LogService:
    def __init__(self, db: Session):
        self.dao = LogDao(db)

    def page(self, param: LogPageParam) -> dict:
        """Mirrors Go Page — keyword filters name/op_user/op_ip, order by created_at DESC."""
        result = self.dao.find_page(param)
        records = [_to_vo(r) for r in result.get("records", [])]
        return page_data(records=records, total=result.get("total", 0),
                         page=param.current, size=param.size)

    def detail(self, param: IdParam) -> Optional[dict]:
        """Mirrors Go Detail — returns VO dict or None."""
        entity = self.dao.find_by_id(param.id)
        if not entity:
            return None
        return _to_vo(entity)

    def delete_by_category(self, param: LogDeleteByCategoryParam) -> None:
        """Mirrors Go DeleteByCategory."""
        db = self.dao.db
        stmt = sa_delete(SysLog).where(SysLog.category == param.category)
        db.execute(stmt)
        db.commit()

    # ---- Chart / Statistics ----
    # Mirrors Go's LoginBarChart, LoginPieChart, OpBarChart, OpPieChart

    def _last_n_days(self, n: int = 7) -> List[str]:
        today = datetime.now().replace(hour=0, minute=0, second=0, microsecond=0)
        return [(today - timedelta(days=i)).strftime("%Y-%m-%d") for i in range(n - 1, -1, -1)]

    def vis_log_line_chart_data(self) -> LogBarChartData:
        """Mirrors Go LoginBarChart — LOGIN/LOGOUT daily trend last 7 days."""
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
        """Mirrors Go LoginPieChart — LOGIN/LOGOUT total proportion."""
        totals = self.dao.count_total_by_category(["LOGIN", "LOGOUT"])
        return LogPieChartData(data=[
            LogCategoryTotal(category="登录", total=totals.get("LOGIN", 0)),
            LogCategoryTotal(category="登出", total=totals.get("LOGOUT", 0)),
        ])

    def op_log_bar_chart_data(self) -> LogBarChartData:
        """Mirrors Go OpBarChart — OPERATE/EXCEPTION daily trend last 7 days."""
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
        """Mirrors Go OpPieChart — OPERATE/EXCEPTION total proportion."""
        totals = self.dao.count_total_by_category(["OPERATE", "EXCEPTION"])
        return LogPieChartData(data=[
            LogCategoryTotal(category="操作", total=totals.get("OPERATE", 0)),
            LogCategoryTotal(category="异常", total=totals.get("EXCEPTION", 0)),
        ])

    async def create(self, vo: LogVO, request: Optional[Request] = None) -> None:
        """Mirrors Go Create()."""
        from core.utils import generate_id
        from core.auth import HeiAuthTool
        now = datetime.now()
        user_id = None
        try:
            user_id = await HeiAuthTool.getLoginIdDefaultNull(request)
        except Exception:
            pass
        entity = SysLog(
            id=generate_id(),
            created_at=now,
            updated_at=now,
        )
        if vo.category:
            entity.category = vo.category
        if vo.name:
            entity.name = vo.name
        if vo.exe_status:
            entity.exe_status = vo.exe_status
        if vo.exe_message:
            entity.exe_message = vo.exe_message
        if vo.op_ip:
            entity.op_ip = vo.op_ip
        if vo.op_address:
            entity.op_address = vo.op_address
        if user_id:
            entity.created_by = user_id
            entity.updated_by = user_id
        self.dao.db.add(entity)
        self.dao.db.commit()

    async def modify(self, vo: LogVO, request: Optional[Request] = None) -> None:
        """Mirrors Go Modify()."""
        from core.auth import HeiAuthTool
        entity = self.dao.find_by_id(vo.id)
        if not entity:
            from core.exception import BusinessException
            raise BusinessException("数据不存在")
        user_id = None
        try:
            user_id = await HeiAuthTool.getLoginIdDefaultNull(request)
        except Exception:
            pass
        now = datetime.now()
        up = {"updated_at": now}
        if vo.category:
            up["category"] = vo.category
        if vo.name:
            up["name"] = vo.name
        if vo.exe_status:
            up["exe_status"] = vo.exe_status
        if user_id:
            up["updated_by"] = user_id
        from sqlalchemy import update as sa_update
        self.dao.db.execute(sa_update(SysLog).where(SysLog.id == vo.id).values(**up))
        self.dao.db.commit()

    def remove(self, param) -> None:
        """Mirrors Go Remove()."""
        ids = param.ids if hasattr(param, 'ids') else param
        if not ids:
            return
        from sqlalchemy import delete as sa_delete
        self.dao.db.execute(sa_delete(SysLog).where(SysLog.id.in_(ids)))
        self.dao.db.commit()
