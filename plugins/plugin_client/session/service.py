"""Client session service — class-based service with DI-friendly provider."""

from datetime import datetime, timedelta, timezone
from typing import Optional

from fastapi import Depends
from sqlalchemy import or_, select
from sqlalchemy.ext.asyncio import AsyncSession

from sdk.auth import Consumer, Sessions
from sdk.infra.db import get_db
from sdk.web.result import page_data

from plugins.plugin_client.user.models import ClientUser
from .params import (
    BarChartData,
    CategorySeries,
    CategoryTotal,
    PieChartData,
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
        return f"剩余 {seconds // 3600}小时{(seconds % 3600) // 60}分钟"
    return f"剩余 {seconds // 86400}天{(seconds % 86400) // 3600}小时"


class ClientSessionService:
    def __init__(self, db: AsyncSession):
        self.db = db
        self._consumer_realm_sessions = Consumer.sessions()
        self._consumer_sessions = Sessions(Consumer)

    async def _search_client_user_ids(self, keyword: Optional[str]) -> Optional[list[str]]:
        if not keyword:
            return None
        result = await self.db.execute(
            select(ClientUser.id).where(
                or_(
                    ClientUser.id == keyword,
                    ClientUser.username.like(f"%{keyword}%"),
                    ClientUser.nickname.like(f"%{keyword}%"),
                )
            )
        )
        rows = result.all()
        user_ids = [str(row[0]) for row in rows if row and row[0]]
        if not user_ids and keyword:
            user_ids = [keyword]
        return user_ids

    async def analysis(self) -> SessionAnalysisResult:
        consumer_stats = (await self._consumer_sessions.stats_by_realm()).get(Consumer.id, {})
        return SessionAnalysisResult(
            total_count=consumer_stats["total_count"],
            max_token_count=consumer_stats["max_token_count"],
            one_hour_newly_added=consumer_stats["one_hour_newly_added"],
            proportion_of_b_and_c=f'0/{consumer_stats["total_count"]}',
        )

    async def page(self, param: SessionPageParam) -> dict:
        current = max(1, param.current)
        size = max(1, param.size)
        candidate_user_ids = await self._search_client_user_ids(param.keyword)
        if candidate_user_ids is None:
            infos, total = await self._consumer_realm_sessions.page(current=current, size=size)
        else:
            infos, total = await self._consumer_realm_sessions.page_by_user_ids(
                candidate_user_ids,
                current=current,
                size=size,
            )
        user_ids = [info["user_id"] for info in infos]
        user_map = {}
        if user_ids:
            rows = (await self.db.execute(select(ClientUser).where(ClientUser.id.in_(user_ids)))).scalars().all()
            user_map = {row.id: row for row in rows}
        records = []
        for info in infos:
            user = user_map.get(info["user_id"])
            username = info.get("username")
            nickname = None
            avatar = None
            status = None
            last_login_ip = None
            last_login_time = ""
            if user:
                username = user.username or username
                nickname = user.nickname
                avatar = user.avatar
                status = user.status
                last_login_ip = user.last_login_ip
                if user.last_login_at:
                    last_login_time = user.last_login_at.strftime("%Y-%m-%d %H:%M:%S")
            records.append(
                SessionPageResult.from_session_info(
                    info,
                    _format_timeout(info.get("session_timeout_seconds", 0)),
                    username=username,
                    nickname=nickname,
                    avatar=avatar,
                    status=status,
                    last_login_ip=last_login_ip,
                    last_login_time=last_login_time,
                )
            )
        return page_data(records, total, current, size)

    async def exit_session(self, user_id: str) -> None:
        await self._consumer_realm_sessions.kickout_user(user_id)

    async def token_list(self, user_id: str) -> list[SessionTokenResult]:
        token_infos = await self._consumer_realm_sessions.tokens(user_id)
        return [
            SessionTokenResult.from_token_info(
                token_info,
                _format_timeout(
                    token_info.get("timeout_seconds", 0),
                ),
            )
            for token_info in token_infos
        ]

    async def exit_token(self, user_id: str, token: str) -> None:
        await self._consumer_realm_sessions.kickout_token(user_id, token)

    async def chart_data(self) -> SessionChartData:
        consumer_stats = (await self._consumer_sessions.stats_by_realm()).get(Consumer.id, {})
        days = [(datetime.now(timezone.utc) - timedelta(days=index)).strftime("%Y-%m-%d") for index in range(6, -1, -1)]
        daily_map = (await self._consumer_sessions.trend_by_realm(days)).get(Consumer.id, {})
        series_data = [daily_map.get(day, 0) for day in days]
        return SessionChartData(
            bar_chart=BarChartData(days=days, series=[CategorySeries(name="新增在线数", data=series_data)]),
            pie_chart=PieChartData(data=[CategoryTotal(category="CONSUMER", total=consumer_stats["total_count"])]),
        )


def get_client_session_service(db: AsyncSession = Depends(get_db)) -> ClientSessionService:
    return ClientSessionService(db)
