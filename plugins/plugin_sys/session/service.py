from datetime import datetime, timedelta, timezone
from typing import Optional

from fastapi import Depends
from sqlalchemy import or_, select
from sqlalchemy.ext.asyncio import AsyncSession

from sdk.auth import BUSINESS_REALM_ID, CONSUMER_REALM_ID, get_auth_util, get_micos_session_util
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
    def __init__(self, db: AsyncSession):
        self.db = db

    async def _search_sys_user_ids(self, keyword: Optional[str]) -> Optional[list[str]]:
        if not keyword:
            return None
        result = await self.db.execute(
            select(SysUser.id).where(
                or_(
                    SysUser.id == keyword,
                    SysUser.username.like(f"%{keyword}%"),
                    SysUser.nickname.like(f"%{keyword}%"),
                )
            )
        )
        rows = result.all()
        user_ids = [str(row[0]) for row in rows if row and row[0]]
        if not user_ids and keyword:
            user_ids = [keyword]
        return user_ids

    async def analysis(self) -> SessionAnalysisResult:
        business_sessions = await get_micos_session_util().list_sessions(BUSINESS_REALM_ID)
        consumer_sessions = await get_micos_session_util().list_sessions(CONSUMER_REALM_ID)
        return SessionAnalysisResult(
            total_count=len(business_sessions) + len(consumer_sessions),
            max_token_count=max([0] + [item.token_count for item in business_sessions] + [item.token_count for item in consumer_sessions]),
            one_hour_newly_added=(await get_micos_session_util().get_analysis()).one_hour_new_token_count,
            proportion_of_b_and_c=f'{len(business_sessions)}/{len(consumer_sessions)}',
        )

    async def page(self, param: SessionPageParam) -> dict:
        current = max(1, param.current)
        size = max(1, param.size)
        candidate_user_ids = await self._search_sys_user_ids(param.keyword)
        if candidate_user_ids is None:
            page_result = await get_micos_session_util().page_sessions(BUSINESS_REALM_ID, current=current, size=size)
            infos = [{"user_id": item.login_id, "nickname": (item.extra or {}).get("nickname"), "session_create_time": item.last_login_at, "session_timeout_seconds": 0, "token_count": item.token_count} for item in page_result.items]
            total = page_result.total
        else:
            sessions = []
            for user_id in candidate_user_ids:
                session = await get_micos_session_util().get_session(BUSINESS_REALM_ID, user_id)
                if session is not None:
                    sessions.append({"user_id": session.login_id, "nickname": (session.extra or {}).get("nickname"), "session_create_time": session.last_login_at, "session_timeout_seconds": 0, "token_count": session.token_count})
            total = len(sessions)
            start = (current - 1) * size
            infos = sessions[start:start + size]
        user_ids = [info["user_id"] for info in infos]
        user_map = {}
        if user_ids:
            rows = (await self.db.execute(select(SysUser).where(SysUser.id.in_(user_ids)))).scalars().all()
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
        token_infos = await get_micos_session_util().list_tokens(BUSINESS_REALM_ID, user_id)
        return [
            SessionTokenResult.from_token_info(
                {
                    "token": token_info.token,
                    "created_at": token_info.issued_at.isoformat(),
                    "timeout_seconds": int((token_info.expires_at - datetime.now(timezone.utc)).total_seconds()),
                    "device_type": (token_info.extra or {}).get("device_type"),
                    "device_id": token_info.device_id,
                },
                _format_timeout(int((token_info.expires_at - datetime.now(timezone.utc)).total_seconds())),
            )
            for token_info in token_infos
        ]

    async def exit_session(self, user_id: str) -> None:
        await get_auth_util().kickout_login_id(BUSINESS_REALM_ID, user_id)

    async def exit_token(self, user_id: str, token: str) -> None:
        del user_id
        await get_auth_util().revoke_token(BUSINESS_REALM_ID, token)

    async def chart_data(self) -> SessionChartData:
        business_sessions = await get_micos_session_util().list_sessions(BUSINESS_REALM_ID)
        consumer_sessions = await get_micos_session_util().list_sessions(CONSUMER_REALM_ID)
        today = datetime.now(timezone.utc).replace(hour=0, minute=0, second=0, microsecond=0)
        days = [(today - timedelta(days=index)).strftime("%Y-%m-%d") for index in range(6, -1, -1)]
        chart_data = await get_micos_session_util().get_chart_data(len(days))
        business_daily_map = dict(zip(chart_data.days, chart_data.realm_series.get(BUSINESS_REALM_ID, [])))
        consumer_daily_map = dict(zip(chart_data.days, chart_data.realm_series.get(CONSUMER_REALM_ID, [])))
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
                    LogCategoryTotal(category="BUSINESS", total=len(business_sessions)),
                    LogCategoryTotal(category="CONSUMER", total=len(consumer_sessions)),
                ],
            ),
        )


def get_session_service(db: AsyncSession = Depends(get_db)) -> SessionService:
    return SessionService(db)
