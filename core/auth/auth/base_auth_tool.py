import secrets
from contextvars import ContextVar
from datetime import datetime, timezone, timedelta
from typing import Optional, Any, Dict, List, Union, Tuple
import json
from fastapi import Request, HTTPException, status

from config.settings import settings
from core.db.redis import get_client


class BaseAuthTool:
    _expire: int = settings.token.expire_seconds
    _token_name: str = settings.token.token_name
    _request_var: ContextVar[Optional[Request]] = ContextVar('base_auth_request', default=None)

    # Subclasses must override these
    TYPE = None
    TOKEN_PREFIX = None
    SESSION_PREFIX = None
    DISABLE_KEY = None

    @classmethod
    def _get_session_index_key(cls) -> str:
        return f"hei:auth:{cls.TYPE}:session:index"

    @classmethod
    def _get_session_expiry_key(cls) -> str:
        return f"hei:auth:{cls.TYPE}:session:expiry"

    @classmethod
    def _get_session_count_key(cls) -> str:
        return f"hei:auth:{cls.TYPE}:session:counts"

    @classmethod
    def _get_token_created_index_key(cls) -> str:
        return f"hei:auth:{cls.TYPE}:token:created"

    @classmethod
    def _get_token_expiry_index_key(cls) -> str:
        return f"hei:auth:{cls.TYPE}:token:expiry"

    @classmethod
    def _get_token_owner_key(cls) -> str:
        return f"hei:auth:{cls.TYPE}:token:owners"

    @classmethod
    def _decode_redis_value(cls, value: Any) -> str:
        if isinstance(value, bytes):
            return value.decode()
        return value or ""

    @classmethod
    async def _scan_keys(cls, pattern: str) -> List[str]:
        redis_client = cls._get_redis()
        if not redis_client:
            return []
        cursor = 0
        keys = []
        while True:
            cursor, batch = await redis_client.scan(cursor=cursor, match=pattern, count=200)
            keys.extend(batch)
            if cursor == 0:
                break
        return [cls._decode_redis_value(key) for key in keys]

    @classmethod
    async def _smembers_by_keys(cls, keys: List[str]) -> Dict[str, List[str]]:
        redis_client = cls._get_redis()
        if not redis_client or not keys:
            return {}
        pipe = redis_client.pipeline()
        commands = [pipe.smembers(key) for key in keys]
        await pipe.execute()
        result: Dict[str, List[str]] = {}
        for key, cmd in zip(keys, commands):
            values = await cmd
            result[key] = [cls._decode_redis_value(v) for v in values]
        return result

    @classmethod
    async def _ttls_by_keys(cls, keys: List[str]) -> Dict[str, int]:
        redis_client = cls._get_redis()
        if not redis_client or not keys:
            return {}
        pipe = redis_client.pipeline()
        commands = [pipe.ttl(key) for key in keys]
        await pipe.execute()
        return {key: int(await cmd) for key, cmd in zip(keys, commands)}

    @classmethod
    async def _token_payloads_by_keys(cls, token_keys: List[str]) -> Dict[str, Dict[str, Any]]:
        redis_client = cls._get_redis()
        if not redis_client or not token_keys:
            return {}
        values = await redis_client.mget(token_keys)
        payloads: Dict[str, Dict[str, Any]] = {}
        for token_key, raw in zip(token_keys, values):
            data_str = cls._decode_redis_value(raw)
            if not data_str:
                continue
            try:
                payloads[token_key] = json.loads(data_str)
            except json.JSONDecodeError:
                continue
        return payloads

    @classmethod
    def _parse_created_at(cls, value: Any) -> Optional[datetime]:
        if not value:
            return None
        if isinstance(value, datetime):
            dt = value
        else:
            try:
                dt = datetime.fromisoformat(str(value))
            except (TypeError, ValueError):
                return None
        if dt.tzinfo is None:
            return dt.replace(tzinfo=timezone.utc)
        return dt.astimezone(timezone.utc)

    @classmethod
    async def _track_login_session(
        cls,
        user_id: str,
        token: str,
        created_at: datetime,
        ttl_seconds: int,
    ) -> None:
        redis_client = cls._get_redis()
        if not redis_client or not user_id or not token:
            return
        session_key = cls._get_session_key(user_id)
        token_count = await redis_client.scard(session_key)
        expires_at = int(created_at.timestamp()) + ttl_seconds
        pipe = redis_client.pipeline()
        pipe.zadd(cls._get_session_index_key(), {user_id: created_at.timestamp()})
        pipe.zadd(cls._get_session_expiry_key(), {user_id: expires_at})
        pipe.zadd(cls._get_session_count_key(), {user_id: max(1, int(token_count))})
        pipe.zadd(cls._get_token_created_index_key(), {token: created_at.timestamp()})
        pipe.zadd(cls._get_token_expiry_index_key(), {token: expires_at})
        pipe.hset(cls._get_token_owner_key(), token, user_id)
        await pipe.execute()

    @classmethod
    async def _remove_session_indexes(cls, user_id: str) -> None:
        redis_client = cls._get_redis()
        if not redis_client or not user_id:
            return
        pipe = redis_client.pipeline()
        pipe.zrem(cls._get_session_index_key(), user_id)
        pipe.zrem(cls._get_session_expiry_key(), user_id)
        pipe.zrem(cls._get_session_count_key(), user_id)
        await pipe.execute()

    @classmethod
    async def _refresh_session_indexes(cls, user_id: str) -> None:
        redis_client = cls._get_redis()
        if not redis_client or not user_id:
            return
        session_key = cls._get_session_key(user_id)
        tokens = [cls._decode_redis_value(v) for v in await redis_client.smembers(session_key)]
        if not tokens:
            await cls._remove_session_indexes(user_id)
            return

        token_keys = [cls._get_token_key(token) for token in tokens]
        payloads = await cls._token_payloads_by_keys(token_keys)
        live_tokens: List[str] = []
        max_created_at: Optional[datetime] = None
        stale_tokens: List[str] = []
        for token, token_key in zip(tokens, token_keys):
            token_data = payloads.get(token_key)
            if not token_data:
                stale_tokens.append(token)
                continue
            created_at_str = token_data.get("created_at", "")
            try:
                created_at = datetime.fromisoformat(created_at_str)
            except (ValueError, TypeError):
                created_at = datetime.now(timezone.utc)
            live_tokens.append(token)
            if max_created_at is None or created_at > max_created_at:
                max_created_at = created_at

        if stale_tokens:
            pipe = redis_client.pipeline()
            pipe.srem(session_key, *stale_tokens)
            pipe.zrem(cls._get_token_created_index_key(), *stale_tokens)
            pipe.zrem(cls._get_token_expiry_index_key(), *stale_tokens)
            pipe.hdel(cls._get_token_owner_key(), *stale_tokens)
            await pipe.execute()

        if not live_tokens or max_created_at is None:
            await cls._remove_session_indexes(user_id)
            return

        ttl = await redis_client.ttl(session_key)
        if ttl <= 0:
            await cls._remove_session_indexes(user_id)
            return

        expires_at = int(datetime.now(timezone.utc).timestamp()) + int(ttl)
        pipe = redis_client.pipeline()
        pipe.zadd(cls._get_session_index_key(), {user_id: max_created_at.timestamp()})
        pipe.zadd(cls._get_session_expiry_key(), {user_id: expires_at})
        pipe.zadd(cls._get_session_count_key(), {user_id: len(live_tokens)})
        for token in live_tokens:
            pipe.hset(cls._get_token_owner_key(), token, user_id)
        await pipe.execute()

    @classmethod
    async def _cleanup_expired_indexes(cls) -> None:
        redis_client = cls._get_redis()
        if not redis_client:
            return
        now_ts = int(datetime.now(timezone.utc).timestamp())
        expired_users = await redis_client.zrangebyscore(
            cls._get_session_expiry_key(),
            min="-inf",
            max=now_ts,
        )
        if expired_users:
            expired_users = [cls._decode_redis_value(v) for v in expired_users]
            pipe = redis_client.pipeline()
            pipe.zrem(cls._get_session_index_key(), *expired_users)
            pipe.zrem(cls._get_session_expiry_key(), *expired_users)
            pipe.zrem(cls._get_session_count_key(), *expired_users)
            await pipe.execute()

        expired_tokens = await redis_client.zrangebyscore(
            cls._get_token_expiry_index_key(),
            min="-inf",
            max=now_ts,
        )
        if not expired_tokens:
            return
        expired_tokens = [cls._decode_redis_value(v) for v in expired_tokens]
        owners = await redis_client.hmget(cls._get_token_owner_key(), expired_tokens)
        pipe = redis_client.pipeline()
        users_to_refresh = set()
        for token, owner in zip(expired_tokens, owners):
            owner_id = cls._decode_redis_value(owner)
            if owner_id:
                pipe.srem(cls._get_session_key(owner_id), token)
                users_to_refresh.add(owner_id)
        pipe.zrem(cls._get_token_created_index_key(), *expired_tokens)
        pipe.zrem(cls._get_token_expiry_index_key(), *expired_tokens)
        pipe.hdel(cls._get_token_owner_key(), *expired_tokens)
        await pipe.execute()
        for user_id in users_to_refresh:
            await cls._refresh_session_indexes(user_id)

    @classmethod
    async def _page_indexed_user_ids(
        cls,
        current: int,
        size: int,
        keyword: Optional[str] = None,
    ) -> Tuple[List[str], int]:
        redis_client = cls._get_redis()
        if not redis_client:
            return [], 0
        if keyword:
            score = await redis_client.zscore(cls._get_session_index_key(), keyword)
            if score is None:
                return [], 0
            if current > 1:
                return [], 1
            return [keyword], 1
        total = int(await redis_client.zcard(cls._get_session_index_key()))
        offset = (current - 1) * size
        user_ids = await redis_client.zrevrange(
            cls._get_session_index_key(),
            offset,
            offset + size - 1,
        )
        return [cls._decode_redis_value(v) for v in user_ids], total

    @classmethod
    async def _hydrate_session_infos(cls, user_ids: List[str]) -> List[Dict[str, Any]]:
        redis_client = cls._get_redis()
        if not redis_client or not user_ids:
            return []
        session_keys = [cls._get_session_key(user_id) for user_id in user_ids]
        session_tokens = await cls._smembers_by_keys(session_keys)
        all_token_keys: List[str] = []
        for tokens in session_tokens.values():
            all_token_keys.extend(cls._get_token_key(token) for token in tokens)
        token_payloads = await cls._token_payloads_by_keys(all_token_keys)
        session_ttls = await cls._ttls_by_keys(session_keys)
        raw_sessions: List[Dict[str, Any]] = []

        for user_id, key in zip(user_ids, session_keys):
            tokens = session_tokens.get(key, [])
            if not tokens:
                await cls._remove_session_indexes(user_id)
                continue

            live_tokens: List[str] = []
            stale_tokens: List[str] = []
            earliest_created_at: Optional[datetime] = None
            latest_created_at: Optional[datetime] = None
            first_payload: Optional[Dict[str, Any]] = None
            username_val = ""

            for token in tokens:
                token_key = cls._get_token_key(token)
                payload = token_payloads.get(token_key)
                if not payload:
                    stale_tokens.append(token)
                    continue
                created_at = cls._parse_created_at(payload.get("created_at"))
                if created_at is None:
                    stale_tokens.append(token)
                    continue
                live_tokens.append(token)
                if earliest_created_at is None or created_at < earliest_created_at:
                    earliest_created_at = created_at
                if latest_created_at is None or created_at > latest_created_at:
                    latest_created_at = created_at
                if first_payload is None or created_at == earliest_created_at:
                    first_payload = payload
                extra = payload.get("extra", {})
                if extra.get("username") and not username_val:
                    username_val = extra.get("username", "")

            if stale_tokens:
                pipe = redis_client.pipeline()
                pipe.srem(key, *stale_tokens)
                pipe.zrem(cls._get_token_created_index_key(), *stale_tokens)
                pipe.zrem(cls._get_token_expiry_index_key(), *stale_tokens)
                pipe.hdel(cls._get_token_owner_key(), *stale_tokens)
                await pipe.execute()

            token_count = len(live_tokens)
            ttl = session_ttls.get(key, -1)
            if not token_count or not first_payload or earliest_created_at is None or latest_created_at is None or ttl <= 0:
                await cls._remove_session_indexes(user_id)
                continue

            expires_at = int(datetime.now(timezone.utc).timestamp()) + int(ttl)
            pipe = redis_client.pipeline()
            pipe.zadd(cls._get_session_index_key(), {user_id: latest_created_at.timestamp()})
            pipe.zadd(cls._get_session_expiry_key(), {user_id: expires_at})
            pipe.zadd(cls._get_session_count_key(), {user_id: token_count})
            for token in live_tokens:
                pipe.hset(cls._get_token_owner_key(), token, user_id)
            await pipe.execute()

            raw_sessions.append({
                "user_id": user_id,
                "username": username_val or None,
                "nickname": first_payload.get("extra", {}).get("nickname", ""),
                "session_create_time": earliest_created_at.isoformat(),
                "token_count": token_count,
                "session_timeout_seconds": max(0, ttl),
            })
        return raw_sessions

    @classmethod
    async def list_session_infos(
        cls,
        current: int = 1,
        size: int = 10,
        keyword: Optional[str] = None,
    ) -> Tuple[List[Dict[str, Any]], int]:
        current = max(1, current)
        size = min(max(1, size), 100)
        await cls._cleanup_expired_indexes()
        user_ids, total = await cls._page_indexed_user_ids(current, size, keyword)
        infos = await cls._hydrate_session_infos(user_ids)
        return infos, total

    @classmethod
    async def list_session_infos_by_user_ids(
        cls,
        user_ids: List[Union[str, int]],
        current: int = 1,
        size: int = 10,
    ) -> Tuple[List[Dict[str, Any]], int]:
        current = max(1, current)
        size = min(max(1, size), 100)
        await cls._cleanup_expired_indexes()
        redis_client = cls._get_redis()
        if not redis_client:
            return [], 0
        deduped = []
        seen = set()
        for user_id in user_ids:
            value = str(user_id)
            if value and value not in seen:
                seen.add(value)
                deduped.append(value)
        if not deduped:
            return [], 0

        rows = []
        chunk_size = 1000
        for start in range(0, len(deduped), chunk_size):
            chunk = deduped[start:start + chunk_size]
            scores = await redis_client.zmscore(cls._get_session_index_key(), chunk)
            for index, score in enumerate(scores):
                if score is None:
                    continue
                rows.append((chunk[index], float(score)))
        rows.sort(key=lambda item: (-item[1], item[0]))
        total = len(rows)
        offset = (current - 1) * size
        page_user_ids = [user_id for user_id, _ in rows[offset:offset + size]]
        infos = await cls._hydrate_session_infos(page_user_ids)
        return infos, total

    @classmethod
    async def get_session_stats(cls) -> Dict[str, int]:
        await cls._cleanup_expired_indexes()
        redis_client = cls._get_redis()
        if not redis_client:
            return {"total_count": 0, "one_hour_newly_added": 0, "max_token_count": 0}
        total = await redis_client.zcard(cls._get_token_created_index_key())
        one_hour_ago = int((datetime.now(timezone.utc) - timedelta(hours=1)).timestamp())
        one_hour_newly_added = await redis_client.zcount(cls._get_token_created_index_key(), one_hour_ago, "+inf")
        top_rows = await redis_client.zrevrange(cls._get_session_count_key(), 0, 0, withscores=True)
        max_token_count = int(top_rows[0][1]) if top_rows else 0
        return {
            "total_count": int(total),
            "one_hour_newly_added": int(one_hour_newly_added),
            "max_token_count": max_token_count,
        }

    @classmethod
    async def get_session_daily_counts(cls, days: List[str]) -> Dict[str, int]:
        result = {day: 0 for day in days}
        await cls._cleanup_expired_indexes()
        redis_client = cls._get_redis()
        if not redis_client:
            return result
        for day in days:
            start = datetime.strptime(day, "%Y-%m-%d").replace(tzinfo=timezone.utc)
            end = start + timedelta(days=1) - timedelta(seconds=1)
            result[day] = int(await redis_client.zcount(
                cls._get_token_created_index_key(),
                int(start.timestamp()),
                int(end.timestamp()),
            ))
        return result

    @classmethod
    async def get_session_tokens(cls, user_id: Union[str, int]) -> List[Dict[str, Any]]:
        user_id = str(user_id)
        session_key = cls._get_session_key(user_id)
        redis_client = cls._get_redis()
        if not redis_client:
            return []
        tokens = [cls._decode_redis_value(v) for v in await redis_client.smembers(session_key)]
        token_keys = [cls._get_token_key(token) for token in tokens]
        payloads = await cls._token_payloads_by_keys(token_keys)
        ttl_map = await cls._ttls_by_keys(token_keys)
        results = []
        for token, token_key in zip(tokens, token_keys):
            token_data = payloads.get(token_key)
            if not token_data:
                continue
            extra = token_data.get("extra", {})
            ttl = ttl_map.get(token_key, -1)
            results.append({
                "token": token,
                "created_at": token_data.get("created_at", ""),
                "timeout_seconds": max(0, ttl),
                "device_type": extra.get("device_type"),
                "device_id": extra.get("device_id"),
            })
        return results

    @classmethod
    def init(cls, expire: int = None, token_name: str = None):
        if expire:
            cls._expire = expire
        if token_name:
            cls._token_name = token_name

    @classmethod
    def getLoginType(cls) -> str:
        return cls.TYPE

    @classmethod
    def getTokenName(cls) -> str:
        return cls._token_name

    @classmethod
    def _get_redis(cls):
        return get_client()

    @classmethod
    def _get_token_key(cls, token: str) -> str:
        return f"{cls.TOKEN_PREFIX}{token}"

    @classmethod
    def _get_session_key(cls, user_id: str) -> str:
        return f"{cls.SESSION_PREFIX}{user_id}"

    @classmethod
    def setRequest(cls, request: Request):
        cls._request_var.set(request)

    @classmethod
    def getRequest(cls) -> Optional[Request]:
        return cls._request_var.get()

    @classmethod
    async def setTokenValue(cls, token_value: str, request: Request = None):
        req = request or cls._request_var.get()
        if req:
            req.state.token_value = token_value

    @classmethod
    async def getTokenValue(cls, request: Request = None) -> Optional[str]:
        req = request or cls._request_var.get()
        if not req:
            return None
        return req.headers.get(cls._token_name)

    @classmethod
    async def getTokenInfo(cls, request: Request = None) -> Optional[Dict]:
        token = await cls.getTokenValue(request)
        if not token:
            return None
        return await cls._get_token_data(token)

    @classmethod
    async def login(cls, id: Union[str, int], request: Request = None, extra: Dict = None) -> str:
        now = datetime.now(timezone.utc)
        user_id = str(id)

        token = secrets.token_hex(32)
        redis_client = cls._get_redis()
        token_data = {
            "user_id": user_id,
            "type": cls.TYPE,
            "created_at": now.isoformat(),
            "extra": extra or {}
        }

        await redis_client.setex(
            cls._get_token_key(token),
            cls._expire,
            json.dumps(token_data, ensure_ascii=False)
        )

        session_key = cls._get_session_key(user_id)
        await redis_client.sadd(session_key, token)
        await redis_client.expire(session_key, cls._expire)
        await cls._track_login_session(user_id, token, now, cls._expire)

        return token

    @classmethod
    async def logout(cls, login_id: Union[str, int] = None, request: Request = None):
        if login_id is not None:
            await cls.kickout(login_id)
            return

        token = await cls.getTokenValue(request)
        if not token:
            return

        data = await cls._get_token_data(token)
        if data:
            user_id = data.get("user_id")
            if user_id:
                session_key = cls._get_session_key(user_id)
                redis_client = cls._get_redis()
                await redis_client.srem(session_key, token)
                await cls._refresh_session_indexes(user_id)

        redis_client = cls._get_redis()
        token_key = cls._get_token_key(token)
        await redis_client.delete(token_key)
        pipe = redis_client.pipeline()
        pipe.zrem(cls._get_token_created_index_key(), token)
        pipe.zrem(cls._get_token_expiry_index_key(), token)
        pipe.hdel(cls._get_token_owner_key(), token)
        await pipe.execute()

    @classmethod
    async def kickout(cls, login_id: Union[str, int]):
        redis_client = cls._get_redis()
        user_id = str(login_id)
        session_key = cls._get_session_key(user_id)
        tokens = [cls._decode_redis_value(v) for v in await redis_client.smembers(session_key)]

        pipe = redis_client.pipeline()
        for token in tokens:
            pipe.delete(cls._get_token_key(token))
        if tokens:
            pipe.zrem(cls._get_token_created_index_key(), *tokens)
            pipe.zrem(cls._get_token_expiry_index_key(), *tokens)
            pipe.hdel(cls._get_token_owner_key(), *tokens)
        pipe.delete(session_key)
        await pipe.execute()

        await cls._remove_session_indexes(user_id)

    @classmethod
    async def kickout_token(cls, login_id: Union[str, int], token: str):
        redis_client = cls._get_redis()
        user_id = str(login_id)
        session_key = cls._get_session_key(user_id)
        token_key = cls._get_token_key(token)

        await redis_client.srem(session_key, token)
        await redis_client.delete(token_key)
        pipe = redis_client.pipeline()
        pipe.zrem(cls._get_token_created_index_key(), token)
        pipe.zrem(cls._get_token_expiry_index_key(), token)
        pipe.hdel(cls._get_token_owner_key(), token)
        await pipe.execute()
        await cls._refresh_session_indexes(user_id)

    @classmethod
    async def isLogin(cls, request: Request = None) -> bool:
        try:
            login_id = await cls.getLoginIdDefaultNull(request)
            return login_id is not None
        except:
            return False

    @classmethod
    async def checkLogin(cls, request: Request = None):
        if not await cls.isLogin(request):
            raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="未授权/未登录")

    @classmethod
    async def getLoginId(cls, request: Request = None) -> Optional[str]:
        return await cls.getLoginIdDefaultNull(request)

    @classmethod
    async def getLoginIdDefaultNull(cls, request: Request = None) -> Optional[str]:
        token = await cls.getTokenValue(request)
        if not token:
            return None

        data = await cls._decode_token(token)
        if data:
            return data.get("user_id")
        return None

    @classmethod
    async def getLoginIdAsString(cls, request: Request = None) -> Optional[str]:
        return await cls.getLoginIdDefaultNull(request)

    @classmethod
    async def getLoginIdAsInt(cls, request: Request = None) -> Optional[int]:
        login_id = await cls.getLoginIdDefaultNull(request)
        if login_id:
            try:
                return int(login_id)
            except:
                return None
        return None

    @classmethod
    async def getLoginIdAsLong(cls, request: Request = None) -> Optional[int]:
        return await cls.getLoginIdAsInt(request)

    @classmethod
    async def getLoginIdByToken(cls, token_value: str) -> Optional[str]:
        if not token_value:
            return None
        data = await cls._decode_token(token_value)
        if data:
            return data.get("user_id")
        return None

    @classmethod
    async def _decode_token(cls, token: str) -> Optional[Dict]:
        if not token:
            return None
        return await cls._get_token_data(token)

    @classmethod
    async def _get_token_data(cls, token: str) -> Optional[Dict]:
        if not token:
            return None

        redis_client = cls._get_redis()
        token_key = cls._get_token_key(token)
        token_data = await redis_client.get(token_key)

        if token_data:
            try:
                return json.loads(token_data)
            except:
                return None
        return None

    @classmethod
    async def getExtra(cls, key: str, request: Request = None) -> Optional[Any]:
        data = await cls.getTokenInfo(request)
        if data:
            return data.get("extra", {}).get(key)
        return None

    @classmethod
    async def getSession(cls, request: Request = None) -> Optional[Dict]:
        token = await cls.getTokenValue(request)
        if not token:
            return None
        return await cls._get_token_data(token)

    @classmethod
    async def getTokenSession(cls, request: Request = None) -> Optional[Dict]:
        return await cls.getSession(request)

    @classmethod
    async def getTokenTimeout(cls, request: Request = None) -> int:
        token = await cls.getTokenValue(request)
        if not token:
            return -1

        redis_client = cls._get_redis()
        token_key = cls._get_token_key(token)
        ttl = await redis_client.ttl(token_key)
        return ttl if ttl > 0 else -1

    @classmethod
    async def getSessionTimeout(cls, request: Request = None) -> int:
        login_id = await cls.getLoginIdDefaultNull(request)
        if not login_id:
            return -1

        redis_client = cls._get_redis()
        session_key = cls._get_session_key(login_id)
        ttl = await redis_client.ttl(session_key)
        return ttl if ttl > 0 else -1

    @classmethod
    async def renewTimeout(cls, timeout: int = None, request: Request = None):
        token = await cls.getTokenValue(request)
        if not token:
            return

        new_timeout = timeout or cls._expire
        redis_client = cls._get_redis()
        token_key = cls._get_token_key(token)

        await redis_client.expire(token_key, new_timeout)

        login_id = await cls.getLoginIdByToken(token)
        if login_id:
            session_key = cls._get_session_key(login_id)
            await redis_client.expire(session_key, new_timeout)
            token_data = await cls._get_token_data(token)
            created_at = datetime.now(timezone.utc)
            if token_data and token_data.get("created_at"):
                try:
                    created_at = datetime.fromisoformat(token_data["created_at"])
                except (ValueError, TypeError):
                    pass
            expires_at = int(datetime.now(timezone.utc).timestamp()) + new_timeout
            pipe = redis_client.pipeline()
            pipe.zadd(cls._get_token_expiry_index_key(), {token: expires_at})
            pipe.hset(cls._get_token_owner_key(), token, str(login_id))
            pipe.zadd(cls._get_session_expiry_key(), {str(login_id): expires_at})
            pipe.zadd(cls._get_session_index_key(), {str(login_id): created_at.timestamp()})
            await pipe.execute()
            await cls._refresh_session_indexes(str(login_id))

    @classmethod
    async def getTokenValueByLoginId(cls, login_id: Union[str, int]) -> Optional[str]:
        redis_client = cls._get_redis()
        session_key = cls._get_session_key(str(login_id))
        tokens = await redis_client.smembers(session_key)
        if tokens:
            t = next(iter(tokens))
            return t.decode() if isinstance(t, bytes) else t
        return None

    @classmethod
    async def getTokenValuesByLoginId(cls, login_id: Union[str, int]) -> List[str]:
        redis_client = cls._get_redis()
        session_key = cls._get_session_key(str(login_id))
        tokens = await redis_client.smembers(session_key)
        return [t.decode() if isinstance(t, bytes) else t for t in tokens]

    @classmethod
    async def disable(cls, login_id: Union[str, int], time: int):
        redis_client = cls._get_redis()
        disable_key = f"{cls.DISABLE_KEY}{login_id}"
        await redis_client.setex(disable_key, time, "1")

    @classmethod
    async def isDisable(cls, login_id: Union[str, int]) -> bool:
        redis_client = cls._get_redis()
        disable_key = f"{cls.DISABLE_KEY}{login_id}"
        return await redis_client.exists(disable_key) > 0

    @classmethod
    async def checkDisable(cls, login_id: Union[str, int]):
        if await cls.isDisable(login_id):
            raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="账号已被禁用")

    @classmethod
    async def getDisableTime(cls, login_id: Union[str, int]) -> int:
        redis_client = cls._get_redis()
        disable_key = f"{cls.DISABLE_KEY}{login_id}"
        ttl = await redis_client.ttl(disable_key)
        return ttl if ttl > 0 else -1

    @classmethod
    async def untieDisable(cls, login_id: Union[str, int]):
        redis_client = cls._get_redis()
        disable_key = f"{cls.DISABLE_KEY}{login_id}"
        await redis_client.delete(disable_key)
