"""Client session service — standalone, mirrors hei-gin plugin-client/session/service.go."""

import json
import logging
from datetime import datetime, timezone, timedelta
from typing import Optional, List, Dict, Any
from sqlalchemy.orm import Session as DbSession
from sqlalchemy import select
from core.db.redis import get_client
from core.constants import SESSION_PREFIX_BUSINESS, TOKEN_PREFIX_BUSINESS, SESSION_PREFIX_CONSUMER, TOKEN_PREFIX_CONSUMER
from core.auth.auth.hei_client_auth_tool import HeiClientAuthTool
from plugins.plugin_client.user.models import ClientUser
from .params import (
    SessionAnalysisResult, SessionPageResult, SessionPageParam,
    SessionChartData, SessionTokenResult, SessionExitParam, SessionExitTokenParam,
    BarChartData, CategorySeries, PieChartData, CategoryTotal,
)

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
    """Format remaining seconds to human-readable string — mirrors Go formatTimeout."""
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


async def _count_tokens(keys: List[str], token_prefix: str) -> tuple:
    """Count tokens: total, one_hour_newly_added, max_per_user — mirrors Go countTokens."""
    redis_client = get_client()
    if not redis_client:
        return 0, 0, 0

    one_hour_ago = datetime.now(timezone.utc) - timedelta(hours=1)
    total = 0
    one_hour_newly_added = 0
    user_token_counts: Dict[str, int] = {}

    for key in keys:
        tokens = await redis_client.smembers(key)
        user_id = key.split(":")[-1]
        user_token_counts[user_id] = len(tokens)

        for t in tokens:
            t_str = t.decode() if isinstance(t, bytes) else t
            token_key = f"{token_prefix}{t_str}"
            data_str = await redis_client.get(token_key)
            if not data_str:
                continue
            try:
                token_data = json.loads(data_str)
            except json.JSONDecodeError:
                continue

            total += 1
            created_at_str = token_data.get("created_at", "")
            if created_at_str:
                try:
                    created_at = datetime.strptime(created_at_str, "%Y-%m-%d %H:%M:%S").replace(tzinfo=timezone.utc)
                    if created_at > one_hour_ago:
                        one_hour_newly_added += 1
                except ValueError:
                    pass

    max_per_user = max(user_token_counts.values()) if user_token_counts else 0
    return total, one_hour_newly_added, max_per_user


async def _count_daily(keys: List[str], token_prefix: str) -> Dict[str, int]:
    """Count daily new tokens — mirrors Go countDaily."""
    redis_client = get_client()
    if not redis_client:
        return {}

    daily: Dict[str, int] = {}
    for key in keys:
        tokens = await redis_client.smembers(key)
        for t in tokens:
            t_str = t.decode() if isinstance(t, bytes) else t
            token_key = f"{token_prefix}{t_str}"
            data_str = await redis_client.get(token_key)
            if not data_str:
                continue
            try:
                token_data = json.loads(data_str)
            except json.JSONDecodeError:
                continue

            created_at_str = token_data.get("created_at", "")
            if created_at_str:
                try:
                    created_at = datetime.strptime(created_at_str, "%Y-%m-%d %H:%M:%S")
                    day_key = created_at.strftime("%Y-%m-%d")
                    daily[day_key] = daily.get(day_key, 0) + 1
                except ValueError:
                    pass
    return daily


async def _collect_sessions(
    session_prefix: str,
    token_prefix: str,
    db: DbSession,
    keyword: Optional[str] = None,
) -> List[Dict[str, Any]]:
    """Collect online sessions from Redis, enriched with ClientUser DB info.
    Returns dicts (not SessionPageResult models) to avoid Pydantic datetime coercion issues.
    Mirrors Go collectSessions.
    """
    redis_client = get_client()
    if not redis_client:
        return []

    session_keys = await _scan_keys(f"{session_prefix}*")
    sessions: List[Dict[str, Any]] = []
    user_cache: Dict[str, Optional[ClientUser]] = {}

    for key in session_keys:
        user_id = key.split(":")[-1]

        # Keyword filter: match user_id or extra.username
        if keyword and keyword not in user_id:
            # Check first token's extra.username before skipping
            tokens = await redis_client.smembers(key)
            found = False
            for t in tokens:
                t_str = t.decode() if isinstance(t, bytes) else t
                token_key = f"{token_prefix}{t_str}"
                data_str = await redis_client.get(token_key)
                if not data_str:
                    continue
                try:
                    td = json.loads(data_str)
                    extra = td.get("extra", {})
                    if keyword in extra.get("username", ""):
                        found = True
                        break
                except json.JSONDecodeError:
                    continue
            if not found:
                continue
        else:
            tokens = await redis_client.smembers(key)

        if not tokens:
            continue

        token_count = 0
        session_create_time = ""
        username_val = ""

        for t in tokens:
            t_str = t.decode() if isinstance(t, bytes) else t
            token_key = f"{token_prefix}{t_str}"
            data_str = await redis_client.get(token_key)
            if not data_str:
                continue
            try:
                token_data = json.loads(data_str)
            except json.JSONDecodeError:
                continue

            token_count += 1
            ct = token_data.get("created_at", "")
            if ct and (not session_create_time or ct < session_create_time):
                session_create_time = ct
            extra = token_data.get("extra", {})
            if extra.get("username") and not username_val:
                username_val = extra.get("username", "")
            # Use first valid token for rest of processing
            break  # Go breaks after first valid token

        if not token_count:
            continue

        # TTL on session key
        ttl = await redis_client.ttl(key)
        timeout_seconds = max(-1, ttl)

        # Cached ClientUser lookup
        user = user_cache.get(user_id)
        if user_id not in user_cache:
            stmt = select(ClientUser).where(ClientUser.id == user_id)
            user = db.execute(stmt).scalar_one_or_none()
            user_cache[user_id] = user

        nickname = None
        avatar = None
        status = None
        last_login_ip = None
        last_login_time = ""
        if user:
            if user.nickname:
                nickname = user.nickname
            if user.avatar:
                avatar = user.avatar
            if user.status:
                status = user.status
            if user.last_login_ip:
                last_login_ip = user.last_login_ip
            if user.last_login_at:
                last_login_time = user.last_login_at.strftime("%Y-%m-%d %H:%M:%S")

        sessions.append({
            "user_id": user_id,
            "username": username_val or None,
            "nickname": nickname,
            "avatar": avatar,
            "status": status,
            "last_login_ip": last_login_ip,
            "last_login_time": last_login_time,
            "token_count": token_count,
            "session_create_time": session_create_time,
            "session_timeout": _format_timeout(timeout_seconds),
            "session_timeout_seconds": max(0, timeout_seconds),
        })

    # Sort by session_create_time descending
    sessions.sort(key=lambda s: s["session_create_time"], reverse=True)
    return sessions


