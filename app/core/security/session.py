from dataclasses import asdict, dataclass
import json
from typing import Any, TypedDict

from app.core.config.enums import DataScope, GrantEffect, GrantSubjectType, LoginScope, UserType

from app.core.config.settings import settings
from app.platform.cache.keys import login_account_tokens_key, login_token_key
from app.platform.cache.redis import get_redis


class PermissionGrantPayload(TypedDict):
    """会话中缓存的权限项结构，避免使用无约束的裸字典。"""

    permission_key: str
    data_scope: DataScope | str
    custom_scope_dept_ids: list[str]
    effect: GrantEffect | str
    source_type: GrantSubjectType | str
    source_id: str


@dataclass(slots=True)
class SessionPayload:
    """登录会话载荷，保存鉴权与数据权限判断所需的最小上下文。"""

    token: str
    account_id: str
    account_type: UserType | str
    login_scope: LoginScope | str
    role_ids: list[str]
    dept_ids: list[str]
    group_ids: list[str]
    permission_keys: list[str]
    permission_grants: list[PermissionGrantPayload]


class SessionStore:
    """会话存储门面，优先使用 Redis，必要时回退到进程内存。"""

    def __init__(self) -> None:
        self._memory: dict[str, dict[str, Any]] = {}

    async def set(self, payload: SessionPayload, ttl_seconds: int) -> None:
        """写入登录会话，并同步维护用户到 token 的反向索引。"""
        data = asdict(payload)
        redis = get_redis()
        if redis:
            await redis.setex(login_token_key(payload.token), ttl_seconds, json.dumps(data))
            await redis.sadd(login_account_tokens_key(str(payload.account_type), payload.account_id), payload.token)
            return
        if not settings.auth.enable_memory_session_fallback:
            raise RuntimeError("Redis is disabled and memory fallback is not allowed")
        self._memory[payload.token] = data

    async def get(self, token: str) -> SessionPayload | None:
        """按 token 读取会话，调用方无需关心底层来自 Redis 还是内存回退。"""
        redis = get_redis()
        if redis:
            raw = await redis.get(login_token_key(token))
            if not raw:
                return None
            raw_text = raw.decode("utf-8") if isinstance(raw, bytes) else raw
            data = json.loads(raw_text)
            return SessionPayload(**data)
        data = self._memory.get(token)
        return SessionPayload(**data) if data else None

    async def delete(self, token: str) -> None:
        """删除会话，并在 Redis 模式下同步清理用户维度的 token 索引。"""
        redis = get_redis()
        if redis:
            payload = await self.get(token)
            await redis.delete(login_token_key(token))
            if payload:
                await redis.srem(login_account_tokens_key(str(payload.account_type), payload.account_id), token)
            return
        self._memory.pop(token, None)


session_store = SessionStore()
