"""Client session service — class-based service with DI-friendly provider."""

from datetime import datetime, timedelta, timezone
from typing import Optional

from fastapi import Depends
from sqlalchemy import or_, select
from sqlalchemy.ext.asyncio import AsyncSession

from sdk.auth import CONSUMER_REALM_ID, get_auth_util, get_micos_session_util
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
        consumer_stats = await get_micos_session_util().get_analysis()
        return SessionAnalysisResult(
            total_count=consumer_stats.total_login_count,
            max_token_count=consumer_stats.total_token_count,
            one_hour_newly_added=consumer_stats.one_hour_new_token_count,
            proportion_of_b_and_c=f'0/{consumer_stats.total_login_count}',
        )

    async def page(self, param: SessionPageParam) -> dict:
        current = max(1, param.current)
        size = max(1, param.size)
        candidate_user_ids = await self._search_client_user_ids(param.keyword)
        if candidate_user_ids is None:
            page_result = await get_micos_session_util().page_sessions(CONSUMER_REALM_ID, current=current, size=size)
            infos = [{"user_id": item.login_id, "session_create_time": item.last_login_at.isoformat(), "session_timeout_seconds": 0, "token_count": item.token_count} for item in page_result.items]
            total = page_result.total
        else:
            sessions = []
            for user_id in candidate_user_ids:
                session = await get_micos_session_util().get_session(CONSUMER_REALM_ID, user_id)
                if session is not None:
                    sessions.append({"user_id": session.login_id, "session_create_time": session.last_login_at.isoformat(), "session_timeout_seconds": 0, "token_count": session.token_count})
            total = len(sessions)
            start = (current - 1) * size
            infos = sessions[start:start + size]
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
        await get_auth_util().kickout_login_id(CONSUMER_REALM_ID, user_id)

    async def token_list(self, user_id: str) -> list[SessionTokenResult]:
        token_infos = await get_micos_session_util().list_tokens(CONSUMER_REALM_ID, user_id)
        return [
            SessionTokenResult.from_token_info(
                {
                    "token": token_info.token,
                    "created_at": token_info.issued_at.isoformat(),
                    "timeout_seconds": int((token_info.expires_at - datetime.now(timezone.utc)).total_seconds()),
                    "device_type": (token_info.extra or {}).get("device_type"),
                    "device_id": token_info.device_id,
                },
                _format_timeout(
                    int((token_info.expires_at - datetime.now(timezone.utc)).total_seconds()),
                ),
            )
            for token_info in token_infos
        ]

    async def exit_token(self, user_id: str, token: str) -> None:
        del user_id
        await get_auth_util().revoke_token(CONSUMER_REALM_ID, token)

    async def chart_data(self) -> SessionChartData:
        consumer_stats = await get_micos_session_util().get_analysis()
        days = [(datetime.now(timezone.utc) - timedelta(days=index)).strftime("%Y-%m-%d") for index in range(6, -1, -1)]
        chart_data = await get_micos_session_util().get_chart_data(len(days))
        daily_map = dict(zip(chart_data.days, chart_data.realm_series.get(CONSUMER_REALM_ID, [])))
        series_data = [daily_map.get(day, 0) for day in days]
        return SessionChartData(
            bar_chart=BarChartData(days=days, series=[CategorySeries(name="新增在线数", data=series_data)]),
            pie_chart=PieChartData(data=[CategoryTotal(category="CONSUMER", total=consumer_stats.total_login_count)]),
        )


def get_client_session_service(db: AsyncSession = Depends(get_db)) -> ClientSessionService:
    return ClientSessionService(db)
