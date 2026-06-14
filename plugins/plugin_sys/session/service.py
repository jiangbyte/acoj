from datetime import datetime, timedelta, timezone
from typing import Optional

from fastapi import Depends
from sqlalchemy import or_, select
from sqlalchemy.orm import Session

from sdk.auth import Business, Consumer, Sessions
from sdk.infra.db import get_db
from sdk.web.result import page_data
from sdk.utils.ip_utils import get_city_info

from plugins.plugin_sys.log.params import LogBarChartData, LogCategorySeries, LogCategoryTotal, LogPieChartData
from plugins.plugin_sys.user.models import SysUser
from .params import (
    SessionAnalysisResult,
    SessionChartData,
    SessionPageParam,
    SessionPageResult,
    SessionTokenResult,
)


def _format_timeout(seconds: int) -> str:
    if seconds < 0:
        return "已过期"
    if seconds == 0:
        return "永久"
    if seconds < 60:
        return f"剩余 {seconds}秒"
    if seconds < 3600:
        return f"剩余 {seconds // 60}分钟"
    if seconds < 86400:
        return f"剩余 {seconds // 3600}小时 {(seconds % 3600) // 60}分钟"
    return f"剩余 {seconds // 86400}天 {(seconds % 86400) // 3600}小时"


class SessionService:
    def __init__(self, db: Session):
        self.db = db
        self._business_sessions = Business.sessions()
        self._all_sessions = Sessions(Business, Consumer)

    async def _search_sys_user_ids(self, keyword: Optional[str]) -> Optional[list[str]]:
        if not keyword:
            return None
        rows = self.db.execute(
            select(SysUser.id).where(
                or_(
                    SysUser.id == keyword,
                    SysUser.username.like(f"%{keyword}%"),
                    SysUser.nickname.like(f"%{keyword}%"),
                )
            )
        ).all()
        user_ids = [str(row[0]) for row in rows if row and row[0]]
        if not user_ids and keyword:
            user_ids = [keyword]
        return user_ids

    async def analysis(self) -> SessionAnalysisResult:
        stats_by_realm = await self._all_sessions.stats_by_realm()
        business_stats = stats_by_realm.get(Business.id, {})
        consumer_stats = stats_by_realm.get(Consumer.id, {})
        return SessionAnalysisResult(
            total_count=business_stats["total_count"] + consumer_stats["total_count"],
            max_token_count=max(business_stats["max_token_count"], consumer_stats["max_token_count"]),
            one_hour_newly_added=business_stats["one_hour_newly_added"] + consumer_stats["one_hour_newly_added"],
            proportion_of_b_and_c=f'{business_stats["total_count"]}/{consumer_stats["total_count"]}',
        )

    async def page(self, param: SessionPageParam) -> dict:
        current = max(1, param.current)
        size = max(1, param.size)
        candidate_user_ids = await self._search_sys_user_ids(param.keyword)
        if candidate_user_ids is None:
            infos, total = await self._business_sessions.page(current=current, size=size)
        else:
            infos, total = await self._business_sessions.page_by_user_ids(
                candidate_user_ids,
                current=current,
                size=size,
            )
        user_ids = [info["user_id"] for info in infos]
        user_map = {}
        if user_ids:
            rows = self.db.execute(select(SysUser).where(SysUser.id.in_(user_ids))).scalars().all()
            user_map = {row.id: row for row in rows}
        records = []
        for info in infos:
            user = user_map.get(info["user_id"])
            nickname = info.get("nickname") or ""
            avatar = ""
            status = ""
            last_login_ip = ""
            last_login_address = ""
            last_login_time = None
            if user:
                nickname = nickname or user.nickname or ""
                avatar = user.avatar or ""
                status = user.status or ""
                last_login_ip = user.last_login_ip or ""
                last_login_time = user.last_login_at if user.last_login_at else None
                if last_login_ip:
                    last_login_address = get_city_info(last_login_ip)
            records.append(
                SessionPageResult.from_session_info(
                    info,
                    _format_timeout(info.get("session_timeout_seconds", 0)),
                    nickname=nickname,
                    avatar=avatar,
                    status=status,
                    last_login_ip=last_login_ip,
                    last_login_address=last_login_address,
                    last_login_time=last_login_time,
                )
            )
        return page_data(records, total, current, size)

    async def token_list(self, user_id: str) -> list[SessionTokenResult]:
        token_infos = await self._business_sessions.tokens(user_id)
        return [
            SessionTokenResult.from_token_info(
                token_info,
                _format_timeout(token_info.get("timeout_seconds", 0)),
            )
            for token_info in token_infos
        ]

    async def exit_session(self, user_id: str) -> None:
        await self._business_sessions.kickout_user(user_id)

    async def exit_token(self, user_id: str, token: str) -> None:
        await self._business_sessions.kickout_token(user_id, token)

    async def chart_data(self) -> SessionChartData:
        stats_by_realm = await self._all_sessions.stats_by_realm()
        business_stats = stats_by_realm.get(Business.id, {})
        consumer_stats = stats_by_realm.get(Consumer.id, {})
        today = datetime.now(timezone.utc).replace(hour=0, minute=0, second=0, microsecond=0)
        days = [(today - timedelta(days=index)).strftime("%Y-%m-%d") for index in range(6, -1, -1)]
        trend_by_realm = await self._all_sessions.trend_by_realm(days)
        business_daily_map = trend_by_realm.get(Business.id, {})
        consumer_daily_map = trend_by_realm.get(Consumer.id, {})
        business_daily = [business_daily_map.get(day, 0) for day in days]
        consumer_daily = [consumer_daily_map.get(day, 0) for day in days]
        return SessionChartData(
            bar_chart=LogBarChartData(
                days=days,
                series=[
                    LogCategorySeries(name="BUSINESS", data=business_daily),
                    LogCategorySeries(name="CONSUMER", data=consumer_daily),
                ],
            ),
            pie_chart=LogPieChartData(
                data=[
                    LogCategoryTotal(category="BUSINESS", total=business_stats["total_count"]),
                    LogCategoryTotal(category="CONSUMER", total=consumer_stats["total_count"]),
                ],
            ),
        )


def get_session_service(db: Session = Depends(get_db)) -> SessionService:
    return SessionService(db)
