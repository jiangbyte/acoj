import json
import logging
from datetime import datetime, timezone, timedelta
from typing import Optional, List
from sqlalchemy.orm import Session as DbSession
from core.db.redis import get_client
from core.constants import SESSION_PREFIX_LOGIN, TOKEN_PREFIX_LOGIN, SESSION_PREFIX_CLIENT, TOKEN_PREFIX_CLIENT
from core.auth import HeiAuthTool
from core.auth.auth.hei_client_auth_tool import HeiClientAuthTool
from core.utils.ip_utils import get_city_info
from modules.sys.user.models import SysUser
from .params import SessionAnalysisResult, SessionPageResult, SessionPageParam, SessionChartData
from modules.sys.log.params import LogBarChartData, LogCategorySeries, LogPieChartData, LogCategoryTotal

logger = logging.getLogger(__name__)


async def _scan_keys(pattern: str) -> List[str]:
    """SCAN Redis keys matching a pattern."""
    redis_client = get_client()
    if not redis_client:
        return []
    cursor = 0
    keys = []
    while True:
        cursor, batch = await redis_client.scan(cursor=cursor, match=pattern, count=200)
        keys.extend(batch)
        if cursor == 0:
            break
    return keys


def _format_timeout(seconds: int) -> str:
    """Format remaining seconds to human-readable string."""
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


async def _collect_sessions(
    session_prefix: str,
    token_prefix: str,
    db: DbSession,
    keyword: Optional[str] = None,
) -> List[SessionPageResult]:
    """Collect online sessions from Redis, enriched with DB user info."""
    redis_client = get_client()
    if not redis_client:
        return []

    session_keys = await _scan_keys(f"{session_prefix}*")
    sessions = []

    for key in session_keys:
        user_id = key.split(":")[-1]
        token = await redis_client.get(key)
        if not token:
            continue

        token_key = f"{token_prefix}{token}"
        token_data_str = await redis_client.get(token_key)
        if not token_data_str:
            continue

        try:
            token_data = json.loads(token_data_str)
        except json.JSONDecodeError:
            continue

        # Filter by keyword (account/nickname)
        extra = token_data.get("extra", {})
        account = extra.get("account", "")
        if keyword and keyword.lower() not in account.lower():
            continue

        ttl = await redis_client.ttl(token_key)
        created_at = token_data.get("created_at", "")

        # Lookup user info from DB
        nickname = extra.get("nickname", "")
        avatar = ""
        status = ""
        last_login_ip = ""
        last_login_address = ""
        last_login_time = None
        user = db.query(SysUser).filter(SysUser.id == user_id).first()
        if user:
            nickname = nickname or user.nickname or ""
            avatar = user.avatar or ""
            status = user.status or ""
            last_login_ip = user.last_login_ip or ""
            last_login_time = user.last_login_at
            if last_login_ip:
                last_login_address = get_city_info(last_login_ip)

        sessions.append(SessionPageResult(
            user_id=user_id,
            account=account,
            nickname=nickname,
            avatar=avatar,
            status=status,
            last_login_ip=last_login_ip,
            last_login_address=last_login_address,
            last_login_time=last_login_time,
            session_create_time=created_at,
            session_timeout=_format_timeout(ttl),
            session_timeout_seconds=max(0, ttl),
            token_count=1,
        ))

    # Sort by session create time desc
    sessions.sort(key=lambda s: s.session_create_time or "", reverse=True)
    return sessions


