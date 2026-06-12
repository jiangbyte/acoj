from datetime import datetime, timezone, timedelta
from typing import List, Optional, Tuple

from sqlalchemy import or_, select
from sqlalchemy.orm import Session as DbSession

from core.auth import HeiAuthTool, HeiClientAuthTool
from core.utils.ip_utils import get_city_info
from plugins.plugin_sys.user.models import SysUser
from plugins.plugin_sys.log.params import LogBarChartData, LogCategorySeries, LogPieChartData, LogCategoryTotal
from .params import (
    SessionAnalysisResult,
    SessionPageResult,
    SessionPageParam,
    SessionChartData,
    SessionTokenResult,
    SessionInfoToSessionPageResult,
    SessionTokenInfoToSessionTokenResult,
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


async def _search_sys_user_ids(db: DbSession, keyword: Optional[str]) -> Optional[List[str]]:
    if not keyword:
        return None
    rows = db.execute(
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


async def _load_business_session_page(
    db: DbSession,
    current: int,
    size: int,
    keyword: Optional[str] = None,
) -> Tuple[List[SessionPageResult], int]:
    candidate_user_ids = await _search_sys_user_ids(db, keyword)
    if candidate_user_ids is None:
        infos, total = await HeiAuthTool.list_session_infos(current=current, size=size)
    else:
        infos, total = await HeiAuthTool.list_session_infos_by_user_ids(candidate_user_ids, current=current, size=size)

    user_ids = [info["user_id"] for info in infos]
    user_map = {}
    if user_ids:
        rows = db.execute(select(SysUser).where(SysUser.id.in_(user_ids))).scalars().all()
        user_map = {row.id: row for row in rows}

    results = []
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

        results.append(SessionInfoToSessionPageResult(
            info,
            _format_timeout(info.get("session_timeout_seconds", 0)),
            nickname=nickname,
            avatar=avatar,
            status=status,
            last_login_ip=last_login_ip,
            last_login_address=last_login_address,
            last_login_time=last_login_time,
        ))
    return results, total


async def analysis(db: DbSession) -> SessionAnalysisResult:
    b_stats = await HeiAuthTool.get_session_stats()
    c_stats = await HeiClientAuthTool.get_session_stats()
    return SessionAnalysisResult(
        total_count=b_stats["total_count"] + c_stats["total_count"],
        max_token_count=max(b_stats["max_token_count"], c_stats["max_token_count"]),
        one_hour_newly_added=b_stats["one_hour_newly_added"] + c_stats["one_hour_newly_added"],
        proportion_of_b_and_c=f'{b_stats["total_count"]}/{c_stats["total_count"]}',
    )


async def list_b_sessions(db: DbSession, param: SessionPageParam) -> dict:
    current = max(1, param.current)
    size = max(1, param.size)
    sessions, total = await _load_business_session_page(db, current, size, param.keyword)
    return {"records": sessions, "total": total}


async def list_c_sessions(db: DbSession, param: SessionPageParam) -> dict:
    current = max(1, param.current)
    size = max(1, param.size)
    sessions, total = await HeiClientAuthTool.list_session_infos(current=current, size=size, keyword=param.keyword)
    return {"records": sessions, "total": total}


async def token_list(user_id: str, auth_tool=HeiAuthTool) -> List[SessionTokenResult]:
    token_infos = await auth_tool.get_session_tokens(user_id)
    return [
        SessionTokenInfoToSessionTokenResult(
            token_info,
            _format_timeout(token_info.get("timeout_seconds", 0)),
        )
        for token_info in token_infos
    ]


async def exit_b_session(user_id: str) -> None:
    await HeiAuthTool.kickout(user_id)


async def exit_c_session(user_id: str) -> None:
    await HeiClientAuthTool.kickout(user_id)


async def exit_b_session_token(user_id: str, token: str) -> None:
    await HeiAuthTool.kickout_token(user_id, token)


async def exit_c_session_token(user_id: str, token: str) -> None:
    await HeiClientAuthTool.kickout_token(user_id, token)


async def chart_data(db: DbSession) -> SessionChartData:
    b_stats = await HeiAuthTool.get_session_stats()
    c_stats = await HeiClientAuthTool.get_session_stats()
    today = datetime.now(timezone.utc).replace(hour=0, minute=0, second=0, microsecond=0)
    days = [(today - timedelta(days=i)).strftime("%Y-%m-%d") for i in range(6, -1, -1)]
    b_daily_map = await HeiAuthTool.get_session_daily_counts(days)
    c_daily_map = await HeiClientAuthTool.get_session_daily_counts(days)
    b_daily = [b_daily_map.get(day, 0) for day in days]
    c_daily = [c_daily_map.get(day, 0) for day in days]

    return SessionChartData(
        bar_chart=LogBarChartData(
            days=days,
            series=[
                LogCategorySeries(name="BUSINESS", data=b_daily),
                LogCategorySeries(name="CONSUMER", data=c_daily),
            ],
        ),
        pie_chart=LogPieChartData(
            data=[
                LogCategoryTotal(category="BUSINESS", total=b_stats["total_count"]),
                LogCategoryTotal(category="CONSUMER", total=c_stats["total_count"]),
            ],
        ),
    )
