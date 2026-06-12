"""Client session service — thin wrapper over auth session APIs."""

from datetime import datetime, timezone, timedelta
from typing import Dict, Any, List, Optional, Tuple

from sqlalchemy import or_, select
from sqlalchemy.orm import Session as DbSession

from sdk.auth.auth.hei_client_auth_tool import HeiClientAuthTool
from sdk.auth import HeiAuthTool
from plugins.plugin_client.user.models import ClientUser
from .params import (
    SessionAnalysisResult, SessionPageResult, SessionPageParam,
    SessionChartData, SessionTokenResult,
    BarChartData, CategorySeries, PieChartData, CategoryTotal,
    SessionInfoToSessionPageResult, SessionTokenInfoToSessionTokenResult,
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


async def _search_client_user_ids(db: DbSession, keyword: Optional[str]) -> Optional[List[str]]:
    if not keyword:
        return None
    rows = db.execute(
        select(ClientUser.id).where(
            or_(
                ClientUser.id == keyword,
                ClientUser.username.like(f"%{keyword}%"),
                ClientUser.nickname.like(f"%{keyword}%"),
            )
        )
    ).all()
    user_ids = [str(row[0]) for row in rows if row and row[0]]
    if not user_ids and keyword:
        user_ids = [keyword]
    return user_ids


async def _build_consumer_page_results(
    db: DbSession,
    current: int,
    size: int,
    keyword: Optional[str] = None,
) -> Tuple[List[Dict[str, Any]], int]:
    candidate_user_ids = await _search_client_user_ids(db, keyword)
    if candidate_user_ids is None:
        infos, total = await HeiClientAuthTool.list_session_infos(current=current, size=size)
    else:
        infos, total = await HeiClientAuthTool.list_session_infos_by_user_ids(candidate_user_ids, current=current, size=size)

    user_ids = [info["user_id"] for info in infos]
    user_map = {}
    if user_ids:
        rows = db.execute(select(ClientUser).where(ClientUser.id.in_(user_ids))).scalars().all()
        user_map = {row.id: row for row in rows}

    results: List[Dict[str, Any]] = []
    for info in infos:
        user = user_map.get(info["user_id"])
        results.append(SessionInfoToSessionPageResult(
            info,
            _format_timeout(info.get("session_timeout_seconds", 0)),
            username=user.username if user and user.username else info.get("username"),
            nickname=user.nickname if user and user.nickname else None,
            avatar=user.avatar if user and user.avatar else None,
            status=user.status if user and user.status else None,
            last_login_ip=user.last_login_ip if user and user.last_login_ip else None,
            last_login_time=user.last_login_at.strftime("%Y-%m-%d %H:%M:%S") if user and user.last_login_at else "",
        ).model_dump())
    return results, total


async def analysis(db: DbSession) -> SessionAnalysisResult:
    b_stats = await HeiAuthTool.get_session_stats()
    c_stats = await HeiClientAuthTool.get_session_stats()
    max_token_count = b_stats["max_token_count"] if b_stats["max_token_count"] > c_stats["max_token_count"] else c_stats["max_token_count"]
    return SessionAnalysisResult(
        total_count=b_stats["total_count"] + c_stats["total_count"],
        max_token_count=max_token_count,
        one_hour_newly_added=b_stats["one_hour_newly_added"] + c_stats["one_hour_newly_added"],
        proportion_of_b_and_c=f'{b_stats["total_count"]}/{c_stats["total_count"]}',
    )


async def page(db: DbSession, param: SessionPageParam) -> Dict[str, Any]:
    current = max(1, param.current)
    size = max(1, param.size)
    sessions, total = await _build_consumer_page_results(db, current, size, param.keyword)
    return {"records": sessions, "total": total}


async def exit_session(user_id: str) -> None:
    await HeiClientAuthTool.kickout(user_id)


async def token_list(user_id: str) -> List[SessionTokenResult]:
    token_infos = await HeiClientAuthTool.get_session_tokens(user_id)
    return [
        SessionTokenInfoToSessionTokenResult(
            token_info,
            _format_timeout(token_info.get("timeout_seconds", 0)),
        )
        for token_info in token_infos
    ]


async def exit_token(user_id: str, token: str) -> None:
    await HeiClientAuthTool.kickout_token(user_id, token)


async def chart_data() -> SessionChartData:
    c_stats = await HeiClientAuthTool.get_session_stats()
    days = [(datetime.now(timezone.utc) - timedelta(days=i)).strftime("%Y-%m-%d") for i in range(6, -1, -1)]
    c_daily = await HeiClientAuthTool.get_session_daily_counts(days)
    series_data = [c_daily.get(day, 0) for day in days]
    return SessionChartData(
        bar_chart=BarChartData(
            days=days,
            series=[CategorySeries(name="新增在线数", data=series_data)],
        ),
        pie_chart=PieChartData(
            data=[CategoryTotal(category="CONSUMER", total=c_stats["total_count"])],
        ),
    )
