import json
import logging
from datetime import datetime, timezone, timedelta
from typing import Optional, List
from sqlalchemy.orm import Session as DbSession
from core.db.redis import get_client
from core.constants import SESSION_PREFIX_BUSINESS, TOKEN_PREFIX_BUSINESS, SESSION_PREFIX_CONSUMER, TOKEN_PREFIX_CONSUMER
from core.auth import HeiAuthTool
from core.auth.auth.hei_client_auth_tool import HeiClientAuthTool
from core.utils.ip_utils import get_city_info
from plugins.plugin_sys.user.models import SysUser
from .params import SessionAnalysisResult, SessionPageResult, SessionPageParam, SessionChartData, SessionTokenResult
from plugins.plugin_sys.log.params import LogBarChartData, LogCategorySeries, LogPieChartData, LogCategoryTotal

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
    user_cache = {}

    for key in session_keys:
        user_id = key.split(":")[-1]
        tokens = await redis_client.smembers(key)
        if not tokens:
            continue

        # Find first valid token for this user
        first_token = None
        token_count = 0
        for t in tokens:
            t_str = t.decode() if isinstance(t, bytes) else t
            token_key = f"{token_prefix}{t_str}"
            if await redis_client.exists(token_key):
                if first_token is None:
                    first_token = t_str
                token_count += 1

        if not first_token:
            continue

        token_key = f"{token_prefix}{first_token}"
        token_data_str = await redis_client.get(token_key)
        if not token_data_str:
            continue

        try:
            token_data = json.loads(token_data_str)
        except json.JSONDecodeError:
            continue

        extra = token_data.get("extra", {})
        username_val = extra.get("username", "")
        # Filter by keyword (matches userID or username, case-sensitive like Go)
        if keyword and keyword not in user_id and keyword not in username_val:
            continue

        ttl = await redis_client.ttl(token_key)
        created_at = token_data.get("created_at", "")

        # Lookup user info from DB (cached per user_id)
        if user_id not in user_cache:
            user = db.query(SysUser).filter(SysUser.id == user_id).first()
            user_cache[user_id] = user
        else:
            user = user_cache[user_id]

        nickname = extra.get("nickname", "")
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

        sessions.append(SessionPageResult(
            user_id=user_id,
            username=username_val,
            nickname=nickname,
            avatar=avatar,
            status=status,
            last_login_ip=last_login_ip,
            last_login_address=last_login_address,
            last_login_time=last_login_time,
            session_create_time=created_at,
            session_timeout=_format_timeout(ttl),
            session_timeout_seconds=max(0, ttl),
            token_count=token_count,
        ))

    # Sort by session create time desc
    sessions.sort(
        key=lambda s: s.session_create_time.strftime("%Y-%m-%d %H:%M:%S") if s.session_create_time else "",
        reverse=True,
    )
    return sessions


async def _count_tokens(keys: list, token_prefix: str) -> tuple:
    """Count total tokens and new tokens (last hour) from session keys."""
    redis_client = get_client()
    one_hour_ago = datetime.now(timezone.utc) - timedelta(hours=1)
    total = 0
    new_total = 0
    max_per_user = 0

    for key in keys:
        tokens = await redis_client.smembers(key)
        user_count = 0
        for t in tokens:
            t_str = t.decode() if isinstance(t, bytes) else t
            token_data_str = await redis_client.get(f"{token_prefix}{t_str}")
            if not token_data_str:
                continue
            total += 1
            user_count += 1
            try:
                td = json.loads(token_data_str)
                created = td.get("created_at", "")
                if created:
                    created_dt = datetime.fromisoformat(created).replace(tzinfo=timezone.utc)
                    if created_dt > one_hour_ago:
                        new_total += 1
            except (json.JSONDecodeError, ValueError):
                pass
        if user_count > max_per_user:
            max_per_user = user_count

    return total, new_total, max_per_user


async def analysis(db: DbSession) -> SessionAnalysisResult:
    """Session statistics across B and C ends."""
    redis_client = get_client()
    if not redis_client:
        return SessionAnalysisResult()

    b_keys = await _scan_keys(f"{SESSION_PREFIX_BUSINESS}*")
    c_keys = await _scan_keys(f"{SESSION_PREFIX_CONSUMER}*")

    b_total, b_new, b_max = await _count_tokens(b_keys, TOKEN_PREFIX_BUSINESS)
    c_total, c_new, c_max = await _count_tokens(c_keys, TOKEN_PREFIX_CONSUMER)

    return SessionAnalysisResult(
        total_count=b_total + c_total,
        max_token_count=max(b_max, c_max),
        one_hour_newly_added=b_new + c_new,
        proportion_of_b_and_c=f"{b_total}/{c_total}",
    )


