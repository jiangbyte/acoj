from dataclasses import asdict, dataclass, field
import json
from typing import TypedDict

from app.core.config.settings import settings
from app.core.config.enums import AccountType, DataScope

from app.platform.cache.keys import login_account_tokens_key, login_token_key
from app.platform.cache.redis import get_redis


class PermissionGrantPayload(TypedDict):
    """会话中缓存的权限项结构，避免使用无约束的裸字典。"""

    permission_key: str
    data_scope: DataScope | str
    custom_scope_dept_ids: list[str]
    effect: str
    source_type: str
    source_id: str


@dataclass(slots=True)
class SessionPayload:
    """登录会话载荷，保存鉴权与数据权限判断所需的最小上下文。"""

    token: str
    account_id: str
    account_type: AccountType | str
    role_ids: list[str] = field(default_factory=list)
    dept_ids: list[str] = field(default_factory=list)
    group_ids: list[str] = field(default_factory=list)
    resource_ids: list[str] = field(default_factory=list)
    button_codes: list[str] = field(default_factory=list)
    permission_keys: list[str] = field(default_factory=list)
    permission_grants: list[PermissionGrantPayload] = field(default_factory=list)


class SessionStore:
    """会话存储门面，Redis 是运行必需依赖。"""

    async def set(self, payload: SessionPayload, ttl_seconds: int) -> None:
        """写入登录会话，并同步维护用户到 token 的反向索引。"""
        data = asdict(payload)
        redis = self._get_required_redis()
        await redis.setex(login_token_key(payload.token), ttl_seconds, json.dumps(data))
        await redis.sadd(
            login_account_tokens_key(str(payload.account_type), payload.account_id),
            payload.token,
        )

    async def get(self, token: str) -> SessionPayload | None:
        """按 token 从 Redis 读取会话。"""
        redis = self._get_required_redis()
        raw = await redis.get(login_token_key(token))
        if not raw:
            return None
        raw_text = raw.decode("utf-8") if isinstance(raw, bytes) else raw
        data = json.loads(raw_text)
        return SessionPayload(**data)

    async def get_account_tokens(self, account_type: str, account_id: str) -> list[str]:
        """读取指定账户当前在线 token 列表，用于授权变更后刷新会话权限。"""
        redis = self._get_required_redis()
        values = await redis.smembers(login_account_tokens_key(account_type, account_id))
        return [
            value.decode("utf-8") if isinstance(value, bytes) else str(value)
            for value in values
        ]

    async def refresh_account_sessions(
        self,
        account_type: str,
        account_id: str,
        payload_factory,
    ) -> None:
        """刷新某个账户所有在线会话中的授权上下文，保留原 token 不变。"""
        tokens = await self.get_account_tokens(account_type, account_id)
        for token in tokens:
            current = await self.get(token)
            if not current:
                continue
            refreshed = await payload_factory(token)
            await self.set(refreshed, ttl_seconds=settings.auth.token_ttl_seconds)

    async def delete(self, token: str) -> None:
        """删除会话，并在 Redis 模式下同步清理用户维度的 token 索引。"""
        redis = self._get_required_redis()
        payload = await self.get(token)
        await redis.delete(login_token_key(token))
        if payload:
            await redis.srem(
                login_account_tokens_key(str(payload.account_type), payload.account_id), token
            )

    async def delete_account_sessions(self, account_type: str, account_id: str) -> None:
        """删除指定账户的所有在线会话和账户维度 token 索引。"""
        redis = self._get_required_redis()
        account_key = login_account_tokens_key(account_type, account_id)
        tokens = await self.get_account_tokens(account_type, account_id)
        for token in tokens:
            await redis.delete(login_token_key(token))
        await redis.delete(account_key)

    def _get_required_redis(self):
        redis = get_redis()
        if redis is None:
            raise RuntimeError("Redis is required for session store")
        return redis


session_store = SessionStore()