async def analysis(db: DbSession) -> SessionAnalysisResult:
    """Session analysis — mirrors Go Analysis()."""
    b_keys = await _scan_keys(f"{SESSION_PREFIX_BUSINESS}*")
    c_keys = await _scan_keys(f"{SESSION_PREFIX_CONSUMER}*")

    b_total, b_newly, b_max = await _count_tokens(b_keys, TOKEN_PREFIX_BUSINESS)
    c_total, c_newly, c_max = await _count_tokens(c_keys, TOKEN_PREFIX_CONSUMER)

    max_token_count = b_max if b_max > c_max else c_max

    return SessionAnalysisResult(
        total_count=b_total + c_total,
        max_token_count=max_token_count,
        one_hour_newly_added=b_newly + c_newly,
        proportion_of_b_and_c=f"{b_total}/{c_total}",
    )


async def page(db: DbSession, param: SessionPageParam) -> Dict[str, Any]:
    """Paginated C-end online session list — mirrors Go Page()."""
    sessions = await _collect_sessions(
        SESSION_PREFIX_CONSUMER, TOKEN_PREFIX_CONSUMER, db, param.keyword
    )

    total = len(sessions)
    current = max(1, param.current)
    size = max(1, param.size)
    offset = (current - 1) * size
    page_sessions = sessions[offset:offset + size]

    return {"records": page_sessions, "total": total}


async def exit_session(user_id: str) -> None:
    """Force logout a C-end user — mirrors Go Exit()."""
    await HeiClientAuthTool.kickout(user_id)


async def token_list(user_id: str) -> List[SessionTokenResult]:
    """List all active tokens for a C-end user — mirrors Go TokenList()."""
    redis_client = get_client()
    if not redis_client:
        return []

    session_key = f"{SESSION_PREFIX_CONSUMER}{user_id}"
    tokens = await redis_client.smembers(session_key)
    results = []

    for t in tokens:
        t_str = t.decode() if isinstance(t, bytes) else t
        token_key = f"{TOKEN_PREFIX_CONSUMER}{t_str}"
        data_str = await redis_client.get(token_key)
        if not data_str:
            continue

        try:
            token_data = json.loads(data_str)
        except json.JSONDecodeError:
            continue

        ttl = await redis_client.ttl(token_key)
        extra = token_data.get("extra", {})

        results.append(SessionTokenResult(
            token=t_str,
            created_at=token_data.get("created_at", ""),
            timeout=_format_timeout(ttl),
            timeout_seconds=max(0, ttl),
            device_type=extra.get("device_type"),
            device_id=extra.get("device_id"),
        ))

    return results


async def exit_token(user_id: str, token: str) -> None:
    """Force logout a specific C-end token — mirrors Go ExitToken()."""
    await HeiClientAuthTool.kickout_token(user_id, token)


async def chart_data() -> SessionChartData:
    """Session chart data — C-end only, mirrors Go ChartData()."""
    redis_client = get_client()
    if not redis_client:
        return SessionChartData()

    c_keys = await _scan_keys(f"{SESSION_PREFIX_CONSUMER}*")
    c_total, _, _ = await _count_tokens(c_keys, TOKEN_PREFIX_CONSUMER)
    c_daily = await _count_daily(c_keys, TOKEN_PREFIX_CONSUMER)

    # Last 7 days
    days = [(datetime.now(timezone.utc) - timedelta(days=i)).strftime("%Y-%m-%d")
            for i in range(6, -1, -1)]
    series_data = [c_daily.get(day, 0) for day in days]

    return SessionChartData(
        bar_chart=BarChartData(
            days=days,
            series=[CategorySeries(name="新增在线数", data=series_data)],
        ),
        pie_chart=PieChartData(
            data=[CategoryTotal(category="CONSUMER", total=c_total)],
        ),
    )