async def list_b_sessions(db: DbSession, param: SessionPageParam) -> dict:
    """Paginated B-end online session list."""
    sessions = await _collect_sessions(SESSION_PREFIX_BUSINESS, TOKEN_PREFIX_BUSINESS, db, param.keyword)

    total = len(sessions)
    current = max(1, param.current)
    size = max(1, param.size)
    offset = (current - 1) * size
    page_sessions = sessions[offset:offset + size]

    return {"records": page_sessions, "total": total}


async def list_c_sessions(db: DbSession, param: SessionPageParam) -> dict:
    """Paginated C-end online session list."""
    sessions = await _collect_sessions(SESSION_PREFIX_CONSUMER, TOKEN_PREFIX_CONSUMER, db, param.keyword)

    total = len(sessions)
    current = max(1, param.current)
    size = max(1, param.size)
    offset = (current - 1) * size
    page_sessions = sessions[offset:offset + size]

    return {"records": page_sessions, "total": total}


async def token_list(session_prefix: str, token_prefix: str, user_id: str) -> List[SessionTokenResult]:
    """List all active tokens for a specific user."""
    redis_client = get_client()
    if not redis_client:
        return []

    session_key = f"{session_prefix}{user_id}"
    tokens = await redis_client.smembers(session_key)
    results = []

    for t in tokens:
        t_str = t.decode() if isinstance(t, bytes) else t
        token_key = f"{token_prefix}{t_str}"
        token_data_str = await redis_client.get(token_key)
        if not token_data_str:
            continue

        try:
            token_data = json.loads(token_data_str)
        except json.JSONDecodeError:
            continue

        ttl = await redis_client.ttl(token_key)
        extra = token_data.get("extra", {})

        created_at_str = token_data.get("created_at", "")
        created_at_dt = None
        if created_at_str:
            if isinstance(created_at_str, datetime):
                created_at_dt = created_at_str
            else:
                try:
                    created_at_dt = datetime.strptime(created_at_str, "%Y-%m-%d %H:%M:%S")
                except (ValueError, TypeError):
                    pass

        results.append(SessionTokenResult(
            token=t_str,
            created_at=created_at_dt,
            timeout=_format_timeout(ttl),
            timeout_seconds=max(0, ttl),
            device_type=extra.get("device_type"),
            device_id=extra.get("device_id"),
        ))

    return results


async def exit_b_session(user_id: str) -> None:
    """Force logout a B-end user (all tokens)."""
    await HeiAuthTool.kickout(user_id)


async def exit_c_session(user_id: str) -> None:
    """Force logout a C-end user (all tokens)."""
    await HeiClientAuthTool.kickout(user_id)


async def exit_b_session_token(user_id: str, token: str) -> None:
    """Force logout a specific B-end token."""
    await HeiAuthTool.kickout_token(user_id, token)


async def exit_c_session_token(user_id: str, token: str) -> None:
    """Force logout a specific C-end token."""
    await HeiClientAuthTool.kickout_token(user_id, token)


async def chart_data(db: DbSession) -> SessionChartData:
    """Session chart data: B/C 7-day trend and proportion."""
    redis_client = get_client()
    if not redis_client:
        return SessionChartData()

    b_keys = await _scan_keys(f"{SESSION_PREFIX_BUSINESS}*")
    c_keys = await _scan_keys(f"{SESSION_PREFIX_CONSUMER}*")

    # --- Pie chart: B vs C total ---
    b_total, _, _ = await _count_tokens(b_keys, TOKEN_PREFIX_BUSINESS)
    c_total, _, _ = await _count_tokens(c_keys, TOKEN_PREFIX_CONSUMER)
    pie_data = [
        LogCategoryTotal(category="BUSINESS", total=b_total),
        LogCategoryTotal(category="CONSUMER", total=c_total),
    ]

    # --- Bar chart: last 7 days daily new sessions ---
    today = datetime.now(timezone.utc).replace(hour=0, minute=0, second=0, microsecond=0)
    days = [(today - timedelta(days=i)).strftime("%Y-%m-%d") for i in range(6, -1, -1)]
    b_daily = [0] * 7
    c_daily = [0] * 7

    async def _count_daily(keys, token_prefix, daily):
        for key in keys:
            tokens = await redis_client.smembers(key)
            for t in tokens:
                t_str = t.decode() if isinstance(t, bytes) else t
                token_data_str = await redis_client.get(f"{token_prefix}{t_str}")
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

    await _count_daily(b_keys, TOKEN_PREFIX_BUSINESS, b_daily)
    await _count_daily(c_keys, TOKEN_PREFIX_CONSUMER, c_daily)

    bar_chart = LogBarChartData(
        days=days,
        series=[
            LogCategorySeries(name="BUSINESS", data=b_daily),
            LogCategorySeries(name="CONSUMER", data=c_daily),
        ],
    )

    return SessionChartData(
        bar_chart=bar_chart,
        pie_chart=LogPieChartData(data=pie_data),
    )