async def analysis(db: DbSession) -> SessionAnalysisResult:
    """Session statistics across B and C ends."""
    redis_client = get_client()
    if not redis_client:
        return SessionAnalysisResult()

    b_keys = await _scan_keys(f"{SESSION_PREFIX_LOGIN}*")
    c_keys = await _scan_keys(f"{SESSION_PREFIX_CLIENT}*")

    b_total = len(b_keys)
    c_total = len(c_keys)

    # Count sessions created in the last hour
    one_hour_ago = datetime.now(timezone.utc) - timedelta(hours=1)
    b_new = 0
    c_new = 0
    max_tokens = 1

    for key in b_keys:
        user_id = key.split(":")[-1]
        token = await redis_client.get(key)
        if not token:
            continue
        token_data_str = await redis_client.get(f"{TOKEN_PREFIX_LOGIN}{token}")
        if token_data_str:
            try:
                td = json.loads(token_data_str)
                created = td.get("created_at", "")
                if created:
                    created_dt = datetime.fromisoformat(created).replace(tzinfo=timezone.utc)
                    if created_dt > one_hour_ago:
                        b_new += 1
            except (json.JSONDecodeError, ValueError):
                pass

    for key in c_keys:
        user_id = key.split(":")[-1]
        token = await redis_client.get(key)
        if not token:
            continue
        token_data_str = await redis_client.get(f"{TOKEN_PREFIX_CLIENT}{token}")
        if token_data_str:
            try:
                td = json.loads(token_data_str)
                created = td.get("created_at", "")
                if created:
                    created_dt = datetime.fromisoformat(created).replace(tzinfo=timezone.utc)
                    if created_dt > one_hour_ago:
                        c_new += 1
            except (json.JSONDecodeError, ValueError):
                pass

    return SessionAnalysisResult(
        total_count=b_total,
        max_token_count=max_tokens,
        one_hour_newly_added=b_new + c_new,
        proportion_of_b_and_c=f"{b_total}/{c_total}",
    )


async def list_b_sessions(db: DbSession, param: SessionPageParam) -> dict:
    """Paginated B-end online session list."""
    sessions = await _collect_sessions(SESSION_PREFIX_LOGIN, TOKEN_PREFIX_LOGIN, db, param.keyword)

    total = len(sessions)
    current = max(1, param.current)
    size = max(1, param.size)
    offset = (current - 1) * size
    page_sessions = sessions[offset:offset + size]

    return {"records": page_sessions, "total": total}


async def list_c_sessions(db: DbSession, param: SessionPageParam) -> dict:
    """Paginated C-end online session list."""
    sessions = await _collect_sessions(SESSION_PREFIX_CLIENT, TOKEN_PREFIX_CLIENT, db, param.keyword)

    total = len(sessions)
    current = max(1, param.current)
    size = max(1, param.size)
    offset = (current - 1) * size
    page_sessions = sessions[offset:offset + size]

    return {"records": page_sessions, "total": total}


async def exit_b_session(user_id: str) -> None:
    """Force logout a B-end user session."""
    await HeiAuthTool.kickout(user_id)


async def exit_c_session(user_id: str) -> None:
    """Force logout a C-end user session."""
    await HeiClientAuthTool.kickout(user_id)


async def chart_data(db: DbSession) -> SessionChartData:
    """Session chart data: B/C 7-day trend and proportion."""
    redis_client = get_client()
    if not redis_client:
        return SessionChartData()

    b_keys = await _scan_keys(f"{SESSION_PREFIX_LOGIN}*")
    c_keys = await _scan_keys(f"{SESSION_PREFIX_CLIENT}*")

    # --- Pie chart: B vs C total ---
    pie_data = [
        LogCategoryTotal(category="B端", total=len(b_keys)),
        LogCategoryTotal(category="C端", total=len(c_keys)),
    ]

    # --- Bar chart: last 7 days daily new sessions ---
    today = datetime.now(timezone.utc).replace(hour=0, minute=0, second=0, microsecond=0)
    days = [(today - timedelta(days=i)).strftime("%Y-%m-%d") for i in range(6, -1, -1)]
    b_daily = [0] * 7
    c_daily = [0] * 7

    async def _count_daily(keys, token_prefix, daily):
        for key in keys:
            token = await redis_client.get(key)
            if not token:
                continue
            token_data_str = await redis_client.get(f"{token_prefix}{token}")
            if not token_data_str:
                continue
            try:
                td = json.loads(token_data_str)
                created = td.get("created_at", "")
                if created:
                    created_dt = datetime.fromisoformat(created).replace(tzinfo=timezone.utc)
                    day_start = created_dt.replace(hour=0, minute=0, second=0, microsecond=0)
                    delta = (today - day_start).days
                    if 0 <= delta < 7:
                        daily[6 - delta] += 1
            except (json.JSONDecodeError, ValueError):
                pass

    await _count_daily(b_keys, TOKEN_PREFIX_LOGIN, b_daily)
    await _count_daily(c_keys, TOKEN_PREFIX_CLIENT, c_daily)

    bar_chart = LogBarChartData(
        days=days,
        series=[
            LogCategorySeries(name="B端", data=b_daily),
            LogCategorySeries(name="C端", data=c_daily),
        ],
    )

    return SessionChartData(
        bar_chart=bar_chart,
        pie_chart=LogPieChartData(data=pie_data),
    )
